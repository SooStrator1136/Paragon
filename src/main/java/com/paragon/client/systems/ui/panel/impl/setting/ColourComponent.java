package com.paragon.client.systems.ui.panel.impl.setting;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.util.calculations.MathUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.GuiUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.ui.animation.Animation;
import com.paragon.client.systems.ui.panel.impl.module.ModuleButton;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.client.systems.module.settings.impl.ColourSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wolfsurge
 */
public class ColourComponent extends SettingComponent {

    private final NumberSetting hue;
    private final NumberSetting alpha;
    private final BooleanSetting rainbow;
    private final NumberSetting rainbowSpeed;
    private final NumberSetting rainbowSaturation;
    private final BooleanSetting sync;

    private Color finalColour;
    private final List<SettingComponent> components = new ArrayList<>();
    private final Animation animation = new Animation(200, false);
    private boolean dragging = false;

    public ColourComponent(ModuleButton moduleButton, ColourSetting setting, float offset, float height) {
        super(moduleButton, setting, offset, height);

        float[] hsbColour = Color.RGBtoHSB(setting.getColour().getRed(), setting.getColour().getGreen(), setting.getColour().getBlue(), null);

        this.hue = new NumberSetting("Hue", "The hue of the colour", hsbColour[0] * 360, 0, 360, 1);
        this.alpha = new NumberSetting("Alpha", "The alpha (transparency) of the colour", setting.getColour().getAlpha(), 0, 255, 1);
        this.rainbow = new BooleanSetting("Rainbow", "Use a rainbow effect", setting.isRainbow());
        this.rainbowSpeed = new NumberSetting("Rainbow Speed", "The speed of the rainbow effect", setting.getRainbowSpeed(), 0.1f, 10, 0.1f);
        this.rainbowSaturation = new NumberSetting("Rainbow Saturation", "The saturation of the rainbow effect", setting.getRainbowSaturation(), 0, 100, 1);
        this.sync = new BooleanSetting("Sync", "Sync the colour to the client's colour", setting.isSync());

        components.add(new SliderComponent(moduleButton, hue, offset + (height * 2), height));
        components.add(new SliderComponent(moduleButton, alpha, offset + (height * 3), height));
        components.add(new BooleanComponent(moduleButton, rainbow, offset + (height * 4), height));
        components.add(new SliderComponent(moduleButton, rainbowSpeed, offset + (height * 5), height));
        components.add(new SliderComponent(moduleButton, rainbowSaturation, offset + (height * 6), height));
        components.add(new BooleanComponent(moduleButton, sync, offset + (height * 8), height));

        finalColour = setting.getColour();
    }

