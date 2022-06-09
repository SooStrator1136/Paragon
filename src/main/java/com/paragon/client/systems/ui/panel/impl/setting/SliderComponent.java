package com.paragon.client.systems.ui.panel.impl.setting;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.util.calculations.MathUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.setting.Setting;
import com.paragon.client.systems.ui.panel.impl.module.ModuleButton;
import com.paragon.client.systems.module.impl.client.Colours;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class SliderComponent extends SettingComponent<Number> {

    private boolean dragging = false;

    public SliderComponent(ModuleButton moduleButton, Setting<Number> setting, float offset, float height) {
        super(moduleButton, setting, offset, height);
    }

    @Override
    public void renderSetting(int mouseX, int mouseY) {
        RenderUtil.drawRect(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getWidth(), getHeight(), isMouseOver(mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());

        float renderWidth = 0;

        if (getSetting().getValue() instanceof Float) {
            // Set values
            float diff = Math.min(88, Math.max(0, mouseX - (getModuleButton().getPanel().getX() + 6)));

            float min = getSetting().getMin().floatValue();
            float max = getSetting().getMax().floatValue();

            renderWidth = 88 * (getSetting().getValue().floatValue() - min) / (max - min);

            if (!Mouse.isButtonDown(0)) {
                dragging = false;
            }

            if (dragging) {
                if (diff == 0) {
                    getSetting().setValue(getSetting().getMin());
                } else {
                    float newValue = (float) MathUtil.roundDouble(((diff / 88) * (max - min) + min), 2);

                    float precision = 1 / getSetting().getIncrementation().floatValue();
                    newValue = Math.round(Math.max(min, Math.min(max, newValue)) * precision) / precision;

                    getSetting().setValue(newValue);
                }
            }
        } else if (getSetting().getValue() instanceof Double) {
            // Set values
            double diff = Math.min(88, Math.max(0, mouseX - (getModuleButton().getPanel().getX() + 6)));

            double min = getSetting().getMin().doubleValue();
            double max = getSetting().getMax().doubleValue();

            renderWidth = (float) (88 * (getSetting().getValue().doubleValue() - min) / (max - min));

            if (!Mouse.isButtonDown(0)) {
                dragging = false;
            }

            if (dragging) {
                if (diff == 0) {
                    getSetting().setValue(getSetting().getMin());
                } else {
                    double newValue = MathUtil.roundDouble(((diff / 88) * (max - min) + min), 2);

                    double precision = 1 / getSetting().getIncrementation().floatValue();
                    newValue = Math.round(Math.max(min, Math.min(max, newValue)) * precision) / precision;

                    getSetting().setValue(newValue);
                }
            }
        }

        GL11.glPushMatrix();
        GL11.glScalef(0.65f, 0.65f, 0.65f);

        {
            float scaleFactor = 1 / 0.65f;
            renderText(getSetting().getName(), (getModuleButton().getPanel().getX() + 5) * scaleFactor, (getModuleButton().getOffset() + getOffset() + 3) * scaleFactor, -1);

            float side = (getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth() - (getStringWidth(String.valueOf(getSetting().getValue())) * 0.65f) - 5) * scaleFactor;
            renderText(formatCode(TextFormatting.GRAY) + " " + getSetting().getValue(), side, (getModuleButton().getOffset() + getOffset() + 3) * scaleFactor, -1);
        }

        GL11.glPopMatrix();

        RenderUtil.drawRect(getModuleButton().getPanel().getX() + 4, getModuleButton().getOffset() + getOffset() + 10, 88, 1, new Color(30, 30, 30).getRGB());
        RenderUtil.drawRect(getModuleButton().getPanel().getX() + 4, getModuleButton().getOffset() + getOffset() + 10, renderWidth, 1, Colours.mainColour.getValue().getRGB());
        RenderUtil.drawRect(getModuleButton().getPanel().getX() + 4 + renderWidth - 0.5f, getModuleButton().getOffset() + getOffset() + 9.5f, 2, 2, -1);

        super.renderSetting(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (isMouseOver(mouseX, mouseY)) {
                // Set dragging state
                dragging = true;

                SettingUpdateEvent settingUpdateEvent = new SettingUpdateEvent(getSetting());
                Paragon.INSTANCE.getEventBus().post(settingUpdateEvent);
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

        for (SettingComponent<?> settingComponent : getSettingComponents()) {
            subsettingHeight += settingComponent.getHeight();
        }

        return isExpanded() ? getHeight() + subsettingHeight : getHeight();
    }

    public boolean isDragging() {
        return dragging;
    }
}