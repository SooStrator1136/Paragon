package com.paragon.client.ui.taskbar;

import com.paragon.api.util.Wrapper;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.impl.client.ClientFont;
import com.paragon.client.ui.animation.Animation;
import com.paragon.client.ui.console.ConsoleGUI;
import com.paragon.client.ui.taskbar.icons.Icon;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Wolfsurge
 */
@SideOnly(Side.CLIENT)
public final class Taskbar implements Wrapper, TextRenderer {

    private ArrayList<Icon> icons = new ArrayList<>();

    private final Animation tooltipAnimation = new Animation(() -> 200f, false, ClickGUI.getEasing()::getValue);
    private String tooltip = "";

    public Taskbar() {
        int x = (int) getStringWidth("Paragon") + 10;

        icons.add(new Icon("GUI", x, ClickGUI::getGUI));
        x += getStringWidth("GUI") + 7;
        icons.add(new Icon("Console", x, ConsoleGUI::new));
    }

    public void drawTaskbar(int mouseX, int mouseY) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        RenderUtil.drawRect(0, scaledResolution.getScaledHeight() - 18, scaledResolution.getScaledWidth(), 18, new Color(20, 20, 20).getRGB());
        RenderUtil.drawRect(0, scaledResolution.getScaledHeight() - 19, scaledResolution.getScaledWidth(), 2, Colours.mainColour.getValue().getRGB());

        renderText("Paragon", 2, scaledResolution.getScaledHeight() - (ClientFont.INSTANCE.isEnabled() ? 12 : 11), Colours.mainColour.getValue().getRGB());

        for (Icon icon : icons) {
            icon.draw(mouseX, mouseY);
        }

        if (tooltip != "" && ClickGUI.getTooltips().getValue()) {
            renderText(tooltip, (float) (scaledResolution.getScaledWidth() - ((getStringWidth(tooltip) + 2) * tooltipAnimation.getAnimationFactor())), (float) ((scaledResolution.getScaledHeight()) - (ClientFont.INSTANCE.isEnabled() ? 12 : 11)), -1);
        }
    }

    public void mouseClicked(int mouseX, int mouseY) {
        icons.forEach(icon -> icon.whenClicked(mouseX, mouseY));
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltipIn) {
        if (Objects.equals(tooltipIn, "")) {
            tooltipAnimation.setState(false);
        }

        else {
            tooltip = tooltipIn;
            tooltipAnimation.setState(true);
        }
    }

}
