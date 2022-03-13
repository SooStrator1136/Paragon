package com.paragon.asm.mixins.render.entity;

import com.paragon.Paragon;
import com.paragon.api.event.player.RenderItemEvent;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(method = "transformFirstPerson", at = @At("HEAD"))
    public void onTransformPre(EnumHandSide hand, float p_187453_2_, CallbackInfo ci) {
        RenderItemEvent renderItemEvent = new RenderItemEvent.Pre(hand);
        Paragon.INSTANCE.getEventBus().post(renderItemEvent);
    }

    @Inject(method = "transformFirstPerson", at = @At("TAIL"))
    public void onTransformPost(EnumHandSide hand, float p_187453_2_, CallbackInfo ci) {
        RenderItemEvent renderItemEvent = new RenderItemEvent.Post(hand);
        Paragon.INSTANCE.getEventBus().post(renderItemEvent);
    }

    @Inject(method = "transformSideFirstPerson", at = @At("HEAD"))
    public void onTransformSide(EnumHandSide hand, float p_187453_2_, CallbackInfo ci) {
        RenderItemEvent renderItemEvent = new RenderItemEvent.Pre(hand);
        Paragon.INSTANCE.getEventBus().post(renderItemEvent);
    }

}
