package com.paragon.client.ui.configuration.zeroday.element.setting;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.font.FontUtil;
import com.paragon.api.util.string.StringUtil;
import com.paragon.api.setting.Bind;
import com.paragon.api.setting.Setting;
import me.surge.animation.Animation;
import me.surge.animation.Easing;
import com.paragon.client.ui.util.Click;
import com.paragon.client.ui.configuration.zeroday.element.Element;
import com.paragon.client.ui.configuration.zeroday.element.module.ModuleElement;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

@SuppressWarnings("unchecked")
public final class EnumElement extends Element {

    private final Setting<Enum<?>> setting;
    private final Animation scrollAnimation = new Animation(() -> 1250f, false, () -> Easing.LINEAR);

    public EnumElement(int layer, Setting<Enum<?>> setting, ModuleElement moduleElement, float x, float y, float width, float height) {
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
            RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), new Color(40, 40, 45).getRGB());
            RenderUtil.drawRect(getX() + getLayer(), getY(), getWidth() - getLayer() * 2, getHeight(), new Color((int) (40 + (30 * getHover().getAnimationFactor())), (int) (40 + (30 * getHover().getAnimationFactor())), (int) (45 + (30 * getHover().getAnimationFactor()))).getRGB());

            float x = getX() + (getLayer() * 2) + 5;
            float totalWidth = getWidth() - (getLayer() * 2);
            float maxTextWidth = totalWidth - FontUtil.getStringWidth(StringUtil.getFormattedText(setting.getValue())) - 5;

            float visibleX = FontUtil.getStringWidth(setting.getName()) - maxTextWidth;

            scrollAnimation.setState(isHovered(mouseX, mouseY));

            if (FontUtil.getStringWidth(setting.getName()) > maxTextWidth) {
                x -= (visibleX + 9) * scrollAnimation.getAnimationFactor();
            }

            float scissorY = MathHelper.clamp(getY(), getParent().getY() + 22, getParent().getY() + MathHelper.clamp(
                    // Scissor comedy
                    getParent().getScissorHeight() + 8, 0, 358));

            float scissorHeight = getHeight();

            RenderUtil.pushScissor(getX() + (getLayer() * 2), scissorY, totalWidth - (FontUtil.getStringWidth(StringUtil.getFormattedText(setting.getValue())) + 9), scissorHeight);

            FontUtil.drawStringWithShadow(setting.getName(), x, getY() + getHeight() / 2 - 3.5f, 0xFFFFFFFF);

            RenderUtil.popScissor();

            FontUtil.drawStringWithShadow(StringUtil.getFormattedText(setting.getValue()), getX() + getWidth() - (getLayer() * 2) - FontUtil.getStringWidth(StringUtil.getFormattedText(setting.getValue())) - (3 + (getSubElements().isEmpty() ? 0 : FontUtil.getStringWidth("...") + 3)), getY() + getHeight() / 2 - 3.5f, 0xFFFFFFFF);

            if (!getSubElements().isEmpty()) {
                FontUtil.drawStringWithShadow("...", getX() + getWidth() - FontUtil.getStringWidth("...") - 5, getY() + 2f, -1);
            }

            super.render(mouseX, mouseY, dWheel);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, Click click) {
        if (setting.isVisible()) {
            if (isHovered(mouseX, mouseY) && getParent().isElementVisible(this) && click.equals(Click.LEFT)) {
                setting.setValue(setting.getNextMode());
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

    public Setting<Enum<?>> getSetting() {
        return setting;
    }

}
