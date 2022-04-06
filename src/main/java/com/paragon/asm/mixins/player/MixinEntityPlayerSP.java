package com.paragon.asm.mixins.player;

import com.paragon.Paragon;
import com.paragon.api.event.player.PlayerMotionEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP extends AbstractClientPlayer {

    public MixinEntityPlayerSP() {
        super(null, null);
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void move(AbstractClientPlayer instance, MoverType moverType, double x, double y, double z) {
        PlayerMotionEvent playerMotionEvent = new PlayerMotionEvent(moverType, x, y, z);
        Paragon.INSTANCE.getEventBus().post(playerMotionEvent);
        super.move(moverType, playerMotionEvent.getX(), playerMotionEvent.getY(), playerMotionEvent.getZ());
    }

}
