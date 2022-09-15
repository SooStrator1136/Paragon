package com.paragon.util.player

import com.paragon.impl.managers.rotation.Rotate
import com.paragon.impl.managers.rotation.Rotation
import com.paragon.util.Wrapper
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d

/**
 * @author Surge
 * @since 27/08/2022
 */
object PlacementUtil : Wrapper {

    @JvmStatic
    fun place(pos: BlockPos, rotation: Rotation, hand: EnumHand = EnumHand.MAIN_HAND) {
        EnumFacing.values().forEach {
            val offset = pos.offset(it)
            val opposite = it.opposite

            if (!offset.getVisibleSides().contains(opposite)) {
                return@forEach
            }

            val original = Vec2f(minecraft.player.rotationYaw, minecraft.player.rotationPitch)

            // Rotate to position
            if (rotation.rotate == Rotate.LEGIT) {
                minecraft.player.rotationYaw = rotation.yaw
                minecraft.player.rotationYawHead = rotation.yaw
                minecraft.player.rotationPitch = rotation.pitch
            }

            if (rotation.rotate != Rotate.NONE) {
                minecraft.player.connection.sendPacket(
                    CPacketPlayer.Rotation(
                        rotation.yaw, rotation.pitch, minecraft.player.onGround
                    )
                )
            }

            val vec = Vec3d(offset).add(Vec3d(0.5, 0.5, 0.5))

            minecraft.player.connection.sendPacket(
                CPacketEntityAction(
                    minecraft.player, CPacketEntityAction.Action.START_SNEAKING
                )
            )

            minecraft.playerController.processRightClickBlock(
                minecraft.player, minecraft.world, offset, it.opposite, vec, hand
            )

            minecraft.player.swingArm(hand)
            minecraft.player.connection.sendPacket(
                CPacketEntityAction(
                    minecraft.player, CPacketEntityAction.Action.STOP_SNEAKING
                )
            )

            if (rotation.rotate != Rotate.NONE) {
                if (rotation.rotate == Rotate.LEGIT) {
                    minecraft.player.rotationYaw = original.x
                    minecraft.player.rotationPitch = original.y
                    minecraft.player.rotationYawHead = original.x
                }

                minecraft.player.connection.sendPacket(
                    CPacketPlayer.Rotation(
                        original.x, original.y, minecraft.player.onGround
                    )
                )
            }

            return
        }
    }

    /**
     * PORTED FROM COSMOS
     *
     * @author linustouchtips
     *
     * I know you're probably going to say that the entire client is a cosmos paste
     * But I was just not sure about how to get the visible sides...
     *
     * (Reminder, I'm still bad at bypassing)
     *
     * Think of it as a compliment that people are using your code due to it being
     * reliable and good kek
     */
    private fun BlockPos.getVisibleSides(): List<EnumFacing> {
        val sides = arrayListOf<EnumFacing>()

        val center = Vec3d(this).add(Vec3d(0.5, 0.5, 0.5))

        val facing = Vec3d(
            minecraft.player.getPositionEyes(minecraft.renderPartialTicks).x - center.x, minecraft.player.getPositionEyes(minecraft.renderPartialTicks).y - center.y, minecraft.player.getPositionEyes(minecraft.renderPartialTicks).z - center.z
        )

        if (facing.x < -0.5) {
            sides.add(EnumFacing.WEST)
        }
        else if (facing.x > 0.5) {
            sides.add(EnumFacing.EAST)
        }
        else if (!minecraft.world.getBlockState(this).isFullBlock || !minecraft.world.isAirBlock(this)) {
            sides.add(EnumFacing.WEST)
            sides.add(EnumFacing.EAST)
        }

        if (facing.y < -0.5) {
            sides.add(EnumFacing.DOWN)
        }
        else if (facing.y > 0.5) {
            sides.add(EnumFacing.UP)
        }
        else if (!minecraft.world.getBlockState(this).isFullBlock || !minecraft.world.isAirBlock(this)) {
            sides.add(EnumFacing.DOWN)
            sides.add(EnumFacing.UP)
        }

        if (facing.z < -0.5) {
            sides.add(EnumFacing.NORTH)
        }
        else if (facing.z > 0.5) {
            sides.add(EnumFacing.SOUTH)
        }
        else if (!minecraft.world.getBlockState(this).isFullBlock || !minecraft.world.isAirBlock(this)) {
            sides.add(EnumFacing.NORTH)
            sides.add(EnumFacing.SOUTH)
        }

        return sides
    }

}