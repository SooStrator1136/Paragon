package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.asm.mixins.accessor.ISPacketPlayerPosLook;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

/**
 * @author Surge
 */
public class NoRotate extends Module {

    public NoRotate() {
        super("NoRotate", Category.MISC, "Stops the server from rotating your head");
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (nullCheck() || event.getPacket() == null) {
            return;
        }

        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            // Set packet yaw
            ((ISPacketPlayerPosLook) event.getPacket()).setYaw(mc.player.rotationYaw);

            // Set packet pitch
            ((ISPacketPlayerPosLook) event.getPacket()).setPitch(mc.player.rotationPitch);
        }
    }

}
