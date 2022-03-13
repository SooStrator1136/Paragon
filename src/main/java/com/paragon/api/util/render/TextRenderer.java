package com.paragon.api.util.render;

import com.paragon.client.systems.module.impl.client.ClientFont;
import com.paragon.font.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.io.InputStream;

public interface TextRenderer {

    FontRenderer custom = new FontRenderer(getFont("mono", 40));

    default void renderText(String text, float x, float y, int colour) {
        if (ClientFont.INSTANCE.isEnabled()) {
            custom.drawStringWithShadow(text, x, y - 3.5f, colour);
            return;
        }

        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, colour);
    }

    default void renderCenteredString(String text, float x, float y, int colour, boolean centeredY) {
        if (ClientFont.INSTANCE.isEnabled()) {
            if (centeredY) {
                y -= custom.getHeight() / 2f;
            }

            custom.drawStringWithShadow(text, (x - custom.getStringWidth(text) / 2f), y - 1, colour);
            return;
        }

        if (centeredY) {
            y -= Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2f;
        }
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, (x - Minecraft.getMinecraft().fontRenderer.getStringWidth(text) / 2f), y, colour);
    }

    default float getStringWidth(String text) {
        if (ClientFont.INSTANCE.isEnabled()) {
            return custom.getStringWidth(text);
        }

        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

    default float getFontHeight() {
        if (ClientFont.INSTANCE.isEnabled()) {
            return custom.getHeight();
        }

        return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
    }

    default String formatCode(TextFormatting textFormatting) {
        return "§" + textFormatting.getColorIndex();
    }

    static Font getFont(String fontName, float size) {
        try {
            InputStream inputStream = TextRenderer.class.getResourceAsStream("/assets/paragon/font/" + fontName + ".ttf");
            Font awtClientFont = Font.createFont(0, inputStream);
            inputStream.close();

            return awtClientFont.deriveFont(Font.PLAIN, size);
        } catch (Exception exception) {
            exception.printStackTrace();
            return new Font("default", Font.PLAIN, (int) size);
        }
    }

}
