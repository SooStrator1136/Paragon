package com.paragon.client.systems.module.impl.client.rotation;

import com.paragon.api.util.Wrapper;
import net.minecraft.network.play.client.CPacketPlayer;

/**
 * @author Wolfsurge
 * @since 23/03/22
 */
public class Rotation implements Wrapper {

    private final float yaw;
    private final float pitch;
    private final Rotate rotate;
    private final RotationPriority priority;

    public Rotation(float yaw, float pitch, Rotate rotate, RotationPriority priority) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.rotate = rotate;
        this.priority = priority;
    }

    public void doRotate() {
        if (rotate.equals(Rotate.NONE)) {
            return;
        }

        switch (rotate) {
            case PACKET:
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(getYaw(), getPitch(), mc.player.onGround));
                break;
            case LEGIT:
                mc.player.rotationYaw = getYaw();
                mc.player.rotationYawHead = getYaw();
                mc.player.rotationPitch = getPitch();
                break;
        }
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public Rotate getRotate() {
        return rotate;
    }

    public RotationPriority getPriority() {
        return priority;
    }
}
