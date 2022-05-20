package com.paragon.client.systems.ui.window;

import com.paragon.Paragon;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.client.systems.ui.window.impl.Window;
import com.paragon.client.systems.ui.window.impl.windows.ModuleWindow;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;
import java.io.IOException;

public class WindowGUI extends GuiScreen {

    private final Window window;

    public WindowGUI() {
        window = new ModuleWindow();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (ClickGUI.darkenBackground.getValue()) {
            drawDefaultBackground();
        }

        window.drawWindow(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        window.mouseClicked(mouseX, mouseY, mouseButton);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        window.mouseReleased(mouseX, mouseY, state);

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        window.keyTyped(typedChar, keyCode);

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        Paragon.INSTANCE.getStorageManager().saveModules("current");
    }

    @Override
    public boolean doesGuiPauseGame() {
        return ClickGUI.pause.getValue();
    }
}
