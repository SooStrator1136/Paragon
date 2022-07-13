package com.paragon.api.event.network

import me.wolfsurge.cerauno.event.CancellableEvent
import net.minecraft.network.Packet

/**
 * @author Wolfsurge
 */
open class PacketEvent(val packet: Packet<*>) : CancellableEvent() {

    class PreReceive(packet: Packet<*>) : PacketEvent(packet)
    class PreSend(packet: Packet<*>) : PacketEvent(packet)
    class PostReceive(packet: Packet<*>) : PacketEvent(packet)
    class PostSend(packet: Packet<*>) : PacketEvent(packet)

}