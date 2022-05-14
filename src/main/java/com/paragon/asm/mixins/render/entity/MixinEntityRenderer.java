package com.paragon.asm.mixins.render.entity;

import com.google.common.base.Predicate;
import com.paragon.Paragon;
import com.paragon.api.event.player.RaytraceEntityEvent;
import com.paragon.api.event.render.entity.CameraClipEvent;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient world, Entity entity, AxisAlignedBB axisAlignedBB, Predicate<? super Entity> predicate) {
        RaytraceEntityEvent raytraceEvent = new RaytraceEntityEvent();
        Paragon.INSTANCE.getEventBus().post(raytraceEvent);

        if (raytraceEvent.isCancelled()) {
            return new ArrayList<>();
        } else {
            return world.getEntitiesInAABBexcluding(entity, axisAlignedBB, predicate);
        }
    }

    @ModifyVariable(method = "orientCamera", at = @At("STORE"), ordinal = 3)
    public double orientCameraX(double distance) {
        CameraClipEvent cameraClipEvent = new CameraClipEvent(distance);
        Paragon.INSTANCE.getEventBus().post(cameraClipEvent);

        if (cameraClipEvent.isCancelled()) {
            return cameraClipEvent.getDistance();
        }

        return distance;
    }

    @ModifyVariable(method = "orientCamera", at = @At("STORE"), ordinal = 7)
    public double orientCameraZ(double distance) {
        CameraClipEvent cameraClipEvent = new CameraClipEvent(distance);
        Paragon.INSTANCE.getEventBus().post(cameraClipEvent);

        if (cameraClipEvent.isCancelled()) {
            return cameraClipEvent.getDistance();
        }

        return distance;
    }

}
