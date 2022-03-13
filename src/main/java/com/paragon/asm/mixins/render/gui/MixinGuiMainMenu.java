package com.paragon.asm.mixins.render.gui;

import com.paragon.Paragon;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiMainMenu.class, priority = Integer.MAX_VALUE)
public class MixinGuiMainMenu {

    @Inject(method = "drawScreen", at = @At("TAIL"))
    public void renderWatermark(int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("Paragon " + TextFormatting.GRAY + Paragon.modVersion, 2, sr.getScaledHeight() - 50, Colours.mainColour.getColour().getRGB());
    }

}
