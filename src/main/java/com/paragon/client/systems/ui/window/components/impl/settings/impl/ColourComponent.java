package com.paragon.client.systems.ui.window.components.impl.settings.impl;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.ui.window.components.Window;
import com.paragon.client.systems.ui.window.components.impl.ModuleButtonComponent;
import com.paragon.client.systems.ui.window.components.impl.settings.SettingComponent;
import com.paragon.client.systems.module.settings.impl.ColourSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author Wolfsurge, with a lot of code from Wurst +3, but edited.
 * @since 30/01/22
 */
public class ColourComponent extends SettingComponent implements TextRenderer {

    // Parent Window
    private Window parentWindow;

    // Parent Module Component
    private ModuleButtonComponent parentModuleButton;

    // Colour Setting
    private final ColourSetting colourSetting;

    private final NumberSetting red;
    private final NumberSetting green;
    private final NumberSetting blue;
    private final NumberSetting alpha;

    private final ArrayList<SliderComponent> sliders = new ArrayList<>();

    public ColourComponent(ModuleButtonComponent parentModuleButton, Window parentWindow, ColourSetting colourSetting, float x, float y) {
        setParentWindow(parentWindow);
        setSetting(colourSetting);
        this.colourSetting = (ColourSetting) getSetting();
        setX(x);
        setY(y);
        setWidth(193);
        setHeight(70);

        // Set settings
        this.red = new NumberSetting("Red", "The red value", colourSetting.getColour().getRed(), 0, 255, 1);
        this.green = new NumberSetting("Green", "The green value", colourSetting.getColour().getGreen(), 0, 255, 1);
        this.blue = new NumberSetting("Blue", "The blue value", colourSetting.getColour().getBlue(), 0, 255, 1);
        this.alpha = new NumberSetting("Alpha", "The alpha (opacity) value", colourSetting.getColour().getAlpha(), 0, 255, 1);

        // Add sliders
        sliders.add(new SliderComponent(parentModuleButton, parentWindow, red, x + 3, y + 22, 85, 20));
        sliders.add(new SliderComponent(parentModuleButton, parentWindow, green, x + 3, y + 47, 85, 20));
        sliders.add(new SliderComponent(parentModuleButton, parentWindow, blue, x + 93, y + 22, 85, 20));
        sliders.add(new SliderComponent(parentModuleButton, parentWindow, alpha, x + 93, y + 47, 85, 20));
    }

    /**
     * Renders the component
     * @param mouseX The mouse's X
     * @param mouseY The mouse's Y
     */
    @Override
    public void render(int mouseX, int mouseY) {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), new Color(23, 23, 23).getRGB());
        renderText(colourSetting.getName(), getX() + 3, getY() + 3, -1);

        GL11.glPushMatrix();
        GL11.glScalef(0.55f, 0.55f, 0); // Shrink scale
        float scaleFactor = 1 / 0.55f;
        renderText(getSetting().getDescription(), (getX() + 3) * scaleFactor, (getY() + 13) * scaleFactor, -1);
        GL11.glPopMatrix();
        RenderUtil.drawRect(getX() + getWidth() - 26, getY() + 1, 20, 20, colourSetting.getColour().getRGB());

        // Some horrible code to keep the sliders where they are meant to be
        float sliderX = getX() + 3;
        float sliderY = getY() + 22;
        for (int i = 0; i < sliders.size(); i++) {
            if (i == 1) {
                sliderY = getY() + 47;
            } else if (i == 2) {
                sliderX = getX() + 93;
                sliderY = getY() + 22;
            } else if (i == 3) {
                sliderY = getY() + 47;
            }

            sliders.get(i).setX(sliderX);
            sliders.get(i).setY(sliderY);
        }

        for (SliderComponent sliderComponent : sliders) {
            sliderComponent.render(mouseX, mouseY);
        }

        colourSetting.setColour(new Color(red.getValue() / 255f, green.getValue() / 255f, blue.getValue() / 255f, alpha.getValue() / 255f));
    }

    /**
     * Called when mouse is released
     * @param mouseX The mouse's X
     * @param mouseY The mouse's Y
     * @param mouseButton The mouse button released
     */
    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (SliderComponent sliderComponent : sliders) {
            sliderComponent.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    /**
     * Called when the mouse is clicked
     * @param mouseX The mouse's X
     * @param mouseY The mouse's Y
     * @param mouseButton The button that is clicked
     */
    @Override
    public void whenClicked(int mouseX, int mouseY, int mouseButton) {
        for (SliderComponent sliderComponent : sliders) {
            if (sliderComponent.isMouseOnButton(mouseX, mouseY)) {
                sliderComponent.whenClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    /**
     * Sets the parent window
     * @param parentWindow The new parent window
     */
    public void setParentWindow(Window parentWindow) {
        this.parentWindow = parentWindow;
    }
}
