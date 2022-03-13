package com.paragon.asm.mixins.network;

import com.paragon.Paragon;
import com.paragon.api.event.network.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void onPacketReceived(ChannelHandlerContext p_channelRead0_1_, Packet<?> p_channelRead0_2_, CallbackInfo ci) {
        PacketEvent.PreReceive preReceive = new PacketEvent.PreReceive(p_channelRead0_2_);
        Paragon.INSTANCE.getEventBus().post(preReceive);

        if (preReceive.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void onPacketSendPre(Packet<?> packetIn, CallbackInfo ci) {
        PacketEvent.PreSend preSend = new PacketEvent.PreSend(packetIn);
        Paragon.INSTANCE.getEventBus().post(preSend);

        if (preSend.isCancelled()) {
            ci.cancel();
        }
    }

}
