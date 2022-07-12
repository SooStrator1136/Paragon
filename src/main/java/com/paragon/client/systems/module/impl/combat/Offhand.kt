package com.paragon.client.systems.module.impl.combat

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.calculations.Timer
import com.paragon.api.util.entity.EntityUtil
import com.paragon.api.util.player.InventoryUtil
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item
import net.minecraft.network.play.client.CPacketCloseWindow
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.util.math.Vec3d

/**
 * @author Wolfsurge
 */
object Offhand : Module("Offhand", Category.COMBAT, "Manages the item in your offhand") {

    private val item = Setting("Item", EnumItem.CRYSTAL)
        .setDescription("The item to prioritise in your offhand")

    private val fallback = Setting("Fallback", EnumItem.TOTEM)
        .setDescription("The item to fallback to if the priority item isn't found")

    // TODO: Add dynamic gapple switching

    private val safety = Setting("Safety", true)
        .setDescription("Switch to a totem in dangerous situations")

    private val health = Setting("Health", true)
        .setDescription("Switch to a totem when you are low on health")
        .setParentSetting(safety)

    private val healthValue = Setting("HealthValue", 10f, 0f, 20f, 1f)
        .setDescription("The health value for when to switch to a totem")
        .setParentSetting(safety)
        .setVisibility(health::value)

    private val falling = Setting("Falling", true)
        .setDescription("Switch to a totem when you are falling")
        .setParentSetting(safety)

    private val elytra = Setting("Elytra", true)
        .setDescription("Switch to a totem when you are flying")
        .setParentSetting(safety)

    private val lava = Setting("Lava", true)
        .setDescription("Switch to a totem when you are in lava")
        .setParentSetting(safety)

    private val fire = Setting("Fire", false)
        .setDescription("Switch to a totem when you are on fire")
        .setParentSetting(safety)

    private val crystal = Setting("Crystal", true)
        .setDescription("Switch to a totem when in vicinity of a potentially deadly crystal")
        .setParentSetting(safety)

    private val delay = Setting("Delay", 5f, 0f, 200f, 1f)
        .setDescription("The delay before switching items")

    private val stopMotion = Setting("StopMotion", true)
        .setDescription("Stop the player when swapping")

    private val inventorySpoof = Setting("InventorySpoof", true)
        .setDescription("Spoof opening your inventory")

    private val swapTimer = Timer()

    override fun onTick() {
        if (nullCheck() || minecraft.currentScreen is GuiContainer || !swapTimer.hasMSPassed(delay.value.toDouble())) {
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
            minecraft.player.connection.sendPacket(CPacketEntityAction(minecraft.player, CPacketEntityAction.Action.OPEN_INVENTORY))
        }

        swapOffhand(item)

        if (inventorySpoof.value) {
            minecraft.player.connection.sendPacket(CPacketCloseWindow(minecraft.player.inventoryContainer.windowId))
        }

        swapTimer.reset()
    }

    private fun getSwitchSlot(): Int {
        var swapItem: Item?

        swapItem = if (InventoryUtil.getItemSlot(item.value.item) != -1) {
            item.value.item
        } else {
            fallback.value.item
        }

        if (safety.value && InventoryUtil.getItemSlot(Items.TOTEM_OF_UNDYING) > -1 && shouldApplySafety()) {
            swapItem = Items.TOTEM_OF_UNDYING
        }

        return if (minecraft.player.heldItemOffhand.item == swapItem) {
            -1
        } else {
            InventoryUtil.getItemSlot(swapItem)
        }
    }

    private fun shouldApplySafety(): Boolean {
        if (crystal.value) {
            var deadlyCrystals = 0
            minecraft.world.loadedEntityList.stream()
                .filter { entity -> entity is EntityEnderCrystal && entity.getDistance(minecraft.player) <= 6 }
                .forEach { entity ->
                    if (AutoCrystalRewrite.INSTANCE.calculateDamage(Vec3d(entity.posX, entity.posY, entity.posZ), minecraft.player) > EntityUtil.getEntityHealth(minecraft.player)) {
                        deadlyCrystals++
                    }
                }
            return true
        }

        return health.value && EntityUtil.getEntityHealth(minecraft.player) <= healthValue.value ||
                falling.value && minecraft.player.fallDistance >= 3 ||
                elytra.value && minecraft.player.isElytraFlying ||
                lava.value && minecraft.player.isInLava ||
                fire.value && minecraft.player.isBurning
    }

    private fun swapOffhand(slot: Int) {
        val window = minecraft.player.inventoryContainer.windowId

        var returnSlot = -1
        if (minecraft.player.inventory.getStackInSlot(slot).isEmpty) {
            returnSlot = slot
        } else {
            for (i in 9..44) {
                if (minecraft.player.inventory.getStackInSlot(i).isEmpty) {
                    returnSlot = i
                    break
                }
            }
        }

        minecraft.playerController.windowClick(window, slot, 0, ClickType.PICKUP, minecraft.player)
        minecraft.playerController.windowClick(window, 45, 0, ClickType.PICKUP, minecraft.player)

        if (returnSlot != -1) {
            minecraft.playerController.windowClick(window, slot, 0, ClickType.PICKUP, minecraft.player)
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

}