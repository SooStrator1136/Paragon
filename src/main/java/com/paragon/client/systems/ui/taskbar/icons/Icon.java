package com.paragon.client.systems.ui.taskbar.icons;

import com.paragon.api.util.Wrapper;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.impl.client.ClientFont;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.function.Supplier;

public class Icon implements Wrapper, TextRenderer {

    private String name;
    private int x, y;
    private Supplier<GuiScreen> guiScreenSupplier;

    public Icon(String name, int x, Supplier<GuiScreen> whenClicked) {
        this.name = name;
        this.x = x;
        this.guiScreenSupplier = whenClicked;
    }

    public void draw(int mouseX, int mouseY) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        this.y = scaledResolution.getScaledHeight() - 19;

        ColourUtil.setColour(-1);

        RenderUtil.drawRect(x, y, getStringWidth(name) + 6, 16, new Color(17, 17, 17).getRGB());

        if (isHovered(x, y, getStringWidth(name) + 6, 16, mouseX, mouseY)) {
            RenderUtil.drawRect(x, y, getStringWidth(name) + 6, 16, new Color(23, 23, 23).getRGB());
        }

        renderCenteredString(name, x + ((getStringWidth(name) + 6) / 2), y + (ClientFont.INSTANCE.isEnabled() ? 2 : 4), -1, false);
    }

    public void whenClicked(int mouseX, int mouseY) {
        if (isHovered(x, y, getStringWidth(name) + 6, 16, mouseX, mouseY)) {
            mc.displayGuiScreen(guiScreenSupplier.get());
        }
    }

}
