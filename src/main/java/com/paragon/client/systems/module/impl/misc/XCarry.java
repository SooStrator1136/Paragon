package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.asm.mixins.accessor.ICPacketCloseWindow;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.client.CPacketCloseWindow;

/**
 * @author Wolfsurge
 */
public class XCarry extends Module {

    public XCarry() {
        super("XCarry", Category.MISC, "Lets you carry items in your crafting grid");
    }

    @Listener
    public void onPacketSent(PacketEvent.PreSend event) {
        if (event.getPacket() instanceof CPacketCloseWindow) {
            if (((ICPacketCloseWindow) event.getPacket()).getID() == mc.player.inventoryContainer.windowId) {
                event.cancel();
            }
        }
    }

}
