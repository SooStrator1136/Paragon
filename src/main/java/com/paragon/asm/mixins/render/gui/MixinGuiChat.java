package com.paragon.asm.mixins.render.gui;

import com.paragon.Paragon;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat {

    @Shadow
    protected GuiTextField inputField;

    @Inject(method = "drawScreen", at = @At("HEAD"))
    public void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            this.inputField.setText(Paragon.INSTANCE.getCommandManager().getLastCommand());
        }
    }

}
