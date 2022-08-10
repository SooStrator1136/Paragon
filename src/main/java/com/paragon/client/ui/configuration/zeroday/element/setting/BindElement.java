package com.paragon.client.ui.configuration.zeroday.element.setting;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.setting.Bind;
import com.paragon.api.setting.Setting;
import com.paragon.api.util.render.font.FontUtil;
import me.surge.animation.Animation;
import me.surge.animation.Easing;
import com.paragon.client.ui.util.Click;
import com.paragon.client.ui.configuration.zeroday.element.Element;
import com.paragon.client.ui.configuration.zeroday.element.module.ModuleElement;
import org.lwjgl.input.Keyboard;

import java.awt.Color;

@SuppressWarnings("unchecked")
public final class BindElement extends Element {

    private final Setting<Bind> setting;

    private boolean listening;
    private final Animation listeningAnimation = new Animation(() -> 200f, false, () -> Easing.LINEAR);

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
            listeningAnimation.setState(listening);

            RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), new Color(40, 40, 45).getRGB());
            RenderUtil.drawRect(getX() + getLayer(), getY(), getWidth() - getLayer() * 2, getHeight(), new Color((int) (40 + (30 * getHover().getAnimationFactor())), (int) (40 + (30 * getHover().getAnimationFactor())), (int) (45 + (30 * getHover().getAnimationFactor()))).getRGB());
            RenderUtil.drawRect(getX() + getLayer(), getY(), 1, (float) (getHeight() * listeningAnimation.getAnimationFactor()), Color.HSBtoRGB(getParent().getLeftHue() / 360, 1f, (float) (0.5f + (0.25f * getHover().getAnimationFactor()))));

            FontUtil.drawStringWithShadow(setting.getName(), getX() + (getLayer() * 2) + 5, getY() + getHeight() / 2 - 3.5f, 0xFFFFFFFF);
            FontUtil.drawStringWithShadow(setting.getValue().getButtonName(), (getX() + getWidth() - (getLayer() * 2)) - FontUtil.getStringWidth(setting.getValue().getButtonName()) - 3, getY() + getHeight() / 2 - 3.5f, new Color(150, 150, 155).getRGB());

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

}
