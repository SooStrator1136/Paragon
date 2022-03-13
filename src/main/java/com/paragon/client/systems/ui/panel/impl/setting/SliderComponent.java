package com.paragon.client.systems.ui.panel.impl.setting;

import com.paragon.api.util.calculations.MathUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.ui.panel.impl.module.ModuleButton;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class SliderComponent extends SettingComponent {

    private boolean dragging = false;

    public SliderComponent(ModuleButton moduleButton, NumberSetting setting, float offset, float height) {
        super(moduleButton, setting, offset, height);
    }

    @Override
    public void renderSetting(int mouseX, int mouseY) {
        RenderUtil.drawRect(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getWidth(), getHeight(), isMouseOver(mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());

        float renderWidth;

        // Set values
        float diff = Math.min(84, Math.max(0, mouseX - (getModuleButton().getPanel().getX() + 6)));

        float min = ((NumberSetting) getSetting()).getMin();
        float max = ((NumberSetting) getSetting()).getMax();

        renderWidth = 84 * (((NumberSetting) getSetting()).getValue() - min) / (max - min);

        if (!Mouse.isButtonDown(0))
            dragging = false;

        if (dragging) {
            if (diff == 0) {
                ((NumberSetting) getSetting()).setValue(((NumberSetting) getSetting()).getMin());
            } else {
                float newValue = (float) MathUtil.roundDouble(((diff / 84) * (max - min) + min), 2);
                ((NumberSetting) getSetting()).setValue(newValue);
            }
        }

        GL11.glPushMatrix();
        GL11.glScalef(0.65f, 0.65f, 0.65f);
        float scaleFactor = 1 / 0.65f;
        renderText(getSetting().getName() + formatCode(TextFormatting.GRAY) + " " + ((NumberSetting) getSetting()).getValue(), (getModuleButton().getPanel().getX() + 4) * scaleFactor, (getModuleButton().getOffset() + getOffset() + 3) * scaleFactor, -1);
        GL11.glPopMatrix();

        RenderUtil.drawRect(getModuleButton().getPanel().getX() + 4, getModuleButton().getOffset() + getOffset() + 10, 84, 1, new Color(30, 30, 30).getRGB());
        RenderUtil.drawRect(getModuleButton().getPanel().getX() + 4, getModuleButton().getOffset() + getOffset() + 10, renderWidth, 1, Colours.mainColour.getColour().getRGB());
        RenderUtil.drawRect(getModuleButton().getPanel().getX() + 4 + renderWidth - 0.5f, getModuleButton().getOffset() + getOffset() + 9.5f, 2, 2, -1);

        super.renderSetting(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (isMouseOver(mouseX, mouseY)) {
                // Set dragging state
                dragging = true;
            }
        }

        if (isExpanded()) {
            getSettingComponents().forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.mouseClicked(mouseX, mouseY, mouseButton);
                }
            });
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        dragging = false;

        getSettingComponents().forEach(settingComponent -> settingComponent.mouseReleased(mouseX, mouseY, mouseButton));

        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public float getAbsoluteHeight() {
        float subsettingHeight = 0;

        for (SettingComponent settingComponent : getSettingComponents()) {
            subsettingHeight += settingComponent.getHeight();
        }

        return isExpanded() ? getHeight() + subsettingHeight : getHeight();
    }
}
