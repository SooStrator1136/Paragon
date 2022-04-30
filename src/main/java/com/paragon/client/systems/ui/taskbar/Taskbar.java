package com.paragon.client.systems.ui.taskbar;

import com.paragon.api.util.Wrapper;
import com.paragon.api.util.render.GuiUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.impl.client.ClientFont;
import com.paragon.client.systems.ui.console.ConsoleGUI;
import com.paragon.client.systems.ui.taskbar.icons.Icon;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author Wolfsurge
 */
public class Taskbar implements Wrapper, TextRenderer {

    private ArrayList<Icon> icons = new ArrayList<>();
    private boolean open = true;

    public Taskbar() {
        int x = (int) getStringWidth("Paragon") + 10;

        icons.add(new Icon("GUI", x, ClickGUI::getGUI));
        x += getStringWidth("GUI") + 7;
        icons.add(new Icon("Console", x, ConsoleGUI::new));
    }

    public void drawTaskbar(int mouseX, int mouseY) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        if (open) {
            RenderUtil.drawRect(0, scaledResolution.getScaledHeight() - 21, scaledResolution.getScaledWidth(), 21, new Color(20, 20, 20).getRGB());
            RenderUtil.drawRect(0, scaledResolution.getScaledHeight() - 23, scaledResolution.getScaledWidth(), 2, Colours.mainColour.getColour().getRGB());

            renderText("Paragon", 2, scaledResolution.getScaledHeight() - 15, Colours.mainColour.getColour().getRGB());

            for (Icon icon : icons) {
                icon.draw(mouseX, mouseY);
            }
        }

        renderText(open ? "Close" : "Open", scaledResolution.getScaledWidth() - getStringWidth(open ? "Close" : "Open") - 3, scaledResolution.getScaledHeight() - getFontHeight() - (ClientFont.INSTANCE.isEnabled() ? 3 : 5), -1);
    }

    public void mouseClicked(int mouseX, int mouseY) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        if (open) {
            icons.forEach(icon -> {
                icon.whenClicked(mouseX, mouseY);
            });
        }

        if (GuiUtil.mouseOver(scaledResolution.getScaledWidth() - getStringWidth(open ? "Close" : "Open") - 3, scaledResolution.getScaledHeight() - getFontHeight() - 5, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), mouseX, mouseY)) {
            open = !open;
        }
    }

}
