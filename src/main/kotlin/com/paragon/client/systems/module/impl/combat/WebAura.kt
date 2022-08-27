package com.paragon.client.systems.module.impl.combat

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.player.InventoryUtil
import com.paragon.api.util.player.PlayerUtil
import com.paragon.api.util.player.RotationUtil
import com.paragon.api.util.world.BlockUtil
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import com.paragon.mixins.accessor.IEntity
import com.paragon.client.managers.rotation.Rotate
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

/**
 * Was pretty late when I wrote this
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

    private val doSwing = Setting(
        "Swing",
        false
    ) describedBy "Swing player arm when placing"

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

                RotationUtil.rotate(RotationUtil.getRotationToBlockPos(playerBlock, 0.5), rotationMode.value)
                minecraft.playerController.processRightClickBlock(
                    minecraft.player,
                    minecraft.world,
                    playerBlock.down(),
                    EnumFacing.UP,
                    Vec3d(0.5, 0.5, 0.5),
                    getWebHand()
                )
                if (doSwing.value) {
                    minecraft.player.swingArm(EnumHand.MAIN_HAND)
                }
                switchBack()
            }

            TargetMode.OTHERS -> {
                val possibleTargets = minecraft.world.loadedEntityList.filter {
                    !it.isDead
                            && it != minecraft.player
                            && it is EntityPlayer
                            && !(it as IEntity).isInWeb
                }.map { PlayerUtil.getBlockUnder(it) }

                if (possibleTargets.isEmpty()) {
                    return
                }

                val placePos = possibleTargets.minWith(Comparator.comparingDouble {
                    it?.getDistance(
                        minecraft.player.posX.toInt(),
                        (minecraft.player.posY + minecraft.player.getEyeHeight()).toInt(),
                        minecraft.player.posZ.toInt()
                    ) ?: Double.MAX_VALUE
                }) ?: return

                if (placePos.getDistance(
                        minecraft.player.posX.toInt(),
                        (minecraft.player.posY + minecraft.player.getEyeHeight()).toInt(),
                        minecraft.player.posZ.toInt()
                    ) > range.value
                ) {
                    return
                }

                RotationUtil.rotate(RotationUtil.getRotationToBlockPos(placePos, 0.5), rotationMode.value)
                minecraft.playerController.processRightClickBlock(
                    minecraft.player,
                    minecraft.world,
                    placePos,
                    EnumFacing.UP,
                    Vec3d(0.5, 0.5, 0.5),
                    getWebHand()
                )
                if (doSwing.value) {
                    minecraft.player.swingArm(EnumHand.MAIN_HAND)
                }
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