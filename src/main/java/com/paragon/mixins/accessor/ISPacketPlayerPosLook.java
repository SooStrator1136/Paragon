package com.paragon.mixins.accessor;

import net.minecraft.network.play.server.SPacketPlayerPosLook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketPlayerPosLook.class)
public interface ISPacketPlayerPosLook {

    @Accessor("yaw")
    void setYaw(float newYaw);

    @Accessor("pitch")
    void setPitch(float newPitch);

}
