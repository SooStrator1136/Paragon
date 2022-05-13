package com.paragon.api.util.render;

import com.paragon.Paragon;
import com.paragon.client.systems.module.impl.client.ClientFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;

public interface TextRenderer {

    default void renderText(String text, float x, float y, int colour) {
        if (ClientFont.INSTANCE.isEnabled()) {
            Paragon.INSTANCE.getFontManager().getFontRenderer().drawStringWithShadow(text, x, (y - 3.5f) + Paragon.INSTANCE.getFontManager().getYIncrease(), colour);
            return;
        }

        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, colour);
    }

    default void renderCenteredString(String text, float x, float y, int colour, boolean centeredY) {
        if (ClientFont.INSTANCE.isEnabled()) {
            if (centeredY) {
                y -= Paragon.INSTANCE.getFontManager().getFontRenderer().getHeight() / 2f;
            }

            Paragon.INSTANCE.getFontManager().getFontRenderer().drawStringWithShadow(text, (x - Paragon.INSTANCE.getFontManager().getFontRenderer().getStringWidth(text) / 2f), (y - 1) + Paragon.INSTANCE.getFontManager().getYIncrease(), colour);
            return;
        }

        if (centeredY) {
            y -= Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2f;
        }

        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, (x - Minecraft.getMinecraft().fontRenderer.getStringWidth(text) / 2f), y, colour);
    }

    default float getStringWidth(String text) {
        if (ClientFont.INSTANCE.isEnabled()) {
            return Paragon.INSTANCE.getFontManager().getFontRenderer().getStringWidth(text);
        }

        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

    default float getFontHeight() {
        if (ClientFont.INSTANCE.isEnabled()) {
            return Paragon.INSTANCE.getFontManager().getFontRenderer().getHeight();
        }

        return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
    }

    default FontRenderer getMCFontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }

    default String formatCode(TextFormatting textFormatting) {
        return "§" + textFormatting.getColorIndex();
    }

}
