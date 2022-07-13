package com.paragon.client.ui.panel.element.module;

import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Bind;
import com.paragon.api.setting.Setting;
import com.paragon.client.ui.panel.Click;
import com.paragon.client.ui.panel.element.Element;
import com.paragon.client.ui.panel.element.setting.*;
import com.paragon.client.ui.panel.panel.CategoryPanel;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public final class ModuleElement extends Element {

    private final Module module;

    private float hover;
    private float enabled;

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
        hover = MathHelper.clamp(hover + (isHovered(mouseX, mouseY) ? 0.02f : -0.02f), 0, 1);
        enabled = MathHelper.clamp(enabled + (module.isEnabled() ? 0.02f : -0.02f), 0, 1);

        if (!getParent().isElementVisible(this)) {
            hover = 0;
        }

        RenderUtil.drawRect(getX(), getY(), getWidth(), getTotalHeight(), new Color(40, 40, 45).getRGB());
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), new Color((int) (40 + (30 * hover)), (int) (40 + (30 * hover)), (int) (45 + (30 * hover))).getRGB());

        RenderUtil.drawRect(getX() + getWidth() - 4, getY() + 1, 2, (getHeight() - 2) * enabled, ColourUtil.integrateAlpha(new Color(Color.HSBtoRGB(getParent().getLeftHue() / 360, 1, 0.5f + (0.25f * hover))), (int) (255 * enabled)).getRGB());

        renderText(module.getName(), getX() + 5, getY() + getHeight() / 2 - 3.5f, new Color((int) (155 + (100 * enabled)), (int) (155 + (100 * enabled)), (int) (155 + (100 * enabled))).getRGB());

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

    public float getHover() {
        return hover;
    }

}
