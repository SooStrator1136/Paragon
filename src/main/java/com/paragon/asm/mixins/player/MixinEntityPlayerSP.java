package com.paragon.asm.mixins.player;

import com.paragon.Paragon;
import com.paragon.api.event.player.MotionEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP extends AbstractClientPlayer {

    public MixinEntityPlayerSP() {
        super(null, null);
    }

}
