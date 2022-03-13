package com.paragon.client.systems.ui.window.components.impl.settings.impl;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.ui.window.components.Window;
import com.paragon.client.systems.ui.window.components.impl.ModuleButtonComponent;
import com.paragon.client.systems.ui.window.components.impl.settings.SettingComponent;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.settings.Setting;
import com.paragon.client.systems.module.settings.impl.*;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author Wolfsurge
 * @since 30/01/22
 */
public class BooleanComponent extends SettingComponent implements TextRenderer {

    // Parent Window
    private Window parentWindow;

    // Parent Module Component
    private ModuleButtonComponent parentModuleButton;

    // Boolean Setting
    private final BooleanSetting booleanSetting;

    public BooleanComponent(ModuleButtonComponent parentModuleButton, Window parentWindow, BooleanSetting booleanSetting, float x, float y) {
        setParentWindow(parentWindow);
        setSetting(booleanSetting);
        this.booleanSetting = (BooleanSetting) getSetting();
        setX(x);
        setY(y);
        setWidth(193);
        setHeight(20);

        float offset = getY() + getHeight() + 0.5f;

        for (Setting setting : booleanSetting.getSubsettings()) {
            SettingComponent settingComponent = null;

            if (setting instanceof BooleanSetting) {
                settingComponent = new BooleanComponent(parentModuleButton, parentWindow, (BooleanSetting) setting, getX() + 5, offset);
            } else if (setting instanceof NumberSetting) {
                settingComponent = new SliderComponent(parentModuleButton, parentWindow, (NumberSetting) setting, getX() + 5, offset, 193, 20);
            } else if (setting instanceof ModeSetting) {
                settingComponent = new ModeComponent(parentModuleButton, parentWindow, (ModeSetting) setting, getX() + 5, offset);
            } else if (setting instanceof ColourSetting) {
                settingComponent = new ColourComponent(parentModuleButton, parentWindow, (ColourSetting) setting, getX() + 5, offset);
            } else if (setting instanceof KeybindSetting) {
                settingComponent = new KeybindComponent(parentModuleButton, parentWindow, (KeybindSetting) setting, getX() + 5, offset);
            }

            if (settingComponent == null) {
                continue;
            }

            settingComponents.add(settingComponent);
            offset += settingComponent.getHeight() + 0.5f;
        }
    }

    /**
     * Renders the component
     * @param mouseX The mouse's X
     * @param mouseY The mouse's Y
     */
    @Override public void render(int mouseX, int mouseY) {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), isMouseOnButton(mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());
        if(booleanSetting.isEnabled()) RenderUtil.drawRect(getX(), getY(), 1, getHeight(), Colours.mainColour.getColour().getRGB());
        renderText(booleanSetting.getName(), getX() + 3, getY() + 3, booleanSetting.isEnabled() ? Colours.mainColour.getColour().getRGB() : -1);

        GL11.glPushMatrix();
        GL11.glScalef(.5f, .5f, 0); // Shrink scale
        renderText(booleanSetting.getDescription(), (getX() + 3) * 2, (getY() + 13) * 2, -1);
        GL11.glPopMatrix();

        if (expanded) {
            for (SettingComponent settingComponent : settingComponents) {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.render(mouseX, mouseY);
                }
            }
        }

        if (!settingComponents.isEmpty()) {
            renderText("...", getX() + getWidth() - getStringWidth("...") - 1, getY() + 4.5f, -1);
        }

        refreshOffsets();
    }

    /**
     * Called when the mouse is clicked
     * @param mouseX The mouse's X
     * @param mouseY The mouse's Y
     * @param mouseButton The button that is clicked
     */
    @Override public void whenClicked(int mouseX, int mouseY, int mouseButton) {
        if(mouseButton == 0) booleanSetting.setEnabled(!booleanSetting.isEnabled());
        else if (mouseButton == 1) this.expanded = !this.expanded;
    }

    /**
     * Gets the parent window
     * @return The parent window
     */
    public Window getParentWindow() {
        return parentWindow;
    }

    /**
     * Sets the parent window
     * @param parentWindow The new parent window
     */
    public void setParentWindow(Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    /**
     * Gets the parent module button
     * @return The parent module button
     */
    public ModuleButtonComponent getParentModuleButton() {
        return parentModuleButton;
    }

    /**
     * Sets the parent module button
     * @param parentModuleButton The new parent module button
     */
    public void setParentModuleButton(ModuleButtonComponent parentModuleButton) {
        this.parentModuleButton = parentModuleButton;
    }
}
