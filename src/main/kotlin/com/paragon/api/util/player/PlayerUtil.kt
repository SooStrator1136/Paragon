package com.paragon.api.util.player

import com.paragon.api.util.Wrapper
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import net.minecraft.client.renderer.EnumFaceDirection
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.init.MobEffects
import net.minecraft.item.EnumAction
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.sin

object PlayerUtil : Wrapper {

    @JvmStatic
    fun stopMotion(fallSpeed: Float) {
        minecraft.player.setVelocity(0.0, fallSpeed.toDouble(), 0.0)
    }

    val isCollided: Boolean
        get() = minecraft.player.collidedHorizontally || minecraft.player.collidedVertically

    val isInLiquid: Boolean
        get() = minecraft.player.isInWater || minecraft.player.isInLava

    @JvmStatic
    fun lockLimbs() {
        minecraft.player.prevLimbSwingAmount = 0f
        minecraft.player.limbSwingAmount = 0f
        minecraft.player.limbSwing = 0f
    }

    val isMoving: Boolean
        get() = minecraft.player.movementInput.moveForward != 0f || minecraft.player.movementInput.moveStrafe != 0f || minecraft.player.posX != minecraft.player.lastTickPosX || minecraft.player.posZ != minecraft.player.lastTickPosZ

    @JvmStatic
    fun move(speed: Float) {
        val mover = if (minecraft.player.isRiding) minecraft.player.ridingEntity else minecraft.player
        var forward = minecraft.player.movementInput.moveForward
        var strafe = minecraft.player.movementInput.moveStrafe
        var playerYaw = minecraft.player.rotationYaw

        if (mover != null) {
            if (forward != 0f) {
                if (strafe >= 1) {
                    playerYaw += (if (forward > 0) -45 else 45).toFloat()
                    strafe = 0f
                }

                else if (strafe <= -1) {
                    playerYaw += (if (forward > 0) 45 else -45).toFloat()
                    strafe = 0f
                }

                if (forward > 0) {
                    forward = 1f
                }

                else if (forward < 0) {
                    forward = -1f
                }
            }

            val sin = sin(Math.toRadians((playerYaw + 90).toDouble()))
            val cos = cos(Math.toRadians((playerYaw + 90).toDouble()))

            mover.motionX = forward.toDouble() * speed * cos + strafe.toDouble() * speed * sin
            mover.motionZ = forward.toDouble() * speed * sin - strafe.toDouble() * speed * cos

            mover.stepHeight = 0.6f

            if (!isMoving) {
                mover.motionX = 0.0
                mover.motionZ = 0.0
            }
        }
    }

    fun forward(speed: Double): Vec3d {
        var forwardInput = minecraft.player.movementInput.moveForward
        var strafeInput = minecraft.player.movementInput.moveStrafe
        var playerYaw = minecraft.player.prevRotationYaw + (minecraft.player.rotationYaw - minecraft.player.prevRotationYaw) * minecraft.renderPartialTicks

        if (forwardInput != 0.0f) {
            if (strafeInput > 0.0f) {
                playerYaw += (if (forwardInput > 0.0f) -45 else 45).toFloat()
            }

            else if (strafeInput < 0.0f) {
                playerYaw += (if (forwardInput > 0.0f) 45 else -45).toFloat()
            }

            strafeInput = 0.0f

            if (forwardInput > 0.0f) {
                forwardInput = 1.0f
            }

            else if (forwardInput < 0.0f) {
                forwardInput = -1.0f
            }
        }

        val sin = sin(Math.toRadians((playerYaw + 90.0f).toDouble()))
        val cos = cos(Math.toRadians((playerYaw + 90.0f).toDouble()))

        val posX = forwardInput * speed * cos + strafeInput * speed * sin
        val posZ = forwardInput * speed * sin - strafeInput * speed * cos

        return Vec3d(posX, minecraft.player.posY, posZ)
    }

    @JvmStatic
    fun propel(speed: Float) {
        val yaw = minecraft.player.rotationYaw

        val pitch = minecraft.player.rotationPitch
        minecraft.player.motionX -= sin(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * speed
        minecraft.player.motionZ += cos(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * speed
        minecraft.player.motionY += -sin(Math.toRadians(pitch.toDouble())) * speed
    }

    val isPlayerEating: Boolean
        get() = minecraft.player.isHandActive && minecraft.player.activeItemStack.itemUseAction == EnumAction.EAT

    val isPlayerDrinking: Boolean
        get() = minecraft.player.isHandActive && minecraft.player.activeItemStack.itemUseAction == EnumAction.DRINK

    val isPlayerConsuming: Boolean
        get() = minecraft.player.isHandActive && (minecraft.player.activeItemStack.itemUseAction == EnumAction.EAT || minecraft.player.activeItemStack.itemUseAction == EnumAction.DRINK)

    val direction: EnumFaceDirection
        get() = EnumFaceDirection.getFacing(EnumFacing.fromAngle(minecraft.player.rotationYaw.toDouble()))

    fun getAxis(direction: EnumFaceDirection?): String {
        when (direction) {
            EnumFaceDirection.NORTH -> return "-Z"
            EnumFaceDirection.SOUTH -> return "+Z"
            EnumFaceDirection.EAST -> return "+X"
            EnumFaceDirection.WEST -> return "-X"
            else -> {}
        }

        return ""
    }

    val baseMoveSpeed: Double
        get() = 0.2873 * if (minecraft.player.isPotionActive(MobEffects.SPEED)) 1.0 + 0.2 * (minecraft.player.getActivePotionEffect(MobEffects.SPEED)!!.amplifier + 1.0) else 1.0

    fun getBlockUnder(player: Entity): BlockPos? {
        var pos = BlockPos(player.posX, player.posY, player.posZ)
        var blockAtPos = pos.getBlockAtPos()
        while (pos.y > -2 && blockAtPos === Blocks.AIR) {
            pos = pos.down()
            blockAtPos = pos.getBlockAtPos()
        }
        return if (pos.y < 0) null else pos
    }

}