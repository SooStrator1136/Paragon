package com.paragon.mixins.render.entity;

import com.paragon.Paragon;
import com.paragon.api.event.player.RenderItemEvent;
import com.paragon.api.event.render.entity.RenderEatingEvent;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(method = "transformEatFirstPerson", at = @At("HEAD"), cancellable = true)
    public void onTransformEat(float a, EnumHandSide side, ItemStack stack, CallbackInfo ci) {
        RenderEatingEvent renderEatingEvent = new RenderEatingEvent();
        Paragon.INSTANCE.getEventBus().post(renderEatingEvent);

        if (renderEatingEvent.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "transformFirstPerson", at = @At("HEAD"))
    public void onTransformPre(EnumHandSide hand, float p_187453_2_, CallbackInfo ci) {
        RenderItemEvent.Pre renderItemEvent = new RenderItemEvent.Pre(hand);
        Paragon.INSTANCE.getEventBus().post(renderItemEvent);
    }

    @Inject(method = "transformFirstPerson", at = @At("TAIL"))
    public void onTransformPost(EnumHandSide hand, float p_187453_2_, CallbackInfo ci) {
        RenderItemEvent.Post renderItemEvent = new RenderItemEvent.Post(hand);
        Paragon.INSTANCE.getEventBus().post(renderItemEvent);
    }

    @Inject(method = "transformSideFirstPerson", at = @At("HEAD"))
    public void onTransformSide(EnumHandSide hand, float p_187453_2_, CallbackInfo ci) {
        RenderItemEvent.Pre renderItemEvent = new RenderItemEvent.Pre(hand);
        Paragon.INSTANCE.getEventBus().post(renderItemEvent);
    }

}