    @Override
    public void renderSetting(int mouseX, int mouseY) {
        this.animation.time = ClickGUI.animationSpeed.getValue();

        RenderUtil.drawRect(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getWidth(), getHeight(), GuiUtil.mouseOver(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth(), getModuleButton().getOffset() + getOffset() + 12, mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());

        GL11.glPushMatrix();
        GL11.glScalef(0.65f, 0.65f, 0.65f);
        float scaleFactor = 1 / 0.65f;
        renderText(getSetting().getName(), (getModuleButton().getPanel().getX() + 5) * scaleFactor, (getModuleButton().getOffset() + getOffset() + 4) * scaleFactor, -1);
        GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("...", (getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth() - 6.5f) * 2, (getModuleButton().getOffset() + getOffset() + 3.5f) * 2, -1);
        GL11.glPopMatrix();

        RenderUtil.drawBorder(getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth() - 20, getModuleButton().getOffset() + getOffset() + 2, 8, 8, 0.5f, -1);
        RenderUtil.drawRect(getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth() - 20, getModuleButton().getOffset() + getOffset() + 2, 8, 8, ((ColourSetting) getSetting()).getColour().getRGB());

        float off = getOffset() + 12;
        for (SettingComponent settingComponent : components) {
            settingComponent.setOffset(off);
            off += 12;
        }

        // ???
        // why doesnt it stop dragging when mouseReleased is called
        if (!Mouse.isButtonDown(0)) {
            dragging = false;
        }

        if (isExpanded()) {
            // Render sliders
            components.forEach(settingComponent -> settingComponent.renderSetting(mouseX, mouseY));

            ((ColourSetting) getSetting()).setRainbow(this.rainbow.isEnabled());
            ((ColourSetting) getSetting()).setRainbowSaturation(this.rainbowSaturation.getValue());
            ((ColourSetting) getSetting()).setRainbowSpeed(this.rainbowSpeed.getValue());
            ((ColourSetting) getSetting()).setSync(this.sync.isEnabled());

            float hue = this.hue.getValue();

            float x = getModuleButton().getPanel().getX() + 4;
            float y = getModuleButton().getOffset() + getOffset() + (components.size() * 12) + 15.5f;
            float dimension = 87;
            float height = dimension * ClickGUI.animation.getCurrentMode().getAnimationFactor((float) animation.getAnimationFactor());

            Color colour = Color.getHSBColor(hue / 360, 1, 1);

            // Background
            RenderUtil.drawRect(getModuleButton().getPanel().getX(), y - 3.5f, getModuleButton().getPanel().getWidth(), height + 7.5f, new Color(23, 23, 23).getRGB());

            // GL shit pt 1
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.shadeModel(7425);

            // Get tessellator and buffer builder
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();

            // Add positions
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x + dimension, y, 0).color(colour.getRed(), colour.getGreen(), colour.getBlue(), colour.getAlpha()).endVertex();
            bufferbuilder.pos(x, y, 0).color(255, 255, 255, 255).endVertex();
            bufferbuilder.pos(x, y + height, 0).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(x + dimension, y + height, 0).color(0, 0, 0, 255).endVertex();

            // Draw rect
            tessellator.draw();

            // GL shit pt 2
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();

            RenderUtil.drawBorder(x, y, dimension, height, 0.5f, -1);

            // awful thing to check if we are dragging the hue slider
            for (SettingComponent settingComponent : components) {
                if (settingComponent.getSetting() == this.hue && ((SliderComponent) settingComponent).isDragging()) {
                    hue = ((NumberSetting) settingComponent.getSetting()).getValue();
                    float[] hsb2 = Color.RGBtoHSB(finalColour.getRed(), finalColour.getGreen(), finalColour.getBlue(), null);
                    finalColour = new Color(Color.HSBtoRGB(hue / 360, hsb2[1], hsb2[2]));
                }

                // If we are dragging a slider, we don't want to pick a colour
                if (settingComponent instanceof SliderComponent && ((SliderComponent) settingComponent).isDragging()) {
                    dragging = false;
                }
            }

            // Check we are dragging
            if (dragging) {
                float saturation;
                float brightness;

                float satDiff = Math.min(dimension, Math.max(0, mouseX - x));

                if (satDiff == 0) {
                    saturation = 0;
                } else {
                    saturation = (float) MathUtil.roundDouble(((satDiff / dimension) * 100), 0);
                }

                float brightDiff = Math.min(height, Math.max(0, y + height - mouseY));

                if (brightDiff == 0) {
                    brightness = 0;
                } else {
                    brightness = (float) MathUtil.roundDouble(((brightDiff / height) * 100), 0);
                }

                finalColour = new Color(Color.HSBtoRGB(hue / 360, saturation / 100, brightness / 100));
            }

            // Get final HSB colours
            float[] finHSB = Color.RGBtoHSB(finalColour.getRed(), finalColour.getGreen(), finalColour.getBlue(), null);

            // Picker X and Y
            float pickerX = x + (finHSB[1]) * dimension;
            float pickerY = y + (1 - (finHSB[2])) * height;

            // Draw picker highlight

            RenderUtil.drawRect(pickerX - 1.5f, pickerY - 1.5f, 3, 3, -1);
            RenderUtil.drawRect(pickerX - 1, pickerY - 1, 2, 2, finalColour.getRGB());
        }

        // Set final colour
        ((ColourSetting) getSetting()).setColour(ColourUtil.integrateAlpha(finalColour, alpha.getValue()));

        super.renderSetting(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (GuiUtil.mouseOver(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth(), getModuleButton().getOffset() + getOffset() + 12, mouseX, mouseY)) {
            // Toggle open state
            animation.setState(!isExpanded());
        }

        float x = getModuleButton().getPanel().getX() + 4;
        float y = getModuleButton().getOffset() + getOffset() + (components.size() * 12) + 15.5f;
        float dimension = 87;

        if (GuiUtil.mouseOver(x, y, x + dimension, y + dimension, mouseX, mouseY)) {
            dragging = true;
        }

        if (isExpanded()) {
            components.forEach(settingComponent -> {
                settingComponent.mouseClicked(mouseX, mouseY, mouseButton);

                SettingUpdateEvent settingUpdateEvent = new SettingUpdateEvent(getSetting());
                Paragon.INSTANCE.getEventBus().post(settingUpdateEvent);
            });
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        dragging = false;

        if (isExpanded()) {
            components.forEach(settingComponent -> {
                settingComponent.mouseReleased(mouseX, mouseY, mouseButton);
            });
        }

        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public float getHeight() {
        return (float) (12 + (((components.size() * 12) + 94.5f) * animation.getAnimationFactor()));
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
