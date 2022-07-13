package com.paragon.client.ui.panel.element.setting;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.setting.Bind;
import com.paragon.api.setting.Setting;
import com.paragon.client.ui.panel.Click;
import com.paragon.client.ui.panel.element.Element;
import com.paragon.client.ui.panel.element.module.ModuleElement;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;

import java.awt.Color;

@SuppressWarnings("unchecked")
public final class BindElement extends Element {

    private final Setting<Bind> setting;

    private boolean listening;
    private float hover;
    private float enabled;

    public BindElement(int layer, Setting<Bind> setting, ModuleElement moduleElement, float x, float y, float width, float height) {
        super(layer, x, y, width, height);

        setParent(moduleElement.getParent());
        this.setting = setting;

        setting.getSubsettings().forEach(subsetting -> {
            if (subsetting.getValue() instanceof Boolean) {
                getSubElements().add(new BooleanElement(layer + 1, (Setting<Boolean>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            } else if (subsetting.getValue() instanceof Enum<?>) {
                getSubElements().add(new EnumElement(layer + 1, (Setting<Enum<?>>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            } else if (subsetting.getValue() instanceof Number) {
                getSubElements().add(new SliderElement(layer + 1, (Setting<Number>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            } else if (subsetting.getValue() instanceof Bind) {
                getSubElements().add(new BindElement(layer + 1, (Setting<Bind>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            } else if (subsetting.getValue() instanceof Color) {
                getSubElements().add(new ColourElement(layer + 1, (Setting<Color>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            } else if (subsetting.getValue() instanceof String) {
                getSubElements().add(new StringElement(layer + 1, (Setting<String>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            }
        });
    }

    @Override
    public void render(int mouseX, int mouseY, int dWheel) {
        if (setting.isVisible()) {
            hover = MathHelper.clamp(hover + (isHovered(mouseX, mouseY) ? 0.02f : -0.02f), 0, 1);
            enabled = MathHelper.clamp(enabled + (listening ? 0.02f : -0.02f), 0, 1);

            RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), new Color(40, 40, 45).getRGB());
            RenderUtil.drawRect(getX() + getLayer(), getY(), getWidth() - getLayer() * 2, getHeight(), new Color((int) (40 + (30 * hover)), (int) (40 + (30 * hover)), (int) (45 + (30 * hover))).getRGB());
            RenderUtil.drawRect(getX() + getLayer(), getY(), 1, getHeight() * enabled, Color.HSBtoRGB(getParent().getLeftHue() / 360, 1, 0.5f + (0.25f * hover)));

            renderText(setting.getName(), getX() + (getLayer() * 2) + 5, getY() + getHeight() / 2 - 3.5f, 0xFFFFFFFF);
            renderText(setting.getValue().getButtonName(), (getX() + getWidth() - (getLayer() * 2)) - getStringWidth(setting.getValue().getButtonName()) - 3, getY() + getHeight() / 2 - 3.5f, new Color(150, 150, 155).getRGB());

            super.render(mouseX, mouseY, dWheel);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, Click click) {
        if (setting.isVisible()) {
            if (isHovered(mouseX, mouseY) && getParent().isElementVisible(this) && click.equals(Click.LEFT)) {
                listening = !listening;
                return;
            }

            if (listening) {
                listening = false;

                setting.getValue().setDevice(Bind.Device.MOUSE);
                setting.getValue().setButtonCode(click.getButton());

                return;
            }

            if (isHovered(mouseX, mouseY) && click.equals(Click.LEFT)) {
                // Set listening
                listening = !listening;

                SettingUpdateEvent settingUpdateEvent = new SettingUpdateEvent(setting);
                Paragon.INSTANCE.getEventBus().post(settingUpdateEvent);
            }

            super.mouseClicked(mouseX, mouseY, click);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, Click click) {
        if (setting.isVisible()) {
            super.mouseReleased(mouseX, mouseY, click);
        }
    }

    @Override
    public void keyTyped(int keyCode, char keyChar) {
        if (setting.isVisible()) {
            if (listening) {
                listening = false;

                setting.getValue().setDevice(Bind.Device.KEYBOARD);

                if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                    setting.getValue().setButtonCode(0);
                    return;
                }

                setting.getValue().setButtonCode(keyCode);
            }
        }

        super.keyTyped(keyCode, keyChar);
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

    public Setting<Bind> getSetting() {
        return setting;
    }

    public float getHover() {
        return hover;
    }

}
