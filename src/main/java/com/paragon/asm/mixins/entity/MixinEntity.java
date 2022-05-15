package com.paragon.asm.mixins.entity;

import com.paragon.Paragon;
import com.paragon.api.event.world.entity.EntityPushEvent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow protected abstract void entityInit();

    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", target="Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void onEntityCollision(Entity entity, double x, double y, double z) {
        EntityPushEvent event = new EntityPushEvent(entity);
        Paragon.INSTANCE.getEventBus().post(event);

        if (!event.isCancelled()) {
            entity.motionX = 0;
            entity.motionY = 0;
            entity.motionZ = 0;
        }
    }

}
