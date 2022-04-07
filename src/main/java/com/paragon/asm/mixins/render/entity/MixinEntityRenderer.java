package com.paragon.asm.mixins.render.entity;

import com.google.common.base.Predicate;
import com.paragon.Paragon;
import com.paragon.api.event.player.RaytraceEvent;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient world, Entity entity, AxisAlignedBB axisAlignedBB, Predicate predicate) {
        RaytraceEvent raytraceEvent = new RaytraceEvent();
        Paragon.INSTANCE.getEventBus().post(raytraceEvent);

        if (raytraceEvent.isCancelled()) {
            return new ArrayList<>();
        } else {
            return world.getEntitiesInAABBexcluding(entity, axisAlignedBB, predicate);
        }
    }

}
