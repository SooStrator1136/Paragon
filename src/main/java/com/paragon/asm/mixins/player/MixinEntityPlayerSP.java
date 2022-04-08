package com.paragon.asm.mixins.player;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP extends AbstractClientPlayer {

    public MixinEntityPlayerSP() {
        super(null, null);
    }

}
