package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.event.render.entity.SwingArmEvent;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.server.SPacketAnimation;

/**
 * @author Wolfsurge
 */
public class NoSwing extends Module {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET_CANCEL)
            .setDescription("How to not swing");

    private final Setting<Boolean> others = new Setting<>("Others", true)
            .setDescription("Whether to cancel other players' animations");

    public NoSwing() {
        super("NoSwing", Category.RENDER, "Cancels the swing animation");
        this.addSettings(mode, others);
    }

    @Listener
    public void onPacketSend(PacketEvent.PreSend event) {
        if (mode.getValue().equals(Mode.PACKET_CANCEL) && event.getPacket() instanceof CPacketAnimation) {
            event.cancel();
        }
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (others.getValue() && event.getPacket() instanceof SPacketAnimation) {
            event.cancel();
        }
    }

    @Listener
    public void onSwingArm(SwingArmEvent event) {
        if (mode.getValue().equals(Mode.METHOD_CANCEL)) {
            event.cancel();
        }
    }

    public enum Mode {
        /**
         * Cancels the swing animation packet
         */
        PACKET_CANCEL,

        /**
         * Cancels the swing method from invoking
         */
        METHOD_CANCEL
    }

}
