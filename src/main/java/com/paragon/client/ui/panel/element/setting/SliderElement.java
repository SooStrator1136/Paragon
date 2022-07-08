package com.paragon.client.ui.panel.element.setting;

import com.paragon.api.util.calculations.MathsUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.setting.Bind;
import com.paragon.api.setting.Setting;
import com.paragon.client.ui.panel.Click;
import com.paragon.client.ui.panel.element.Element;
import com.paragon.client.ui.panel.element.module.ModuleElement;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class SliderElement extends Element {

    private final Setting<Number> setting;

    private float hover;

    private boolean dragging = false;

    private final ModuleElement moduleElement;

    public SliderElement(int layer, Setting<Number> setting, ModuleElement moduleElement, float x, float y, float width, float height) {
        super(layer, x, y, width, height);

        setParent(moduleElement.getParent());
        this.moduleElement = moduleElement;
        this.setting = setting;

        setting.getSubsettings().forEach(subsetting -> {
            if (subsetting.getValue() instanceof Boolean) {
                getSubElements().add(new BooleanElement(layer + 1, (Setting<Boolean>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            }

            else if (subsetting.getValue() instanceof Enum<?>) {
                getSubElements().add(new EnumElement(layer + 1, (Setting<Enum<?>>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            }

            else if (subsetting.getValue() instanceof Number) {
                getSubElements().add(new SliderElement(layer + 1, (Setting<Number>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            }

            else if (subsetting.getValue() instanceof Bind) {
                getSubElements().add(new BindElement(layer + 1, (Setting<Bind>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            }

            else if (subsetting.getValue() instanceof Color) {
                getSubElements().add(new ColourElement(layer + 1, (Setting<Color>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            }

            else if (subsetting.getValue() instanceof String) {
                getSubElements().add(new StringElement(layer + 1, (Setting<String>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            }
        });
    }

    @Override
    public void render(int mouseX, int mouseY, int dWheel) {
        if (setting.isVisible()) {
            hover = MathHelper.clamp(hover + (isHovered(mouseX, mouseY) ? 0.02f : -0.02f), 0, 1);

            float renderWidth = 0;
            float maxWidth = getWidth() - getLayer() * 2;

            if (setting.getValue() instanceof Float) {
                // Set values
                float diff = Math.min(maxWidth, Math.max(0, mouseX - (getX() + getLayer())));

                float min = setting.getMin().floatValue();
                float max = setting.getMax().floatValue();

                renderWidth = maxWidth * (setting.getValue().floatValue() - min) / (max - min);

                if (!Mouse.isButtonDown(0)) {
                    dragging = false;
                }

                if (dragging) {
                    if (diff == 0) {
                        setting.setValue(setting.getMin());
                    } else {
                        float newValue = (float) MathsUtil.roundDouble(((diff / maxWidth) * (max - min) + min), 2);

                        float precision = 1 / setting.getIncrementation().floatValue();
                        newValue = Math.round(Math.max(min, Math.min(max, newValue)) * precision) / precision;

                        setting.setValue(newValue);
                    }
                }
            } else if (setting.getValue() instanceof Double) {
                // Set values
                double diff = Math.min(maxWidth, Math.max(0, mouseX - (getX() + getLayer())));

                double min = setting.getMin().doubleValue();
                double max = setting.getMax().doubleValue();

                renderWidth = (float) (maxWidth * (setting.getValue().doubleValue() - min) / (max - min));

                if (!Mouse.isButtonDown(0)) {
                    dragging = false;
                }

                if (dragging) {
                    if (diff == 0) {
                        setting.setValue(setting.getMin());
                    } else {
                        double newValue = MathsUtil.roundDouble(((diff / maxWidth) * (max - min) + min), 2);

                        double precision = 1 / setting.getIncrementation().floatValue();
                        newValue = Math.round(Math.max(min, Math.min(max, newValue)) * precision) / precision;

                        setting.setValue(newValue);
                    }
                }
            }

            RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), new Color(40, 40, 45).getRGB());
            RenderUtil.drawRect(getX() + getLayer(), getY(), getWidth() - getLayer() * 2, getHeight(), new Color((int) (40 + (30 * hover)), (int) (40 + (30 * hover)), (int) (45 + (30 * hover))).getRGB());

            RenderUtil.drawRect(getX() + getLayer(), getY(), renderWidth, getHeight(), Color.HSBtoRGB(getParent().getLeftHue() / 360, 1, 0.5f + (0.25f * hover)));

            renderText(setting.getName(), getX() + (getLayer() * 2) + 5, getY() + getHeight() / 2 - 3.5f, 0xFFFFFFFF);
            renderText(setting.getValue().toString(), getX() + getWidth() - (getLayer() * 2) - getStringWidth(setting.getValue().toString()) - 3, getY() + getHeight() / 2 - 3.5f, 0xFFFFFFFF);

            super.render(mouseX, mouseY, dWheel);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, Click click) {
        if (setting.isVisible()) {
            if (isHovered(mouseX, mouseY) && getParent().isElementVisible(this)) {
                if (click.equals(Click.LEFT)) {
                    dragging = true;
                }
            }

            super.mouseClicked(mouseX, mouseY, click);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, Click click) {
        dragging = false;

        if (setting.isVisible()) {
            super.mouseReleased(mouseX, mouseY, click);
        }
    }

    @Override
    public void keyTyped(int keyCode, char keyChar) {
        if (setting.isVisible()) {
            super.keyTyped(keyCode, keyChar);
        }
    }

    @Override
    public float getHeight() {
        return getSetting().isVisible() ? super.getHeight() : 0;
    }

    @Override
    public float getSubElementsHeight() {
        return getSetting().isVisible() ? super.getSubElementsHeight() : 0;
    }

    @Override
    public float getTotalHeight() {
        return getSetting().isVisible() ? super.getTotalHeight() : 0;
    }

    public boolean isDragging() {
        return dragging;
    }

    public Setting<Number> getSetting() {
        return setting;
    }

    public float getHover() {
        return hover;
    }
}
