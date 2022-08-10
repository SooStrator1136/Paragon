package com.paragon.api.util.player;

import com.paragon.api.util.Wrapper;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public final class PlayerUtil implements Wrapper {

    public static void stopMotion(float fallSpeed) {
        mc.player.setVelocity(0, fallSpeed, 0);
    }

    public static boolean isCollided() {
        return mc.player.collidedHorizontally || mc.player.collidedVertically;
    }

    public static boolean isInLiquid() {
        return mc.player.isInWater() || mc.player.isInLava();
    }

    public static void lockLimbs() {
        mc.player.prevLimbSwingAmount = 0;
        mc.player.limbSwingAmount = 0;
        mc.player.limbSwing = 0;
    }

    public static boolean isMoving() {
        return mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0 || mc.player.posX != mc.player.lastTickPosX || mc.player.posZ != mc.player.lastTickPosZ;
    }

    public static void move(float speed) {
        Entity mover = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;

        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        float playerYaw = mc.player.rotationYaw;

        if (mover != null) {
            if (forward != 0) {
                if (strafe >= 1) {
                    playerYaw += (forward > 0 ? -45 : 45);
                    strafe = 0;
                } else if (strafe <= -1) {
                    playerYaw += (forward > 0 ? 45 : -45);
                    strafe = 0;
                }

                if (forward > 0) {
                    forward = 1;
                } else if (forward < 0) {
                    forward = -1;
                }
            }

            double sin = Math.sin(Math.toRadians(playerYaw + 90));
            double cos = Math.cos(Math.toRadians(playerYaw + 90));

            mover.motionX = (double) forward * speed * cos + (double) strafe * speed * sin;
            mover.motionZ = (double) forward * speed * sin - (double) strafe * speed * cos;
            mover.stepHeight = 0.6f;

            if (!isMoving()) {
                mover.motionX = 0;
                mover.motionZ = 0;
            }
        }
    }

    public static Vec3d forward(double speed) {
        float forwardInput = mc.player.movementInput.moveForward;
        float strafeInput = mc.player.movementInput.moveStrafe;
        float playerYaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

        if (forwardInput != 0.0f) {
            if (strafeInput > 0.0f) {
                playerYaw += ((forwardInput > 0.0f) ? -45 : 45);
            } else if (strafeInput < 0.0f) {
                playerYaw += ((forwardInput > 0.0f) ? 45 : -45);
            }

            strafeInput = 0.0f;

            if (forwardInput > 0.0f) {
                forwardInput = 1.0f;
            } else if (forwardInput < 0.0f) {
                forwardInput = -1.0f;
            }
        }

        double sin = Math.sin(Math.toRadians(playerYaw + 90.0f));
        double cos = Math.cos(Math.toRadians(playerYaw + 90.0f));

        double posX = forwardInput * speed * cos + strafeInput * speed * sin;
        double posZ = forwardInput * speed * sin - strafeInput * speed * cos;

        return new Vec3d(posX, mc.player.posY, posZ);
    }

    public static void propel(float speed) {
        float yaw = mc.player.rotationYaw;
        float pitch = mc.player.rotationPitch;

        mc.player.motionX -= Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * speed;
        mc.player.motionZ += Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * speed;
        mc.player.motionY += -(Math.sin(Math.toRadians(pitch))) * speed;
    }

    public static boolean isPlayerEating() {
        return mc.player.isHandActive() && mc.player.getActiveItemStack().getItemUseAction().equals(EnumAction.EAT);
    }

    public static boolean isPlayerDrinking() {
        return mc.player.isHandActive() && mc.player.getActiveItemStack().getItemUseAction().equals(EnumAction.DRINK);
    }

    public static boolean isPlayerConsuming() {
        return mc.player.isHandActive() && (mc.player.getActiveItemStack().getItemUseAction().equals(EnumAction.EAT) || mc.player.getActiveItemStack().getItemUseAction().equals(EnumAction.DRINK));
    }

    public static EnumFaceDirection getDirection() {
        return EnumFaceDirection.getFacing(EnumFacing.fromAngle(mc.player.rotationYaw));
    }

    public static String getAxis(EnumFaceDirection direction) {
        switch (direction) {
            case NORTH:
                return "-Z";

            case SOUTH:
                return "+Z";

            case EAST:
                return "+X";

            case WEST:
                return "-X";
        }

        return "";
    }

    public static double getBaseMoveSpeed() {
        return 0.2873 * (mc.player.isPotionActive(MobEffects.SPEED) ? 1 + (0.2 * (mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1)) : 1);
    }

}
