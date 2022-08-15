package com.paragon.client.systems.module.impl.misc

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.calculations.Timer
import com.paragon.api.util.player.RotationUtil
import com.paragon.api.util.world.BlockUtil
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import com.paragon.client.managers.rotation.Rotate
import com.paragon.client.managers.rotation.Rotation
import com.paragon.client.managers.rotation.RotationPriority
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.math.BlockPos

/**
 * @author SooStrator1136
 */
object Lawnmower : Module("Lawnmower", Category.MISC, "Removes grass and flowers") {

    private val range = Setting(
        "Range",
        4F,
        1F,
        7F,
        0.1F
    ) describedBy "The range in which to remove lawn"

    private val delay = Setting("Delay", 100.0, 0.0, 1000.0, 10.0)

    private val rotateMode = Setting("Rotation", Rotate.NONE)

    private val lawnBlocks = arrayOf(
        Blocks.TALLGRASS,
        Blocks.RED_FLOWER,
        Blocks.DOUBLE_PLANT,
        Blocks.YELLOW_FLOWER,
    )
    private val timer = Timer()
    private val toRemove = ArrayDeque<BlockPos>()

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        toRemove.addAll(BlockUtil.getSphere(range.value, true).filter {
            lawnBlocks.contains(it.getBlockAtPos()) && !toRemove.contains(it)
        })

        toRemove.filter {
            BlockUtil.canSeePos(it) && it.getDistance(
                minecraft.player.posX.toInt(),
                (minecraft.player.posY + minecraft.player.getEyeHeight()).toInt(),
                minecraft.player.posZ.toInt()
            ) <= range.value
        }

        if (timer.hasMSPassed(delay.value) && toRemove.isNotEmpty()) {
            val blockPos = toRemove.removeFirst()
            val rotation = RotationUtil.getRotationToBlockPos(blockPos, 0.5)

            if (rotateMode.value == Rotate.LEGIT) {
                Paragon.INSTANCE.rotationManager.addRotation(
                    Rotation(
                        rotation.x,
                        rotation.y,
                        rotateMode.value,
                        RotationPriority.HIGH
                    )
                )
            } else if (rotateMode.value == Rotate.PACKET) {
                minecraft.player.connection.sendPacket(
                    CPacketPlayer.Rotation(
                        rotation.x,
                        rotation.y,
                        minecraft.player.onGround
                    )
                )
            }

            minecraft.objectMouseOver.sideHit

            minecraft.player.connection.sendPacket(
                CPacketPlayerDigging(
                    CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
                    blockPos,
                    BlockUtil.getFacing(blockPos)
                )
            )

            timer.reset()
        }
    }

    override fun onDisable() {
        toRemove.clear()
    }

}