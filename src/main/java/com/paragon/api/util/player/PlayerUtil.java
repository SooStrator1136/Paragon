package com.paragon.api.util.player;

import com.paragon.api.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumAction;

public class PlayerUtil implements Wrapper {

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
        return mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0;
    }

    public static void move(float speed) {
        Entity mover = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;

        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        float playerYaw = mc.player.rotationYaw;

        if (mover != null) {
            if (forward != 0) {
                if (strafe >= 1) {
                    playerYaw += (float) (forward > 0 ? -45 : 45);
                    strafe = 0;
                } else if (strafe <= -1) {
                    playerYaw += (float) (forward > 0 ? 45 : -45);
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

}
