package com.paragon.client.systems.ui.panel.impl.setting;

import com.paragon.api.util.render.GuiUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.ui.animation.Animation;
import com.paragon.client.systems.ui.panel.impl.module.ModuleButton;
import com.paragon.client.systems.module.impl.client.GUI;
import com.paragon.client.systems.module.settings.impl.ColourSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author Wolfsurge
 * Could do with pickers...
 */
public class ColourComponent extends SettingComponent {

    private final NumberSetting red;
    private final NumberSetting green;
    private final NumberSetting blue;
    private final NumberSetting alpha;

    private final ArrayList<SliderComponent> sliders = new ArrayList<>();

    private final Animation animation;

    public ColourComponent(ModuleButton moduleButton, ColourSetting setting, float offset, float height) {
        super(moduleButton, setting, offset, height);

        this.red = new NumberSetting("Red", "The red value of the colour", setting.getColour().getRed(), 0, 255, 1);
        this.green = new NumberSetting("Green", "The green value of the colour", setting.getColour().getGreen(), 0, 255, 1);
        this.blue = new NumberSetting("Blue", "The blue value of the colour", setting.getColour().getBlue(), 0, 255, 1);
        this.alpha = new NumberSetting("Alpha", "The alpha (opacity) value of the colour", setting.getColour().getAlpha(), 0, 255, 1);

        // Add sliders
        float off = offset + height;
        sliders.add(new SliderComponent(moduleButton, red, off, height));
        off += height;
        sliders.add(new SliderComponent(moduleButton, green, off, height));
        off += height;
        sliders.add(new SliderComponent(moduleButton, blue, off, height));
        off += height;
        sliders.add(new SliderComponent(moduleButton, alpha, off, height));

        animation = new Animation(100, false);
    }

    @Override
    public void renderSetting(int mouseX, int mouseY) {
        // Set animation speed
        animation.time = GUI.animationSpeed.getValue();

        RenderUtil.drawRect(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getWidth(), getHeight(), GuiUtil.mouseOver(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth(), getModuleButton().getOffset() + getOffset() + 12, mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());

        GL11.glPushMatrix();
        GL11.glScalef(0.7f, 0.7f, 0.7f);
        float scaleFactor = 1 / 0.7f;
        renderText(getSetting().getName(), (getModuleButton().getPanel().getX() + 4) * scaleFactor, (getModuleButton().getOffset() + getOffset() + 4) * scaleFactor, ((ColourSetting) getSetting()).getColour().getRGB());
        GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("...", (getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth() - 6.5f) * 2, (getModuleButton().getOffset() + getOffset() + 3.5f) * 2, -1);
        GL11.glPopMatrix();

        // Refresh offsets of sliders
        float off = getOffset() + 12;
        for (SliderComponent sliderComponent : sliders) {
            sliderComponent.setOffset(off);
            off += 12 * GUI.animation.getCurrentMode().getAnimationFactor(animation.getAnimationFactor());
        }

        // Draw picker if expanded
        if (isExpanded()) {
            sliders.forEach(sliderComponent -> {
                sliderComponent.renderSetting(mouseX, mouseY);
            });

            ((ColourSetting) getSetting()).setColour(new Color(red.getValue() / 255f, green.getValue() / 255f, blue.getValue() / 255f, alpha.getValue() / 255f));
        }

        super.renderSetting(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (GuiUtil.mouseOver(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth(), getModuleButton().getOffset() + getOffset() + 12, mouseX, mouseY)) {
            // Toggle open state
            animation.setState(!isExpanded());
        }

        if (isExpanded()) {
            sliders.forEach(sliderComponent -> {
                sliderComponent.mouseClicked(mouseX, mouseY, mouseButton);
            });
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (isExpanded()) {
            sliders.forEach(sliderComponent -> {
                sliderComponent.mouseReleased(mouseX, mouseY, mouseButton);
            });
        }

        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public float getHeight() {
        return 12 + (48 * animation.getAnimationFactor());
    }

    @Override
    public float getAbsoluteHeight() {
        return getHeight();
    }

    @Override
    public boolean isExpanded() {
        return animation.getAnimationFactor() > 0;
    }
}
