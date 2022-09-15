package com.paragon.impl.module.combat

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Bind.Device
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.combat.CrystalUtil.getDamageToEntity
import com.paragon.util.entity.EntityUtil
import com.paragon.util.player.InventoryUtil
import com.paragon.util.world.BlockUtil
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item
import net.minecraft.network.play.client.CPacketCloseWindow
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.util.math.BlockPos
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import kotlin.math.floor

/**
 * @author Surge
 */
object Offhand : Module("Offhand", Category.COMBAT, "Manages the item in your offhand") {

    private val item = Setting(
        "Item", EnumItem.CRYSTAL
    ) describedBy "The item to prioritise in your offhand"

    private val fallback = Setting(
        "Fallback", EnumItem.GAPPLE
    ) describedBy "The item to fallback to if the priority item isn't found"

    private val keySwap = Setting(
        "KeySwap", EnumItem.TOTEM
    ) describedBy "Swap the item in your offhand when you press a key"

    private val key = Setting(
        "Key", Bind(0, Device.KEYBOARD)
    ) describedBy "The key to press to swap the item in your offhand" subOf keySwap

    private val overrideSafety = Setting(
        "OverrideSafety", false
    ) describedBy "Override the safety check to swap the item in your offhand" subOf keySwap

    private val dynamicGapple = Setting(
        "DynamicGapple", DynamicGapple.SWORD
    ) describedBy "When to dynamically switch to a gapple"

    private val safety = Setting(
        "Safety", true
    ) describedBy "Switch to a totem in dangerous situations"

    private val health = Setting(
        "Health", true
    ) describedBy "Switch to a totem when you are low on health" subOf safety

    private val healthValue = Setting(
        "HealthValue", 10f, 0f, 20f, 1f
    ) describedBy "The health value for when to switch to a totem" subOf safety visibleWhen { health.value }

    private val falling = Setting(
        "Falling", true
    ) describedBy "Switch to a totem when you are falling" subOf safety

    private val elytra = Setting(
        "Elytra", true
    ) describedBy "Switch to a totem when you are flying" subOf safety

    private val lava = Setting(
        "Lava", true
    ) describedBy "Switch to a totem when you are in lava" subOf safety

    private val fire = Setting(
        "Fire", false
    ) describedBy "Switch to a totem when you are on fire" subOf safety

    private val crystal = Setting(
        "Crystal", true
    ) describedBy "Switch to a totem when in vicinity of a potentially deadly crystal" subOf safety

    private val delay = Setting(
        "Delay", 5f, 0f, 200f, 1f
    ) describedBy "The delay before switching items"

    private val stopMotion = Setting(
        "StopMotion", true
    ) describedBy "Stop the player when swapping"

    private val inventorySpoof = Setting(
        "InventorySpoof", true
    ) describedBy "Spoof opening your inventory"

    private val swapTimer = Timer()

    override fun onTick() {
        if (minecraft.anyNull || minecraft.player.isDead || minecraft.currentScreen is GuiContainer || !swapTimer.hasMSPassed(delay.value.toDouble())) {
            return
        }

        val item = getSwitchSlot()

        if (item == -1) {
            return
        }

        if (stopMotion.value) {
            minecraft.player.setVelocity(0.0, minecraft.player.motionY, 0.0)
        }

        if (inventorySpoof.value) {
            minecraft.player.connection.sendPacket(
                CPacketEntityAction(
                    minecraft.player, CPacketEntityAction.Action.OPEN_INVENTORY
                )
            )
        }

        swapOffhand(item)

        swapTimer.reset()
    }

