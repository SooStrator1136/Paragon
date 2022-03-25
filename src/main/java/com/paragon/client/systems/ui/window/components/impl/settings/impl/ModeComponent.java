package com.paragon.client.systems.ui.window.components.impl.settings.impl;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.api.util.string.EnumFormatter;
import com.paragon.client.systems.ui.window.components.Window;
import com.paragon.client.systems.ui.window.components.impl.ModuleButtonComponent;
import com.paragon.client.systems.ui.window.components.impl.settings.SettingComponent;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.settings.Setting;
import com.paragon.client.systems.module.settings.impl.*;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author Wolfsurge
 * @since 30/01/22
 */
public class ModeComponent extends SettingComponent implements TextRenderer {

    // Parent Window
    private Window parentWindow;

    // Parent Module Component
    private ModuleButtonComponent parentModuleButton;

    // Mode Setting
    private final ModeSetting modeSetting;

    public ModeComponent(ModuleButtonComponent parentModuleButton, Window parentWindow, ModeSetting modeSetting, float x, float y) {
        setParentWindow(parentWindow);
        setSetting(modeSetting);
        this.modeSetting = (ModeSetting) getSetting();
        setX(x);
        setY(y);
        setWidth(193);
        setHeight(20);

        float offset = getY() + getHeight() + 0.5f;

        for (Setting setting : modeSetting.getSubsettings()) {
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

        RenderUtil.drawRect(getX(), getY(), 1, getHeight(), Colours.mainColour.getColour().getRGB());
        renderText(modeSetting.getName() + formatCode(TextFormatting.GRAY) + " " + EnumFormatter.getFormattedText((Enum<?>) modeSetting.getCurrentMode()), getX() + 3, getY() + 3, -1);

        if (!settingComponents.isEmpty() && hasVisibleSubsettings()) {
            renderText("...", getX() + getWidth() - 9, getY() + 5f, -1);
        }

        GL11.glPushMatrix();
        GL11.glScalef(0.55f, 0.55f, 0); // Shrink scale
        float scaleFactor = 1 / 0.55f;
        renderText(getSetting().getDescription(), (getX() + 3) * scaleFactor, (getY() + 13) * scaleFactor, -1);
        GL11.glPopMatrix();

        if (expanded) {
            for (SettingComponent settingComponent : settingComponents) {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.render(mouseX, mouseY);
                }
            }
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
        if (mouseButton == 0) {
            modeSetting.cycleMode();

            SettingUpdateEvent settingUpdateEvent = new SettingUpdateEvent(getSetting());
            Paragon.INSTANCE.getEventBus().post(settingUpdateEvent);
        } else if (mouseButton == 1) {
            this.expanded = !expanded;
        }
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
