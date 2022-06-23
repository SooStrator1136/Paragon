package com.paragon.client.systems.ui.window.impl.windows.components.settings;

import com.paragon.api.util.calculations.MathsUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.setting.Setting;
import com.paragon.client.systems.ui.window.impl.Window;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class SliderComponent extends SettingComponent<Number> {

    private boolean dragging = false;

    public SliderComponent(Window window, Setting<Number> setting, float x, float y, float width, float height) {
        super(window, setting, x, y, width, height);
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), 0x90000000);

        float renderWidth = 0;

        if (getSetting().getValue() instanceof Float) {
            // Set values
            float diff = Math.min((getWidth() - 8), Math.max(0, mouseX - getX()));

            float min = getSetting().getMin().floatValue();
            float max = getSetting().getMax().floatValue();

            renderWidth = (getWidth() - 8) * (getSetting().getValue().floatValue() - min) / (max - min);

            if (!Mouse.isButtonDown(0)) {
                dragging = false;
            }

            if (dragging) {
                if (diff == 0) {
                    getSetting().setValue(getSetting().getMin());
                } else {
                    float newValue = (float) MathsUtil.roundDouble(((diff / (getWidth() - 8)) * (max - min) + min), 2);

                    float precision = 1 / getSetting().getIncrementation().floatValue();
                    newValue = Math.round(Math.max(min, Math.min(max, newValue)) * precision) / precision;

                    getSetting().setValue(newValue);
                }
            }
        } else if (getSetting().getValue() instanceof Double) {
            // Set values
            double diff = Math.min((getWidth() - 8), Math.max(0, mouseX - getX() + 8));

            double min = getSetting().getMin().doubleValue();
            double max = getSetting().getMax().doubleValue();

            renderWidth = (float) ((getWidth() - 8) * (getSetting().getValue().doubleValue() - min) / (max - min));

            if (!Mouse.isButtonDown(0)) {
                dragging = false;
            }

            if (dragging) {
                if (diff == 0) {
                    getSetting().setValue(getSetting().getMin());
                } else {
                    double newValue = MathsUtil.roundDouble(((diff / (getWidth() - 8)) * (max - min) + min), 2);

                    double precision = 1 / getSetting().getIncrementation().floatValue();
                    newValue = Math.round(Math.max(min, Math.min(max, newValue)) * precision) / precision;

                    getSetting().setValue(newValue);
                }
            }
        }

        renderText(getSetting().getName() + " " + TextFormatting.GRAY + getSetting().getValue(), getX() + 4, getY() + 4, -1);

        RenderUtil.drawRoundedRect(getX() + 4, getY() + getHeight() - 6, getWidth() - 8, 4, 4, 4, 4, 4, new Color(25, 25, 28).getRGB());

        if (renderWidth >= 4) {
            RenderUtil.drawRoundedRect(getX() + 4, getY() + getHeight() - 6, renderWidth, 4, 4, 4, 4, 4, Colours.mainColour.getValue().getRGB());
        }

        RenderUtil.drawRoundedRect(MathHelper.clamp(getX() + 4 + renderWidth - 4, getX() + 4, getX() + getWidth()), getY() + getHeight() - 6, 4, 4, 4, 4, 4, 4, -1);

        super.drawComponent(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && isWithinWindowBounds(getWindow().getY() + 40, getWindow().getY() + getWindow().getHeight()) && button == 0) {
            dragging = true;
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;

        super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean isDragging() {
        return dragging;
    }
}
