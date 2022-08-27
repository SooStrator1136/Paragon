package com.paragon.client.systems.module.impl.misc

import com.paragon.api.event.network.PacketEvent.PreSend
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.mixins.accessor.ICPacketCloseWindow
import com.paragon.bus.listener.Listener
import net.minecraft.network.play.client.CPacketCloseWindow

/**
 * @author Surge
 */
object XCarry : Module("XCarry", Category.MISC, "Lets you carry items in your crafting grid") {

    @Listener
    fun onPacketSent(event: PreSend) {
        if (event.packet is CPacketCloseWindow && (event.packet as ICPacketCloseWindow).id == minecraft.player.inventoryContainer.windowId) {
            event.cancel()
        }
    }

}