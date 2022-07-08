package com.paragon.client.ui.panel.element.setting;

import com.paragon.api.setting.Bind;
import com.paragon.api.setting.Setting;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.ui.panel.Click;
import com.paragon.client.ui.panel.element.Element;
import com.paragon.client.ui.panel.element.module.ModuleElement;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class StringElement extends Element {

    private final Setting<String> setting;

    private boolean focused;

    private float hover;
    private float listeningFactor;

    public StringElement(int layer, Setting<String> setting, ModuleElement moduleElement, float x, float y, float width, float height) {
        super(layer, x, y, width, height);

        setParent(moduleElement.getParent());
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
            listeningFactor = MathHelper.clamp(listeningFactor + (focused ? 0.02f : -0.02f), 0, 1);

            RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), new Color(40, 40, 45).getRGB());
            RenderUtil.drawRect(getX() + getLayer(), getY(), getWidth() - getLayer() * 2, getHeight(), new Color((int) (40 + (30 * hover)), (int) (40 + (30 * hover)), (int) (45 + (30 * hover))).getRGB());
            RenderUtil.drawRect(getX() + getLayer(), getY(), 1, getHeight() * listeningFactor, Color.HSBtoRGB(getParent().getLeftHue() / 360, 1, 0.5f + (0.25f * hover)));

            renderText(setting.getName(), getX() + (getLayer() * 2) + 5, getY() + getHeight() / 2 - 3.5f, 0xFFFFFFFF);

            glPushMatrix();
            glScalef(0.8f, 0.8f, 0.8f);

            {
                float scaleFactor = 1 / 0.8f;

                float side = (getX() + getWidth() - (getStringWidth(getSetting().getValue() + (focused ? "_" : "")) * 0.8f) - 5) * scaleFactor;
                renderText(formatCode(TextFormatting.GRAY) + " " + getSetting().getValue() + (focused ? "_" : ""), side, (getY() + 5f) * scaleFactor, -1);
            }

            glPopMatrix();

            super.render(mouseX, mouseY, dWheel);
        }

        else {
            focused = false;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, Click click) {
        if (setting.isVisible()) {
            if (isHovered(mouseX, mouseY) && getParent().isElementVisible(this)) {
                if (click.equals(Click.LEFT)) {
                    focused = !focused;
                }
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
            if (focused) {
                if (keyCode == Keyboard.KEY_BACK) {
                    if (getSetting().getValue().length() > 0){
                        getSetting().setValue(getSetting().getValue().substring(0, getSetting().getValue().length() - 1));
                    }
                }

                else if (keyCode == Keyboard.KEY_RETURN) {
                    focused = false;
                }

                else if (ChatAllowedCharacters.isAllowedCharacter(keyChar)) {
                    getSetting().setValue(getSetting().getValue() + keyChar);
                }
            }

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

    public Setting<String> getSetting() {
        return setting;
    }

    public float getHover() {
        return hover;
    }
}
