package com.paragon.client.ui.configuration.zeroday.element.module;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Bind;
import com.paragon.api.setting.Setting;
import com.paragon.api.util.render.font.FontUtil;
import com.paragon.client.ui.util.Click;
import com.paragon.client.ui.configuration.zeroday.element.Element;
import com.paragon.client.ui.configuration.zeroday.element.setting.*;
import com.paragon.client.ui.configuration.zeroday.panel.CategoryPanel;
import com.paragon.client.ui.util.animation.Animation;
import com.paragon.client.ui.util.animation.Easing;

import java.awt.Color;

public final class ModuleElement extends Element {

    private final Module module;

    private final Animation enabledAnimation = new Animation(() -> 200f, false, () -> Easing.LINEAR);

    public ModuleElement(CategoryPanel parent, Module module, float x, float y, float width, float height) {
        super(0, x, y, width, height);

        setParent(parent);
        this.module = module;

        module.getSettings().forEach(setting -> {
            if (setting.getValue() instanceof Boolean) {
                getSubElements().add(new BooleanElement(1, (Setting<Boolean>) setting, this, getX(), getY(), getWidth(), getHeight()));
            } else if (setting.getValue() instanceof Enum<?>) {
                getSubElements().add(new EnumElement(1, (Setting<Enum<?>>) setting, this, getX(), getY(), getWidth(), getHeight()));
            } else if (setting.getValue() instanceof Number) {
                getSubElements().add(new SliderElement(1, (Setting<Number>) setting, this, getX(), getY(), getWidth(), getHeight()));
            } else if (setting.getValue() instanceof Bind) {
                getSubElements().add(new BindElement(1, (Setting<Bind>) setting, this, getX(), getY(), getWidth(), getHeight()));
            } else if (setting.getValue() instanceof Color) {
                getSubElements().add(new ColourElement(1, (Setting<Color>) setting, this, getX(), getY(), getWidth(), getHeight()));
            } else if (setting.getValue() instanceof String) {
                getSubElements().add(new StringElement(1, (Setting<String>) setting, this, getX(), getY(), getWidth(), getHeight()));
            }
        });
    }

    @Override
    public void render(int mouseX, int mouseY, int dWheel) {
        enabledAnimation.setState(module.isEnabled());

        RenderUtil.drawRect(getX(), getY(), getWidth(), getTotalHeight(), new Color(40, 40, 45).getRGB());
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), new Color((int) (40 + (30 * getHover().getAnimationFactor())), (int) (40 + (30 * getHover().getAnimationFactor())), (int) (45 + (30 * getHover().getAnimationFactor()))).getRGB());

        RenderUtil.drawRect(getX(), getY(), (float) (getWidth() * enabledAnimation.getAnimationFactor()), (float) (getHeight()), new Color(Color.HSBtoRGB(getParent().getLeftHue() / 360, 1f, (float) (0.75f + (0.25f * getHover().getAnimationFactor())))).getRGB());

        int factor = (int) (155 + (100 * enabledAnimation.getAnimationFactor()));

        Color textColour = new Color(factor, factor, factor);

        FontUtil.drawStringWithShadow(module.getName(), getX() + 5, getY() + getHeight() / 2 - 3.5f, textColour.getRGB());

        if (getSubElements().size() > 2) {
            FontUtil.drawStringWithShadow("...", getX() + getWidth() - FontUtil.getStringWidth("...") - 5, getY() + 2f, -1);
        }

        super.render(mouseX, mouseY, dWheel);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, Click click) {
        if (isHovered(mouseX, mouseY) && getParent().isElementVisible(this) && click.equals(Click.LEFT)) {
            module.toggle();
        }

        super.mouseClicked(mouseX, mouseY, click);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, Click click) {
        super.mouseReleased(mouseX, mouseY, click);
    }

    @Override
    public void keyTyped(int keyCode, char keyChar) {
        super.keyTyped(keyCode, keyChar);
    }

    public Module getModule() {
        return module;
    }

}
