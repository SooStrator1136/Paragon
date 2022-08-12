package com.paragon.api.util.player;

import com.paragon.api.util.Wrapper;
import com.paragon.client.managers.rotation.Rotate;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RotationUtil implements Wrapper {

    /**
     * Gets the rotation to a block position
     *
     * @param pos The block position to calculate angles to
     * @param yOffset The y offset to use
     * @return The calculated angles
     */
    public static Vec2f getRotationToBlockPos(final BlockPos pos, final double yOffset) {
        return getRotationToVec3d(new Vec3d(pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5));
    }

    /**
     * Gets the yaw and pitch to rotate to a Vec3D
     *
     * @param vec3d The Vec3D to calculate rotations to
     * @return A Vec2f of the angles
     */
    public static Vec2f getRotationToVec3d(final Vec3d vec3d) {
        final float yaw = (float) (Math.toDegrees(
                Math.atan2(
                        vec3d.subtract(mc.player.getPositionEyes(1)).z,
                        vec3d.subtract(mc.player.getPositionEyes(1)).x)) - 90);

        final float pitch = (float) Math.toDegrees(
                -Math.atan2(
                        vec3d.subtract(mc.player.getPositionEyes(1)).y,
                        Math.hypot(
                                vec3d.subtract(mc.player.getPositionEyes(1)).x,
                                vec3d.subtract(mc.player.getPositionEyes(1)).z)));

        return new Vec2f(MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch));
    }

    public static void rotate(final Vec2f rotationVec, final Rotate mode) {
        if (mode == Rotate.NONE) {
            return;
        }

        rotate(rotationVec, mode == Rotate.PACKET);
    }

    public static void rotate(final Vec2f rotationVec, final boolean packet) {
        if (packet) {
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotationVec.x, rotationVec.y, mc.player.onGround));
            return;
        }

        mc.player.rotationYaw = rotationVec.x;
        mc.player.rotationYawHead = rotationVec.x;
        mc.player.rotationPitch = rotationVec.y;
    }

    public static float normalizeAngle(float angle) {
        float normalizedAngle = angle;
        normalizedAngle %= 360.0f;

        if (normalizedAngle >= 180.0f) {
            normalizedAngle -= 360.0f;
        }

        if (normalizedAngle < -180.0f) {
            normalizedAngle += 360.0f;
        }

        return normalizedAngle;
    }

}
