package com.paragon.client.systems.module.impl.combat

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.player.InventoryUtil
import com.paragon.api.util.player.PlacementUtil
import com.paragon.api.util.player.PlayerUtil
import com.paragon.api.util.player.RotationUtil
import com.paragon.api.util.world.BlockUtil
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import com.paragon.api.util.world.BlockUtil.getDistanceToEyes
import com.paragon.client.managers.rotation.Rotate
import com.paragon.client.managers.rotation.Rotation
import com.paragon.client.managers.rotation.RotationPriority
import com.paragon.mixins.accessor.IEntity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos

/**
 * 我寫這篇文章的時候已經很晚了
 *
 * @author SooStrator1136
 */
object WebAura : Module("WebAura", Category.COMBAT, "Spiderman on drugs wtf") {

    private val mode = Setting("TargetMode", TargetMode.OTHERS)

    private val rotationMode = Setting("Rotation", Rotate.NONE)

    private val onlyHole = Setting(
        "Only holes",
        false
    ) describedBy "Only places webs when you are in a hole" visibleWhen { mode.value == TargetMode.SELF }

    private val range = Setting(
        "Range",
        5F,
        2F,
        7F,
        0.5F
    ) describedBy "Range to place webs in" visibleWhen { mode.value == TargetMode.OTHERS }

    private val packetOnItemChange = Setting("SyncItem", true)

    private val itemCobweb = Item.getItemFromBlock(Blocks.WEB)

    override fun onTick() {
        if (minecraft.anyNull || InventoryUtil.getItemInHotbar(itemCobweb) == -1) {
            return
        }

        when (mode.value) {
            TargetMode.SELF -> {
                val playerBlock = BlockPos(minecraft.player.posX, minecraft.player.posY, minecraft.player.posZ)
                if ((onlyHole.value && !BlockUtil.isHole(playerBlock)) || playerBlock.getBlockAtPos() != Blocks.AIR) {
                    return
                }

                val placeRotation = RotationUtil.getRotationToBlockPos(playerBlock, 0.5)
                PlacementUtil.place(
                    playerBlock,
                    Rotation(placeRotation.x, placeRotation.y, rotationMode.value, RotationPriority.HIGH),
                    getWebHand()
                )
                switchBack()
            }

            TargetMode.OTHERS -> {
                val possibleTargets = minecraft.world.loadedEntityList.filter {
                    !it.isDead
                            && it != minecraft.player
                            && it is EntityPlayer
                            && !(it as IEntity).isInWeb
                }.mapNotNull {
                    val blockUnder = PlayerUtil.getBlockUnder(it)
                    return@mapNotNull if (blockUnder == null || blockUnder.getDistanceToEyes() > range.value) {
                        null
                    } else {
                        blockUnder
                    }
                }.also {
                    if (it.isEmpty()) {
                        return
                    }
                }

                val placePos = possibleTargets.minWith(Comparator.comparingDouble {
                    it.getDistanceToEyes()
                })

                val placeRotation = RotationUtil.getRotationToBlockPos(placePos, 0.5)
                PlacementUtil.place(
                    placePos,
                    Rotation(placeRotation.x, placeRotation.y, rotationMode.value, RotationPriority.HIGH),
                    getWebHand()
                )
                switchBack()
            }
        }
    }

    private var previousSlot = 0

    private fun getWebHand(): EnumHand { //Who puts  webs in their offhand cmon
        previousSlot = minecraft.player.inventory.currentItem
        minecraft.player.inventory.currentItem = InventoryUtil.getItemInHotbar(itemCobweb)
        if (packetOnItemChange.value) {
            minecraft.connection?.sendPacket(CPacketHeldItemChange(minecraft.player.inventory.currentItem))
        }

        return EnumHand.MAIN_HAND
    }

    private fun switchBack() {
        minecraft.player.inventory.currentItem = previousSlot
        if (packetOnItemChange.value) {
            minecraft.connection?.sendPacket(CPacketHeldItemChange(previousSlot))
        }
    }

    internal enum class TargetMode {
        SELF, OTHERS
    }

}