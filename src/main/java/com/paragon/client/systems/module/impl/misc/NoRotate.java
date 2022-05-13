package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.asm.mixins.accessor.ISPacketPlayerPosLook;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

/**
 * @author Wolfsurge
 */
public class NoRotate extends Module {

    public NoRotate() {
        super("NoRotate", Category.MISC, "Stops the server from rotating your head");
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            // Set packet yaw
            ((ISPacketPlayerPosLook) event.getPacket()).setYaw(mc.player.rotationYaw);

            // Set packet pitch
            ((ISPacketPlayerPosLook) event.getPacket()).setPitch(mc.player.rotationPitch);
        }
    }

}
