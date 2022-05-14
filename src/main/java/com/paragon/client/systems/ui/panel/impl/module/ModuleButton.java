package com.paragon.client.systems.ui.panel.impl.module;

import com.paragon.api.util.render.GuiUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.ui.animation.Animation;
import com.paragon.client.systems.ui.panel.PanelGUI;
import com.paragon.client.systems.ui.panel.impl.Panel;
import com.paragon.client.systems.ui.panel.impl.setting.*;
import com.paragon.client.systems.module.Module;
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
public class ModuleButton implements TextRenderer {

    // Opening / Closing animation
    private final Animation animation;

    // The parent panel
    private final Panel panel;

    // The module
    private final Module module;
    // A list of all setting components
    private final ArrayList<SettingComponent<?>> settingComponents = new ArrayList<>();
    // The offset and height
    public float offset, height;

    public ModuleButton(Panel panel, Module module, float offset, float height) {
        this.panel = panel;
        this.module = module;
        this.offset = offset;
        this.height = height;

        float settingOffset = height;

        // Add settings. Please make a PR if you want to make this look better.
        for (Setting<?> setting : getModule().getSettings()) {
            if (setting.getValue() instanceof Boolean) {
                settingComponents.add(new BooleanComponent(this, (Setting<Boolean>) setting, settingOffset, height));
                settingOffset += height;
            } else if (setting.getValue() instanceof AtomicInteger) {
                settingComponents.add(new KeybindComponent(this, (Setting<AtomicInteger>) setting, settingOffset, height));
                settingOffset += height;
            } else if (setting.getValue() instanceof Number) {
                settingComponents.add(new SliderComponent(this, (Setting<Number>) setting, settingOffset, height));
                settingOffset += height;
            } else if (setting.getValue() instanceof Enum<?>) {
                settingComponents.add(new ModeComponent(this, (Setting<Enum<?>>) setting, settingOffset, height));
                settingOffset += height;
            } else if (setting.getValue() instanceof Color) {
                settingComponents.add(new ColourComponent(this, (Setting<Color>) setting, settingOffset, height));
                settingOffset += height;
            }
        }

        animation = new Animation(100, false, () -> ClickGUI.easing.getValue());
    }

    public void renderModuleButton(int mouseX, int mouseY) {
        // Set animation speed
        animation.time = ClickGUI.animationSpeed.getValue();

        // Header
        RenderUtil.drawRect(getPanel().getX(), getOffset(), getPanel().getWidth(), getHeight(), isMouseOver(mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());

        GL11.glPushMatrix();
        // Scale it
        GL11.glScalef(0.8f, 0.8f, 0.8f);

        float scaleFactor = 1.25f;

        // Render the module's name
        renderText(getModule().getName(), (getPanel().getX() + 3) * scaleFactor, (getOffset() + 4f) * scaleFactor, getModule().isEnabled() ? Colours.mainColour.getValue().getRGB() : -1);

        // Render some dots at the side if we have more settings than just the keybind
        if (module.getSettings().size() > 1) {
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("...", (getPanel().getX() + getPanel().getWidth() - Minecraft.getMinecraft().fontRenderer.getStringWidth("...") - 1) * scaleFactor, (getOffset() + 1.5f) * scaleFactor, -1);
        }

        GL11.glPopMatrix();

        // Refresh settings
        refreshSettingOffsets();

        if (animation.getAnimationFactor() > 0) {
            // Render settings
            settingComponents.forEach(settingComponent -> {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.renderSetting(mouseX, mouseY);
                    RenderUtil.drawRect(getPanel().getX(), getOffset() + settingComponent.getOffset(), 1, (settingComponent instanceof ColourComponent ? getHeight() : settingComponent.getHeight()), Colours.mainColour.getValue().getRGB());
                }
            });
        }

        if (isMouseOver(mouseX, mouseY)) {
            PanelGUI.tooltip = module.getDescription();
        }
    }

    /**
     * Check if the mouse is over the module button
     *
     * @param mouseX The mouse's X
     * @param mouseY The mouse's Y
     * @return If the mouse is over the module button
     */
    public boolean isMouseOver(int mouseX, int mouseY) {
        return GuiUtil.mouseOver(getPanel().getX(), getOffset(), getPanel().getX() + getPanel().getWidth(), getOffset() + getHeight(), mouseX, mouseY);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (isMouseOver(mouseX, mouseY)) {
                // Toggle the module
                getModule().toggle();
            }
        } else if (mouseButton == 1) {
            if (isMouseOver(mouseX, mouseY)) {
                // Expand the settings
                animation.setState(!(animation.getAnimationFactor() > 0));
            }
        }

        if (animation.getAnimationFactor() > 0) {
            // Mouse clicked
            for (SettingComponent<?> settingComponent : settingComponents) {
                if (settingComponent.getSetting().isVisible()) {
                    settingComponent.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (SettingComponent<?> settingComponent : settingComponents) {
            if (settingComponent.getSetting().isVisible()) {
                settingComponent.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    public void keyTyped(char keyTyped, int keyCode) {
        for (SettingComponent<?> settingComponent : settingComponents) {
            if (settingComponent.getSetting().isVisible()) {
                settingComponent.keyTyped(keyTyped, keyCode);
            }
        }
    }

    public void refreshSettingOffsets() {
        float settingOffset = height;

        // EW
        for (SettingComponent<?> settingComponent : settingComponents) {
            if (settingComponent.getSetting().isVisible()) {
                settingComponent.setOffset(settingOffset);
                settingOffset += settingComponent.getHeight() * ClickGUI.animation.getValue().getAnimationFactor((float) animation.getAnimationFactor());

                if (settingComponent.animation.getAnimationFactor() > 0) {
                    float subsettingOffset = settingComponent.getOffset() + settingComponent.getHeight();
                    for (SettingComponent<?> settingComponent1 : settingComponent.getSettingComponents()) {
                        if (settingComponent1.getSetting().isVisible()) {
                            settingComponent1.setOffset(subsettingOffset);
                            subsettingOffset += settingComponent1.getHeight() * ClickGUI.animation.getValue().getAnimationFactor((float) settingComponent.animation.getAnimationFactor());
                            settingOffset += settingComponent1.getHeight() * settingComponent.animation.getAnimationFactor();
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the parent panel
     *
     * @return The parent panel
     */
    public Panel getPanel() {
        return panel;
    }

    /**
     * Gets the module
     *
     * @return The module
     */
    public Module getModule() {
        return module;
    }

    /**
     * Gets the offset
     *
     * @return The offset
     */
    public float getOffset() {
        return offset;
    }

    /**
     * Gets the height of the button
     *
     * @return The height
     */
    public float getHeight() {
        return height;
    }

    /**
     * Gets the height of the button and it's settings
     *
     * @return The absolute height
     */
    public float getAbsoluteHeight() {
        float settingHeight = 0;

        for (SettingComponent<?> settingComponent : settingComponents) {
            if (settingComponent.getSetting().isVisible()) {
                settingHeight += settingComponent.getHeight();

                if (settingComponent.animation.getAnimationFactor() > 0) {
                    for (SettingComponent<?> settingComponent1 : settingComponent.getSettingComponents()) {
                        if (settingComponent1.getSetting().isVisible()) {
                            settingHeight += settingComponent1.getHeight() * settingComponent.animation.getAnimationFactor();
                        }
                    }
                }
            }
        }

        return (float) (height + (settingHeight * animation.getAnimationFactor()));
    }

    /**
     * Gets whether the component is expanded or not
     *
     * @return Whether the component is expanded or not
     */
    public boolean isExpanded() {
        return animation.getAnimationFactor() > 0;
    }
}
