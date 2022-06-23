package com.paragon.client.systems.module.impl.client.rotation;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.event.player.RotationUpdateEvent;
import com.paragon.api.event.player.UpdateEvent;
import com.paragon.api.util.player.RotationUtil;
import com.paragon.asm.mixins.accessor.ICPacketPlayer;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Constant;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Wolfsurge
 */
@Constant
public class Rotations extends Module {

    public static Rotations INSTANCE;

    private final CopyOnWriteArrayList<Rotation> rotationsQueue = new CopyOnWriteArrayList<>();

    public static Setting<Step> yawStep = new Setting<>("YawStep", Step.THRESHOLD)
            .setDescription("The way to limit yaw");

    public static Setting<Float> yawThreshold = new Setting<>("Threshold", 40f, 1f, 100f, 1f)
            .setDescription("The limit to the yaw step")
            .setParentSetting(yawStep)
            .setVisibility(() -> yawStep.getValue().equals(Step.THRESHOLD));

    public static Setting<Step> pitchStep = new Setting<>("PitchStep", Step.THRESHOLD)
            .setDescription("The way to limit pitch");

    public static Setting<Float> pitchThreshold = new Setting<>("Threshold", 40f, 1f, 100f, 1f)
            .setDescription("The limit to the yaw step")
            .setParentSetting(pitchStep)
            .setVisibility(() -> pitchStep.getValue().equals(Step.THRESHOLD));

    public Rotations() {
        super("Rotations", Category.CLIENT, "Global rotation settings");

        INSTANCE = this;
    }

    @Listener
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (!rotationsQueue.isEmpty()) {
            event.cancel();
        }
    }

    @Listener
    public void onPacketSend(PacketEvent.PreSend event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            if (!rotationsQueue.isEmpty()) {
                rotationsQueue.sort(Comparator.comparing(rotation -> rotation.getPriority().getPriority()));

                rotationsQueue.removeIf(rotation -> rotation.getYaw() == mc.player.rotationYaw && rotation.getPitch() == mc.player.rotationPitch || rotation.getRotate().equals(Rotate.NONE));

                Rotation rotation = rotationsQueue.get(0);

                // Default to full rotation
                float finYaw = rotation.getYaw();
                float finPitch = rotation.getPitch();

                if (yawStep.getValue().equals(Step.THRESHOLD)) {
                    // YAW
                    float yawWanted = rotation.getYaw() - mc.player.rotationYaw;

                    if (Math.abs(yawWanted) > 180) {
                        yawWanted = RotationUtil.normalizeAngle(yawWanted);
                    }

                    int yawDirection = yawWanted > 0 ? 1 : -1;

                    if (Math.abs(yawWanted) > yawThreshold.getValue()) {
                        finYaw = RotationUtil.normalizeAngle(mc.player.rotationYaw + yawThreshold.getValue() * yawDirection);
                    } else {
                        finYaw = rotation.getYaw();
                    }
                }

                if (pitchStep.getValue().equals(Step.THRESHOLD)) {
                    // PITCH
                    float pitchWanted = rotation.getPitch() - mc.player.rotationPitch;

                    if (Math.abs(pitchWanted) > 180) {
                        pitchWanted = RotationUtil.normalizeAngle(pitchWanted);
                    }

                    int pitchDirection = pitchWanted > 0 ? 1 : -1;

                    if (Math.abs(pitchWanted) > pitchThreshold.getValue()) {
                        finPitch = RotationUtil.normalizeAngle(mc.player.rotationPitch + pitchThreshold.getValue() * pitchDirection);
                    } else {
                        finPitch = rotation.getPitch();
                    }
                }

                ((ICPacketPlayer) event.getPacket()).setYaw(finYaw);
                ((ICPacketPlayer) event.getPacket()).setPitch(finPitch);

                if (rotation.getRotate().equals(Rotate.LEGIT)) {
                    mc.player.rotationYaw = finYaw;
                    mc.player.rotationPitch = finPitch;
                }

                // Clear rotations queue when we have reached our final rotation
                if (finYaw == rotation.getYaw() && finPitch == rotation.getPitch()) {
                    rotationsQueue.clear();
                }
            }
        }
    }

    @Listener
    public void onUpdate(UpdateEvent event) {
        if (!rotationsQueue.isEmpty()) {
            // Send packet if we haven't cleared the queue yet
            mc.player.connection.sendPacket(new CPacketPlayer());
        }
    }

    public void addRotation(Rotation rotation) {
        this.rotationsQueue.add(rotation);
    }

    public enum Step {
        FULL,
        THRESHOLD
    }

}