    private fun getSwitchSlot(): Int {
        var swapItem: Item

        swapItem = if (InventoryUtil.getItemSlot(item.value.item) != -1) {
            item.value.item
        }
        else {
            fallback.value.item
        }

        var keyPressed = false

        // isPressed() doesn't work :(
        if (key.value.buttonCode != 0) {
            when (key.value.device) {
                Device.KEYBOARD -> if (Keyboard.isKeyDown(key.value.buttonCode)) {
                    keyPressed = true
                }

                Device.MOUSE -> if (Mouse.isButtonDown(key.value.buttonCode)) {
                    keyPressed = true
                }
            }
        }

        if (shouldDynamicGapple()) {
            swapItem = Items.GOLDEN_APPLE
        }

        if (keyPressed) {
            swapItem = keySwap.value.item
        }

        if (safety.value && InventoryUtil.getItemSlot(Items.TOTEM_OF_UNDYING) > -1 && shouldApplySafety()) {
            swapItem = Items.TOTEM_OF_UNDYING
        }

        if (keyPressed && overrideSafety.value) {
            swapItem = keySwap.value.item
        }

        return if (minecraft.player.heldItemOffhand.item == swapItem) {
            -1
        }
        else {
            InventoryUtil.getItemSlot(swapItem)
        }
    }

    private fun shouldApplySafety(): Boolean {
        if (crystal.value) {
            var deadlyCrystals = 0

            minecraft.world.loadedEntityList.stream().filter { it.getDistance(minecraft.player) <= 6 }.forEach {
                if (it is EntityEnderCrystal && it.getDamageToEntity(minecraft.player) > EntityUtil.getEntityHealth(minecraft.player)) {
                    deadlyCrystals++
                }
            }

            if (deadlyCrystals > 0) {
                return true
            }
        }

        return health.value && EntityUtil.getEntityHealth(minecraft.player) <= healthValue.value || falling.value && minecraft.player.fallDistance >= 3 || elytra.value && minecraft.player.isElytraFlying || lava.value && minecraft.player.isInLava || fire.value && minecraft.player.isBurning
    }

    private fun shouldDynamicGapple(): Boolean {
        if (dynamicGapple.value == DynamicGapple.NEVER) {
            return false
        }

        if ((dynamicGapple.value == DynamicGapple.SWORD || dynamicGapple.value == DynamicGapple.BOTH) && InventoryUtil.isHoldingSword) {
            return true
        }

        if ((dynamicGapple.value == DynamicGapple.HOLE || dynamicGapple.value == DynamicGapple.BOTH) && BlockUtil.isSafeHole(
                BlockPos(
                    floor(minecraft.player.posX), floor(minecraft.player.posY), floor(minecraft.player.posZ)
                ), true
            )
        ) {
            return true
        }

        return false
    }

    private fun swapOffhand(slot: Int) {
        val window = minecraft.player.inventoryContainer.windowId

        minecraft.playerController.windowClick(window, slot, 0, ClickType.PICKUP, minecraft.player)
        minecraft.playerController.windowClick(window, 45, 0, ClickType.PICKUP, minecraft.player)

        var returnSlot = -1

        for (i in 9..44) {
            if (minecraft.player.inventory.getStackInSlot(i).isEmpty) {
                returnSlot = i
                break
            }
        }

        if (returnSlot != -1) {
            minecraft.playerController.windowClick(0, slot, 0, ClickType.PICKUP, minecraft.player)
        }

        minecraft.playerController.updateController()

        if (inventorySpoof.value && minecraft.connection != null) {
            minecraft.connection?.sendPacket(CPacketCloseWindow(window))
        }
    }

    enum class EnumItem(val item: Item) {
        /**
         * Switch to crystal
         */
        CRYSTAL(Items.END_CRYSTAL),

        /**
         * Switch to totem
         */
        TOTEM(Items.TOTEM_OF_UNDYING),

        /**
         * Switch to gapple
         */
        GAPPLE(Items.GOLDEN_APPLE)
    }

    enum class DynamicGapple {
        /**
         * Swap to gapple when holding a sword in your main hand
         */
        SWORD,

        /**
         * Swap to gapple when you're in a hole
         */
        HOLE,

        /**
         * Swap to gapple for both events
         */
        BOTH,

        /**
         * Never switch to gapples
         */
        NEVER
    }

}