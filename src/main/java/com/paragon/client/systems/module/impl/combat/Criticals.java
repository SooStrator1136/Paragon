package com.paragon.client.systems.module.impl.combat;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;

/**
 * @author Wolfsurge
 */
public class Criticals extends Module {

    public Criticals() {
        super("Criticals", ModuleCategory.COMBAT, "Makes all your hits critical hits");
    }

    @Listener
    public void onPacketSend(PacketEvent.PreSend event) {
        // We are attacking an entity
        if (event.getPacket() instanceof CPacketUseEntity) {
            // Check the packets action
            if (((CPacketUseEntity) event.getPacket()).getAction().equals(CPacketUseEntity.Action.ATTACK)) {
                // Check the entity we are attacking is a living entity
                if (((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world) instanceof EntityLivingBase) {
                    // We are on the ground and we aren't jumping
                    if (mc.player.onGround && !mc.gameSettings.keyBindJump.isKeyDown()) {
                        // Send packets
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1, mc.player.posZ, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    }
                }
            }
        }
    }

}
