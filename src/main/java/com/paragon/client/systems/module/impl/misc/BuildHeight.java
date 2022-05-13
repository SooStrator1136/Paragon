package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;

/**
 * @author Wolfsurge
 */
public class BuildHeight extends Module {

    public BuildHeight() {
        super("BuildHeight", Category.MISC, "Lets you interact with blocks at the maximum build height");
    }

    @Listener
    public void onPacketSent(PacketEvent.PreSend event) {
        // Check packet is a player try use item on block packet
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            // Get packet
            CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) event.getPacket();

            // Check the position we are trying to place at is 255 or above, and we are placing on top of a block
            if (packet.getPos().getY() >= 255 && packet.getDirection().equals(EnumFacing.UP)) {

                // Send new packet with the place direction being down
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(packet.getPos(), EnumFacing.DOWN, packet.getHand(), packet.getFacingX(), packet.getFacingY(), packet.getFacingZ()));

                // Don't send original packet
                event.cancel();
            }
        }
    }

}
