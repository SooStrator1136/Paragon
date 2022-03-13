package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

/**
 * @author Wolfsurge
 */
public class NoRotate extends Module {

    public NoRotate() {
        super("NoRotate", ModuleCategory.MISC, "Stops the server from rotating your head");
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            // Set packet yaw
            ((SPacketPlayerPosLook) event.getPacket()).yaw = mc.player.rotationYaw;

            // Set packet pitch
            ((SPacketPlayerPosLook) event.getPacket()).pitch = mc.player.rotationPitch;
        }
    }

}
