package com.paragon.client.ui.console;

import com.paragon.Paragon;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class ConsoleGUI extends GuiScreen {

    @Override
    public void initGui() {
        Paragon.INSTANCE.getConsole().init();
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Make the background darker
        if (ClickGUI.darkenBackground.getValue()) {
            drawDefaultBackground();
        }

        Paragon.INSTANCE.getConsole().draw(mouseX, mouseY);

        Paragon.INSTANCE.getTaskbar().drawTaskbar(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        Paragon.INSTANCE.getTaskbar().mouseClicked(mouseX, mouseY);

        Paragon.INSTANCE.getConsole().mouseClicked(mouseX, mouseY, mouseButton);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        Paragon.INSTANCE.getConsole().keyTyped(typedChar, keyCode);

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return ClickGUI.pause.getValue();
    }
}
