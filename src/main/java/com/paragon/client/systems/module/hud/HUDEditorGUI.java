package com.paragon.client.systems.module.hud;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.io.IOException;

public class HUDEditorGUI extends GuiScreen {

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        ScaledResolution scaledResolution = new ScaledResolution(mc);

        RenderUtil.drawRect((scaledResolution.getScaledWidth() / 2f) - 0.5f, 0, 1, scaledResolution.getScaledHeight(), new Color(255, 255, 255, 100).getRGB());
        RenderUtil.drawRect(0, (scaledResolution.getScaledHeight() / 2f) - 0.5f, scaledResolution.getScaledWidth(), 1, new Color(255, 255, 255, 100).getRGB());

        Paragon.INSTANCE.getModuleManager().getHUDModules().forEach(hudModule -> {
            if (hudModule.isEnabled()) {
                hudModule.updateComponent(mouseX, mouseY);
                hudModule.render();
            }
        });

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        Paragon.INSTANCE.getModuleManager().getHUDModules().forEach(hudModule -> {
            if (hudModule.isEnabled()) {
                hudModule.mouseClicked(mouseX, mouseY, mouseButton);
            }
        });

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        Paragon.INSTANCE.getModuleManager().getHUDModules().forEach(hudModule -> {
            if (hudModule.isEnabled()) {
                hudModule.mouseReleased(mouseX, mouseY, state);
            }
        });

        super.mouseReleased(mouseX, mouseY, state);
    }
}
