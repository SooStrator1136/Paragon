package com.paragon.client.systems.ui.panel.impl.setting;

import com.paragon.api.util.render.GuiUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.ui.animation.Animation;
import com.paragon.client.systems.ui.panel.PanelGUI;
import com.paragon.client.systems.ui.panel.impl.module.ModuleButton;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Wolfsurge
 */
public class SettingComponent<T> implements TextRenderer {

    private final ModuleButton moduleButton;
    private final Setting<T> setting;
    private float offset;
    private final float height;

    private final ArrayList<SettingComponent<?>> settingComponents = new ArrayList<>();

    public Animation animation;

    public SettingComponent(ModuleButton moduleButton, Setting<T> setting, float offset, float height) {
        this.moduleButton = moduleButton;
        this.setting = setting;
        this.offset = offset;
        this.height = height;

        float settingOffset = getOffset();

        if (!setting.getSubsettings().isEmpty()) {
            for (Setting<?> setting1 : setting.getSubsettings()) {
                if (setting1.getValue() instanceof Boolean) {
                    getSettingComponents().add(new BooleanComponent(getModuleButton(), (Setting<Boolean>) setting1, settingOffset, height));
                    settingOffset += height;
                } else if (setting1.getValue() instanceof AtomicInteger) {
                    getSettingComponents().add(new KeybindComponent(getModuleButton(), (Setting<AtomicInteger>) setting1, settingOffset, height));
                    settingOffset += height;
                } else if (setting1.getValue() instanceof Number) {
                    getSettingComponents().add(new SliderComponent(getModuleButton(), (Setting<Number>) setting1, settingOffset, height));
                    settingOffset += height;
                } else if (setting1.getValue() instanceof Enum<?>) {
                    getSettingComponents().add(new ModeComponent(getModuleButton(), (Setting<Enum<?>>) setting1, settingOffset, height));
                    settingOffset += height;
                } else if (setting1.getValue() instanceof Color) {
                    getSettingComponents().add(new ColourComponent(getModuleButton(), (Setting<Color>) setting1, settingOffset, height));
                    settingOffset += height;
                }
            }
        }

        animation = new Animation(200, false, ClickGUI.easing::getValue);
    }

    public void renderSetting(int mouseX, int mouseY) {
        // Set animation speed
        animation.time = ClickGUI.animationSpeed.getValue();

        if (!getSettingComponents().isEmpty() && hasVisibleSubsettings()) {
            GL11.glPushMatrix();
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("...", (getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth() - 6.5f) * 2, (getModuleButton().getOffset() + getOffset() + 4f) * 2, -1);
            GL11.glPopMatrix();
        }

        if (animation.getAnimationFactor() > 0) {
            getSettingComponents().forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.renderSetting(mouseX, mouseY);
                }
            });

            for (SettingComponent<?> settingComponent : settingComponents) {
                if (settingComponent.getSetting().isVisible()) {
                    RenderUtil.drawRect(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + settingComponent.getOffset(), 2, settingComponent instanceof ColourComponent ? getHeight() : settingComponent.getHeight(), Colours.mainColour.getValue().getRGB());
                }
            }
        }

        if (isMouseOver(mouseX, mouseY) && !(this instanceof ColourComponent)) {
            PanelGUI.tooltip = setting.getDescription();
        }

        // If it's a colour component, we only want to render if the mouse is over the actual button, not the pickers
        else if (this instanceof ColourComponent) {
            if (GuiUtil.mouseOver(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth(), getModuleButton().getOffset() + getOffset() + 13, mouseX, mouseY)) {
                PanelGUI.tooltip = setting.getDescription();
            }
        }
    }

    public boolean hasVisibleSubsettings() {
        for (SettingComponent<?> settingComponent : getSettingComponents()) {
            if (settingComponent.getSetting().isVisible()) {
                return true;
            }
        }

        return false;
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 1) {
            if (isMouseOver(mouseX, mouseY)) {
                animation.setState(!isExpanded());
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

    }

    public void keyTyped(char typedChar, int keyCode) {
        for (SettingComponent<?> settingComponent : getSettingComponents()) {
            if (settingComponent.getSetting().isVisible()) {
                settingComponent.keyTyped(typedChar, keyCode);
            }
        }
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return GuiUtil.mouseOver(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth(), getModuleButton().getOffset() + getOffset() + getHeight(), mouseX, mouseY);
    }

    /**
     * Gets the module button
     * @return The module button
     */
    public ModuleButton getModuleButton() {
        return moduleButton;
    }

    /**
     * Gets the setting
     * @return The setting
     */
    public Setting<T> getSetting() {
        return setting;
    }

    /**
     * Gets the offset
     * @return The offset
     */
    public float getOffset() {
        return offset;
    }

    /**
     * Sets the offset
     * @param newOffset The new offset
     */
    public void setOffset(float newOffset) {
        this.offset = newOffset;
    }

    /**
     * Gets whether the setting component is expanded or not
     * @return Whether the setting component is expanded or not
     */
    public boolean isExpanded() {
        return animation.getAnimationFactor() > 0;
    }

    /**
     * Gets the list of setting components
     * @return The setting components
     */
    public ArrayList<SettingComponent<?>> getSettingComponents() {
        return settingComponents;
    }

    /**
     * Gets the height of the component (without subsettings)
     * @return The height of the component
     */
    public float getHeight() {
        return height;
    }

    /**
     * Gets the absolute height of the module
     * @return The absolute height of the module
     */
    public float getAbsoluteHeight() {
        // return getHeight() + (expanded ? subsettingHeight : 0);
        float subsettingHeight = 0;

        for (SettingComponent settingComponent : getSettingComponents()) {
            subsettingHeight += settingComponent.getHeight();
        }

        return isExpanded() ? getHeight() + subsettingHeight : getHeight();
    }
}
