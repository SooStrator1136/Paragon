package com.paragon.api.event.network;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.network.Packet;

public class PacketEvent extends CancellableEvent {

    private Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public static class PreReceive extends PacketEvent {
        public PreReceive(Packet<?> packet) {
            super(packet);
        }
    }

    public static class PreSend extends PacketEvent {
        public PreSend(Packet<?> packet) {
            super(packet);
        }
    }

    public static class PostReceive extends PacketEvent {
        public PostReceive(Packet<?> packet) {
            super(packet);
        }
    }

    public static class PostSend extends PacketEvent {
        public PostSend(Packet<?> packet) {
            super(packet);
        }
    }

}
