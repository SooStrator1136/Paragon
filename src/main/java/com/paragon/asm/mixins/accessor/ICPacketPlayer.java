package com.paragon.asm.mixins.accessor;

import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketPlayer.class)
public interface ICPacketPlayer {

    @Accessor("yaw")
    void setYaw(float yaw);

    @Accessor("yaw")
    float getYaw();

    @Accessor("pitch")
    void setPitch(float pitch);

    @Accessor("pitch")
    float getPitch();

    @Accessor("y")
    void setY(double y);

    @Accessor("onGround")
    void setOnGround(boolean onGround);

}
