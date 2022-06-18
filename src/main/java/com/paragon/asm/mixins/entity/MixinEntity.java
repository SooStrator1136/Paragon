package com.paragon.asm.mixins.entity;

import com.paragon.Paragon;
import com.paragon.api.event.player.StepEvent;
import com.paragon.api.event.world.entity.EntityPushEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow public abstract AxisAlignedBB getEntityBoundingBox();

    @Shadow public float stepHeight;

    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", target="Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void onEntityCollision(Entity entity, double x, double y, double z) {
        EntityPushEvent event = new EntityPushEvent(entity);
        Paragon.INSTANCE.getEventBus().post(event);

        if (!event.isCancelled()) {
            entity.motionX += x;
            entity.motionY += y;
            entity.motionZ += z;
        }
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", shift = At.Shift.BEFORE, ordinal = 0))
    public void onMove(MoverType type, double x, double y, double z, CallbackInfo ci) {
        StepEvent stepEvent = new StepEvent(getEntityBoundingBox(), (Entity) (Object) this, stepHeight);
        Paragon.INSTANCE.getEventBus().post(stepEvent);

        if (stepEvent.isCancelled()) {
            stepHeight = stepEvent.getHeight();
        }
    }

}
