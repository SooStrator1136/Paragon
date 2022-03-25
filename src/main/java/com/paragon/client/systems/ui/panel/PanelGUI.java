package com.paragon.client.systems.ui.panel;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.ui.panel.impl.Panel;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.impl.client.ClientFont;
import com.paragon.client.systems.module.impl.client.GUI;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Wolfsurge
 */
public class PanelGUI extends GuiScreen implements TextRenderer {

    // List of panels
    private ArrayList<Panel> panels = new ArrayList<>();

    // The tooltip being rendered
    public static String tooltip = "";

    public PanelGUI() {
        // X position of panel
        float x = (RenderUtil.getScreenWidth() / 2) - ((ModuleCategory.values().length * 100) / 2f);

        // Add a panel for every category
        for (ModuleCategory category : ModuleCategory.values()) {
            // Add panel
            panels.add(new Panel(x, 30, 95, 16, category));

            // Increase X
            x += 100;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Reset tooltip
        tooltip = "";

        // Make the background darker
        if (GUI.darkenBackground.isEnabled()) {
            drawDefaultBackground();
        }

        // Render HUD modules
        Paragon.INSTANCE.getModuleManager().getHUDModules().forEach(hudModule -> {
            if (hudModule.isEnabled()) {
                hudModule.updateComponent(mouseX, mouseY);
                hudModule.render();
            }
        });

        scrollPanels();

        // Render panels
        panels.forEach(panel -> {
            panel.renderPanel(mouseX, mouseY);
        });

        Paragon.INSTANCE.getTaskbar().drawTaskbar(mouseX, mouseY);

        if (!tooltip.isEmpty() && GUI.tooltips.isEnabled()) {
            RenderUtil.drawRect(mouseX + 7, mouseY - 5, getStringWidth(tooltip) + 4, getFontHeight() + 2, 0x90000000);
            RenderUtil.drawBorder(mouseX + 7, mouseY - 5, getStringWidth(tooltip) + 4, getFontHeight() + 2, 0.5f, Colours.mainColour.getColour().getRGB());
            renderText(tooltip, mouseX + 9, mouseY - (ClientFont.INSTANCE.isEnabled() ? 2 : 4), -1);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Clicks
        panels.forEach(panel -> {
            panel.mouseClicked(mouseX, mouseY, mouseButton);
        });

        Paragon.INSTANCE.getModuleManager().getHUDModules().forEach(hudModule -> {
            if (hudModule.isEnabled()) {
                hudModule.mouseClicked(mouseX, mouseY, mouseButton);
            }
        });

        Paragon.INSTANCE.getTaskbar().mouseClicked(mouseX, mouseY);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        // Click releases
        panels.forEach(panel -> {
            panel.mouseReleased(mouseX, mouseY, state);
        });

        Paragon.INSTANCE.getModuleManager().getHUDModules().forEach(hudModule -> {
            if (hudModule.isEnabled()) {
                hudModule.mouseReleased(mouseX, mouseY, state);
            }
        });

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {

        // Keys typed
        panels.forEach(panel -> {
            panel.keyTyped(typedChar, keyCode);
        });

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        Paragon.INSTANCE.getModuleManager().getModules().forEach(module -> {
            Paragon.INSTANCE.getStorageManager().saveModuleConfiguration(module);
        });
    }

    public void scrollPanels() {
        int dWheel = Mouse.getDWheel();

        for (Panel panel : panels) {
            if (dWheel > 0) {
                panel.setY(panel.getY() - GUI.scrollSpeed.getValue());
            } else if (dWheel < 0) {
                panel.setY(panel.getY() + GUI.scrollSpeed.getValue());
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        // Pause the game if pause is enabled in the GUI settings
        return GUI.pause.isEnabled();
    }

    /**
     * Gets the panels
     * @return The panels
     */
    public ArrayList<Panel> getPanels() {
        return panels;
    }
}
