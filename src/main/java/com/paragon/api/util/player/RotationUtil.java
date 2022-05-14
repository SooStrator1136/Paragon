package com.paragon.api.util.player;

import com.paragon.api.util.Wrapper;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class RotationUtil implements Wrapper {

    /**
     * Gets the rotation to a block position
     *
     * @param pos The block position to calculate angles to
     * @return The calculated angles
     */
    public static Vec2f getRotationToBlockPos(BlockPos pos) {
        return getRotationToVec3d(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    /**
     * Gets the yaw and pitch to rotate to a Vec3D
     *
     * @param vec3d The Vec3D to calculate rotations to
     * @return A Vec2f of the angles
     */
    public static Vec2f getRotationToVec3d(Vec3d vec3d) {
        float yaw = (float) (Math.toDegrees(
                Math.atan2(
                        vec3d.subtract(mc.player.getPositionEyes(1)).z,
                        vec3d.subtract(mc.player.getPositionEyes(1)).x)) - 90);

        float pitch = (float) Math.toDegrees(
                -Math.atan2(
                        vec3d.subtract(mc.player.getPositionEyes(1)).y,
                        Math.hypot(
                                vec3d.subtract(mc.player.getPositionEyes(1)).x,
                                vec3d.subtract(mc.player.getPositionEyes(1)).z)));

        return new Vec2f(MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch));
    }

    public static void rotate(Vec2f rotationVec, boolean packet) {
        if (packet) {
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotationVec.x, rotationVec.y, mc.player.onGround));
            return;
        }

        mc.player.rotationYaw = rotationVec.x;
        mc.player.rotationYawHead = rotationVec.x;
        mc.player.rotationPitch = rotationVec.y;
    }

}
