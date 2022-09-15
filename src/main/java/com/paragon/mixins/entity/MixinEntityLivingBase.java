package com.paragon.mixins.entity;

import com.paragon.Paragon;
import com.paragon.impl.event.render.entity.SwingArmEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public class MixinEntityLivingBase {

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    public void hookSwingArm(EnumHand hand, CallbackInfo ci) {
        SwingArmEvent event = new SwingArmEvent();
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}
