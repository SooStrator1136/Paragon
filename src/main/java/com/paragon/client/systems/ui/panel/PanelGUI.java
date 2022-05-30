package com.paragon.client.systems.ui.panel;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.ui.panel.impl.Panel;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.impl.client.ClientFont;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Wolfsurge
 */
public class PanelGUI extends GuiScreen implements TextRenderer {

    // The tooltip being rendered
    public static String tooltip = "";
    // List of panels
    private final ArrayList<Panel> panels = new ArrayList<>();

    public PanelGUI() {
        // X position of panel
        float x = (RenderUtil.getScreenWidth() / 2) - ((Category.values().length * 100) / 2f);

        // Add a panel for every category
        for (Category category : Category.values()) {
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
        if (ClickGUI.darkenBackground.getValue()) {
            drawDefaultBackground();
        }

        scrollPanels();

        // Render panels
        panels.forEach(panel -> {
            panel.renderPanel(mouseX, mouseY);
        });

        Paragon.INSTANCE.getTaskbar().drawTaskbar(mouseX, mouseY);

        if (ClickGUI.tooltips.getValue() && !Objects.equals(tooltip, "")) {
            RenderUtil.drawRect(mouseX + 7, mouseY - 5, getStringWidth(tooltip) + 4, getFontHeight() + 2, 0x90000000);
            RenderUtil.drawBorder(mouseX + 7, mouseY - 5, getStringWidth(tooltip) + 4, getFontHeight() + 2, 0.5f, Colours.mainColour.getValue().getRGB());
            renderText(tooltip, mouseX + 9, mouseY - (ClientFont.INSTANCE.isEnabled() ? 2 : 4), -1);
        }

        if (ClickGUI.catgirl.getValue()) {
            ScaledResolution sr = new ScaledResolution(mc);

            mc.getTextureManager().bindTexture(new ResourceLocation("paragon", "textures/ew.png"));
            RenderUtil.drawModalRectWithCustomSizedTexture(0, sr.getScaledHeight() - 145, 0, 0, 100, 167.777777778f, 100, 167.777777778f);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Clicks
        panels.forEach(panel -> {
            panel.mouseClicked(mouseX, mouseY, mouseButton);
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
        Paragon.INSTANCE.getStorageManager().saveModules("current");
    }

    public void scrollPanels() {
        int dWheel = Mouse.getDWheel();

        for (Panel panel : panels) {
            panel.setY(panel.getY() + (dWheel / 100f) * ClickGUI.scrollSpeed.getValue());
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        // Pause the game if pause is enabled in the GUI settings
        return ClickGUI.pause.getValue();
    }

    /**
     * Gets the panels
     *
     * @return The panels
     */
    public ArrayList<Panel> getPanels() {
        return panels;
    }
}
