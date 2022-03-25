package com.paragon.client.systems.ui.window.components.impl.settings.impl;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.util.render.GuiUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.ui.window.components.Window;
import com.paragon.client.systems.ui.window.components.impl.ModuleButtonComponent;
import com.paragon.client.systems.ui.window.components.impl.settings.SettingComponent;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.module.settings.impl.KeybindSetting;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author Wolfsurge
 * @since 30/01/22
 */
public class KeybindComponent extends SettingComponent implements TextRenderer {

    // Parent Window
    private Window parentWindow;

    // Parent Module Component
    private ModuleButtonComponent parentModuleButton;

    // Keybind Setting
    private final KeybindSetting keybindSetting;

    private boolean isListening;

    public KeybindComponent(ModuleButtonComponent parentModuleButton, Window parentWindow, KeybindSetting keybindSetting, float x, float y) {
        setParentWindow(parentWindow);
        setSetting(keybindSetting);
        this.keybindSetting = (KeybindSetting) getSetting();
        setX(x);
        setY(y);
        setWidth(193);
        setHeight(20);
    }

    /**
     * Renders the component
     * @param mouseX The mouse's X
     * @param mouseY The mouse's Y
     */
    @Override
    public void render(int mouseX, int mouseY) {
        // Render background
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), isMouseOnButton(mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());

        // Render bar if listening
        if(isListening) RenderUtil.drawRect(getX(), getY(), 1, getHeight(), Colours.mainColour.getColour().getRGB());

        // Render text
        renderText(keybindSetting.getName() + formatCode(TextFormatting.GRAY) + " " + (isListening ? "..." : Keyboard.getKeyName(keybindSetting.getKeyCode())), getX() + 3, getY() + 3, -1);

        GL11.glPushMatrix();
        GL11.glScalef(0.55f, 0.55f, 0); // Shrink scale
        float scaleFactor = 1 / 0.55f;
        renderText(getSetting().getDescription(), (getX() + 3) * scaleFactor, (getY() + 13) * scaleFactor, -1);
        GL11.glPopMatrix();

        // Render reset text
        renderText("Reset", getX() + getWidth() - 40, getY() + 5.5f, -1);
    }

    /**
     * Called when the mouse is clicked
     * @param mouseX The mouse's X
     * @param mouseY The mouse's Y
     * @param mouseButton The button that is clicked
     */
    @Override
    public void whenClicked(int mouseX, int mouseY, int mouseButton) {
        if (GuiUtil.mouseOver(getX(), getY(), getX() + getWidth() - 45, getY() + getHeight(), mouseX, mouseY)) {
            isListening = !isListening;

            SettingUpdateEvent settingUpdateEvent = new SettingUpdateEvent(getSetting());
            Paragon.INSTANCE.getEventBus().post(settingUpdateEvent);
        }
        // If the user presses the reset text, set the bind to 0 (none)
        else if(GuiUtil.mouseOver(getX() + getWidth() - 44, getY(), getX() + getWidth(), getY() + getHeight(), mouseX, mouseY)) {
            keybindSetting.setKeyCode(0);

            SettingUpdateEvent settingUpdateEvent = new SettingUpdateEvent(getSetting());
            Paragon.INSTANCE.getEventBus().post(settingUpdateEvent);

            isListening = false;
        }
    }

    /**
     * Triggered when a key is pressed
     * @param typedChar The character typed
     * @param keyCode The key code of the character
     */
    @Override public void keyTyped(char typedChar, int keyCode) {
        if(isListening) {
            keybindSetting.setKeyCode(keyCode);
            isListening = false;
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
