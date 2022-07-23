package com.paragon.asm.mixins.render.entity;

import com.paragon.Paragon;
import com.paragon.api.event.render.entity.RenderNametagEvent;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {

    @Inject(method = "renderEntityName(Lnet/minecraft/entity/Entity;DDDLjava/lang/String;D)V", at = @At("HEAD"), cancellable = true)
    public void onRenderEntityName(Entity par1, double par2, double par3, double par4, String par5, double par6, CallbackInfo ci) {
        RenderNametagEvent renderNametagEvent = new RenderNametagEvent(par1);
        Paragon.INSTANCE.getEventBus().post(renderNametagEvent);

        if (renderNametagEvent.isCancelled()) {
            ci.cancel();
        }
    }

}
