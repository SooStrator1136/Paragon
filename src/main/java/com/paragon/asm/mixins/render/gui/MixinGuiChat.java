package com.paragon.asm.mixins.render.gui;

import com.paragon.Paragon;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat {

    @Shadow protected abstract void setText(String newChatText, boolean shouldOverwrite);

    @Inject(method = "drawScreen", at = @At("HEAD"))
    public void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            this.setText(Paragon.INSTANCE.getCommandManager().getLastCommand(), true);
        }
    }

}
