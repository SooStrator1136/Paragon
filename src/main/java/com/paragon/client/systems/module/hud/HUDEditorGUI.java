package com.paragon.client.systems.module.hud;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HUDEditorGUI extends GuiScreen {

    private boolean draggingComponent;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        ScaledResolution scaledResolution = new ScaledResolution(mc);

        RenderUtil.drawRect((scaledResolution.getScaledWidth() / 2f) - 0.5f, 0, 1, scaledResolution.getScaledHeight(), new Color(255, 255, 255, 100).getRGB());
        RenderUtil.drawRect(0, (scaledResolution.getScaledHeight() / 2f) - 0.5f, scaledResolution.getScaledWidth(), 1, new Color(255, 255, 255, 100).getRGB());

        Paragon.INSTANCE.getModuleManager().getModulesThroughPredicate(module -> module instanceof HUDModule).forEach(hudModule -> {
            if (hudModule.isEnabled()) {
                ((HUDModule) hudModule).updateComponent(mouseX, mouseY);
                ((HUDModule) hudModule).render();
            }
        });

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ArrayUtils.reverse(Paragon.INSTANCE.getModuleManager().getModules());

        final AtomicBoolean stopLoop = new AtomicBoolean(false); //Replace with a simple return@run when making this kt
        Paragon.INSTANCE.getModuleManager().getModulesThroughPredicate(module -> module instanceof HUDModule).forEach(hudModule -> {
            if (stopLoop.get()) {
                return;
            }
            if (hudModule.isEnabled() && !draggingComponent) {
                if (((HUDModule) hudModule).mouseClicked(mouseX, mouseY, mouseButton)) {
                    stopLoop.set(true);
                    return;
                }

                if (((HUDModule) hudModule).isDragging()) {
                    draggingComponent = true;
                }
            }
        });

        ArrayUtils.reverse(Paragon.INSTANCE.getModuleManager().getModules());

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        draggingComponent = false;

        Paragon.INSTANCE.getModuleManager().getModulesThroughPredicate(module -> module instanceof HUDModule).forEach(hudModule -> {
            if (hudModule.isEnabled()) {
                ((HUDModule) hudModule).mouseReleased(mouseX, mouseY, state);
            }
        });

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onGuiClosed() {
        this.draggingComponent = false;

        Paragon.INSTANCE.getModuleManager().getModulesThroughPredicate(module -> module instanceof HUDModule).forEach(hudModule -> {
            if (hudModule.isEnabled()) {
                ((HUDModule) hudModule).mouseReleased(0, 0, 0);
            }
        });
    }
}
