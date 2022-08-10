package com.paragon.client.ui.configuration.old;

import com.paragon.Paragon;
import com.paragon.api.module.Category;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.font.FontUtil;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.client.systems.module.impl.client.ClientFont;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.ui.configuration.GuiImplementation;
import com.paragon.client.ui.configuration.old.impl.Panel;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Wolfsurge
 */
public final class OldPanelGUI extends GuiImplementation {

    public static OldPanelGUI INSTANCE = new OldPanelGUI();

    // The tooltip being rendered
    public static String tooltip = "";
    // List of panels
    private final ArrayList<Panel> panels = new ArrayList<>();

    private OldPanelGUI() {
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
    public void drawScreen(int mouseX, int mouseY, int mouseDelta) {
        // Reset tooltip
        tooltip = "";

        // Make the background darker
        if (ClickGUI.getDarkenBackground().getValue()) {
            drawDefaultBackground();
        }

        for (Panel panel : panels) {
            panel.setY(panel.getY() + (mouseDelta / 100f) * ClickGUI.getScrollSpeed().getValue());
        }

        // Render panels
        panels.forEach(panel -> {
            panel.renderPanel(mouseX, mouseY);
        });

        if (ClickGUI.getTooltips().getValue() && !Objects.equals(tooltip, "")) {
            RenderUtil.drawRect(mouseX + 7, mouseY - 5, FontUtil.getStringWidth(tooltip) + 4, FontUtil.getHeight() + 2, 0x90000000);
            RenderUtil.drawBorder(mouseX + 7, mouseY - 5, FontUtil.getStringWidth(tooltip) + 4, FontUtil.getHeight() + 2, 0.5f, Colours.mainColour.getValue().getRGB());
            FontUtil.drawStringWithShadow(tooltip, mouseX + 9, mouseY - (ClientFont.INSTANCE.isEnabled() ? 2 : 4), -1);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // Clicks
        panels.forEach(panel -> {
            panel.mouseClicked(mouseX, mouseY, mouseButton);
        });
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        // Click releases
        panels.forEach(panel -> {
            panel.mouseReleased(mouseX, mouseY, state);
        });
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // Keys typed
        panels.forEach(panel -> {
            panel.keyTyped(typedChar, keyCode);
        });
    }

    @Override
    public void onGuiClosed() {
        Paragon.INSTANCE.getStorageManager().saveModules("current");
    }

    public void scrollPanels() {
        int dWheel = Mouse.getDWheel();

        for (Panel panel : panels) {
            panel.setY(panel.getY() + (dWheel / 100f) * ClickGUI.getScrollSpeed().getValue());
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        // Pause the game if pause is enabled in the GUI settings
        return ClickGUI.getPause().getValue();
    }

    /**
     * Gets the panels
     *
     * @return The panels
     */
    public ArrayList<Panel> getPanels() {
        return panels;
    }

    public static boolean isInside(float x, float y, float x2, float y2, int checkX, int checkY) {
        return checkX >= x && checkX <= x2 && checkY > y && checkY <= y2;
    }

}
