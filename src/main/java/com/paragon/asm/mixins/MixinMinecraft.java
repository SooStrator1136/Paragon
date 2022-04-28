package com.paragon.asm.mixins;

import com.paragon.client.systems.ui.menu.ParagonMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
    public void onDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
        if (guiScreenIn instanceof GuiMainMenu) {
            Minecraft.getMinecraft().displayGuiScreen(new ParagonMenu());
            ci.cancel();
        }
    }

}
