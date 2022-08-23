package com.paragon.api.event.network

import com.paragon.bus.event.CancellableEvent
import net.minecraft.network.Packet

/**
 * @author Surge
 */
open class PacketEvent(val packet: Packet<*>) : CancellableEvent() {

    class PreReceive(packet: Packet<*>) : PacketEvent(packet)
    class PreSend(packet: Packet<*>) : PacketEvent(packet)
    class PostReceive(packet: Packet<*>) : PacketEvent(packet)
    class PostSend(packet: Packet<*>) : PacketEvent(packet)

}