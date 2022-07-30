package com.paragon.api.util.render;

import com.paragon.Paragon;
import com.paragon.client.systems.module.impl.client.ClientFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;

public interface ITextRenderer {

    default void renderText(String text, float x, float y, int colour) {
        if (ClientFont.INSTANCE.isEnabled()) {
            Paragon.INSTANCE.getFontManager().getFontRenderer().drawStringWithShadow(text, x, y - (3f) + Paragon.INSTANCE.getFontManager().getYIncrease(), colour);
            return;
        }

        if (text.contains(System.lineSeparator())) {
            String[] parts = text.split(System.lineSeparator());
            float newY = 0.0f;

            for (String s : parts) {
                Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(s, x, y + newY, colour);
                newY += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
            }

            return;
        }

        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, colour);
    }

    default void renderCenteredString(String text, float x, float y, int colour, boolean centeredY) {
        if (ClientFont.INSTANCE.isEnabled()) {
            if (centeredY) {
                y -= Paragon.INSTANCE.getFontManager().getFontRenderer().getHeight() / 2f;
            }

            if (text.contains("\n")) {
                String[] parts = text.split("\n");

                float newY = 0.0f;
                for (String s : parts) {
                    Paragon.INSTANCE.getFontManager().getFontRenderer().drawStringWithShadow(s, x - Paragon.INSTANCE.getFontManager().getFontRenderer().getStringWidth(s) / 2f, (y - 3.5f) + Paragon.INSTANCE.getFontManager().getYIncrease() + newY, colour);
                    newY += Paragon.INSTANCE.getFontManager().getFontRenderer().getHeight();
                }

                return;
            }

            Paragon.INSTANCE.getFontManager().getFontRenderer().drawStringWithShadow(text, x - (getStringWidth(text) / 2f), (y - 3f) + Paragon.INSTANCE.getFontManager().getYIncrease(), colour);
            return;
        }

        if (centeredY) {
            y -= Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2f;
        }

        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, (x - Minecraft.getMinecraft().fontRenderer.getStringWidth(text) / 2f), y, colour);
    }

    default float getStringWidth(String text) {
        if (text.contains("\n")) {
            String[] parts = text.split("\n");
            float width = 0;

            for (String s : parts) {
                width = Math.max(width, getStringWidth(s));
            }

            return width;
        }

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
