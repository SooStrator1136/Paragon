package com.paragon.client.systems.ui.window;

import com.paragon.Paragon;
import com.paragon.client.systems.ui.window.components.Window;
import com.paragon.client.systems.ui.window.components.impl.CategoryComponent;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import java.io.IOException;

/**
 * @author Wolfsurge
 * @since 29/01/22
 */
public class WindowGUI extends GuiScreen {

    // The window to be rendered
    private Window mainWindow;

    // The selected category
    public static CategoryComponent selectedCategory;

    public WindowGUI() {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        // Create window
        mainWindow = new Window("Paragon " + Paragon.modVersion, scaledResolution.getScaledWidth() / 2f - 200, scaledResolution.getScaledHeight() / 2f - 150, 400, 300);
    }

    @Override
    public void initGui() {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        // Reset X and Y
        mainWindow = new Window("Paragon " + Paragon.modVersion, scaledResolution.getScaledWidth() / 2f - 200, scaledResolution.getScaledHeight() / 2f - 150, 400, 300);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Make the background darker
        if (ClickGUI.darkenBackground.isEnabled()) {
            drawDefaultBackground();
        }

        // Render window
        mainWindow.render(mouseX, mouseY);

        Paragon.INSTANCE.getTaskbar().drawTaskbar(mouseX, mouseY);
    }

    /**
     * Called when the GUI is closed
     */
    @Override
    public void onGuiClosed() {
        Paragon.INSTANCE.getModuleManager().getModules().forEach(module -> {
            Paragon.INSTANCE.getStorageManager().saveModuleConfiguration(module);
        });
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Handle clicks in the window
        mainWindow.mouseClicked(mouseX, mouseY, mouseButton);

        // Handle taskbar clicks
        Paragon.INSTANCE.getTaskbar().mouseClicked(mouseX, mouseY);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        mainWindow.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        mainWindow.keyTyped(typedChar, keyCode);
    }

    /**
     * Changes whether the game is being paused when in the screen
     * @return Is the game paused whilst in the screen
     */
    public boolean doesGuiPauseGame() {
        return ClickGUI.pause.isEnabled();
    }

}
