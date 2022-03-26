package com.paragon.asm.mixins.player;

import com.paragon.Paragon;
import com.paragon.api.event.player.PlayerMotionEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP extends AbstractClientPlayer {

    public MixinEntityPlayerSP() {
        super(null, null);
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void onMove(MoverType type, double x, double y, double z, CallbackInfo ci) {
        PlayerMotionEvent playerMotionEvent = new PlayerMotionEvent(type, x, y, z);
        Paragon.INSTANCE.getEventBus().post(playerMotionEvent);

        if (playerMotionEvent.isCancelled()) {
            ci.cancel();

            super.move(type, playerMotionEvent.getX(), playerMotionEvent.getY(), playerMotionEvent.getZ());
        }
    }

}
