package com.paragon.client.ui.configuration.old.impl.setting;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.setting.Setting;
import com.paragon.api.util.calculations.MathsUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.font.FontUtil;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.client.ui.configuration.old.OldPanelGUI;
import com.paragon.client.ui.configuration.old.impl.module.ModuleButton;
import me.surge.animation.Animation;
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
public class ColourComponent extends SettingComponent<Color> {

    private final Setting<Float> hue;
    private final Setting<Float> alpha;
    private final Setting<Boolean> rainbow;
    private final Setting<Float> rainbowSpeed;
    private final Setting<Float> rainbowSaturation;
    private final Setting<Boolean> sync;
    private final List<SettingComponent<?>> components = new ArrayList<>();
    private final Animation animation = new Animation(ClickGUI.getAnimationSpeed()::getValue, false, ClickGUI.getEasing()::getValue);
    private Color finalColour;
    private boolean dragging = false;

    public ColourComponent(ModuleButton moduleButton, Setting<Color> setting, float offset, float height) {
        super(moduleButton, setting, offset, height);

        float[] hsbColour = Color.RGBtoHSB(setting.getValue().getRed(), setting.getValue().getGreen(), setting.getValue().getBlue(), null);

        this.hue = new Setting<>("Hue", (float) ((int) (hsbColour[0] * 360f)), 0f, 360f, 1f)
                .setDescription("The hue of the colour");

        this.alpha = new Setting<>("Alpha", (float) setting.getValue().getAlpha(), 0f, 255f, 1f)
                .setDescription("The alpha of the colour");

        this.rainbow = new Setting<>("Rainbow", setting.isRainbow())
                .setDescription("Whether the colour is a rainbow");

        this.rainbowSpeed = new Setting<>("Rainbow Speed", setting.getRainbowSpeed(), 0.1f, 10f, 0.1f)
                .setDescription("The speed of the rainbow");

        this.rainbowSaturation = new Setting<>("Rainbow Saturation", setting.getRainbowSaturation(), 0f, 100f, 1f)
                .setDescription("The saturation of the rainbow");

        this.sync = new Setting<>("Sync", setting.isSync())
                .setDescription("Whether the colour is synced to the client's main colour");

        List<Setting<?>> settings = new ArrayList<>();
        settings.add(hue);
        settings.add(alpha);
        settings.add(rainbow);
        settings.add(rainbowSpeed);
        settings.add(rainbowSaturation);
        settings.add(sync);

        // I hate this btw
        float count = 2;
        for (Setting<?> setting1 : settings) {
            if (setting1.getValue() instanceof Boolean) {
                components.add(new BooleanComponent(moduleButton, (Setting<Boolean>) setting1, offset + (height * count), height));
            } else if (setting1.getValue() instanceof Float || setting1.getValue() instanceof Double) {
                components.add(new SliderComponent(moduleButton, (Setting<Number>) setting1, offset + (height * count), height));
            }

            count++;
        }

        finalColour = setting.getValue();
    }

    @Override
    public void renderSetting(int mouseX, int mouseY) {

        RenderUtil.drawRect(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getWidth(), getHeight(), OldPanelGUI.isInside(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth(), getModuleButton().getOffset() + getOffset() + 13, mouseX, mouseY) ? new Color(23, 23, 23).brighter().getRGB() : new Color(23, 23, 23).getRGB());

        GL11.glPushMatrix();
        GL11.glScalef(0.65f, 0.65f, 0.65f);
        float scaleFactor = 1 / 0.65f;

        FontUtil.drawStringWithShadow(getSetting().getName(), (getModuleButton().getPanel().getX() + 5) * scaleFactor, (getModuleButton().getOffset() + getOffset() + 4.5f) * scaleFactor, -1);

        GL11.glScalef(scaleFactor, scaleFactor, scaleFactor);
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("...", (getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth() - 6.5f) * 2, (getModuleButton().getOffset() + getOffset() + 3.5f) * 2, -1);
        GL11.glPopMatrix();

        RenderUtil.drawBorder(getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth() - 20, getModuleButton().getOffset() + getOffset() + 2.5f, 8, 8, 0.5f, -1);
        RenderUtil.drawRect(getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth() - 20, getModuleButton().getOffset() + getOffset() + 2.5f, 8, 8, getSetting().getValue().getRGB());

        float off = getOffset() + 13;
        for (SettingComponent<?> settingComponent : components) {
            settingComponent.setOffset(off);
            off += 13;
        }

        // ???
        // why doesnt it stop dragging when mouseReleased is called
        if (!Mouse.isButtonDown(0)) {
            dragging = false;
        }

        if (isExpanded()) {
            // Render sliders
            components.forEach(settingComponent -> settingComponent.renderSetting(mouseX, mouseY));

            getSetting().setAlpha(this.alpha.getValue());
            getSetting().setRainbow(this.rainbow.getValue());
            getSetting().setRainbowSaturation(this.rainbowSaturation.getValue());
            getSetting().setRainbowSpeed(this.rainbowSpeed.getValue());
            getSetting().setSync(this.sync.getValue());

            float hue = this.hue.getValue();

            float x = getModuleButton().getPanel().getX() + 4;
            float y = getModuleButton().getOffset() + getOffset() + (components.size() * 13) + 15.5f;
            float dimension = 87;
            float height = dimension * (float) animation.getAnimationFactor();

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
            for (SettingComponent<?> settingComponent : components) {
                if (settingComponent.getSetting() == this.hue && ((SliderComponent) settingComponent).isDragging()) {
                    hue = ((Number) settingComponent.getSetting().getValue()).floatValue();

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
                    saturation = (float) MathsUtil.roundDouble(((satDiff / dimension) * 100), 0);
                }

                float brightDiff = Math.min(height, Math.max(0, y + height - mouseY));

                if (brightDiff == 0) {
                    brightness = 0;
                } else {
                    brightness = (float) MathsUtil.roundDouble(((brightDiff / height) * 100), 0);
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
        getSetting().setValue(ColourUtil.integrateAlpha(finalColour, alpha.getValue()));

        super.renderSetting(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (OldPanelGUI.isInside(getModuleButton().getPanel().getX(), getModuleButton().getOffset() + getOffset(), getModuleButton().getPanel().getX() + getModuleButton().getPanel().getWidth(), getModuleButton().getOffset() + getOffset() + 13, mouseX, mouseY)) {
            // Toggle open state
            animation.setState(!isExpanded());
        }

        float x = getModuleButton().getPanel().getX() + 4;
        float y = getModuleButton().getOffset() + getOffset() + (components.size() * 13) + 15.5f;
        float dimension = 87;

        if (OldPanelGUI.isInside(x, y, x + dimension, y + dimension, mouseX, mouseY)) {
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
        return (float) (13 + (((components.size() * 13) + 93.5f) * animation.getAnimationFactor()));
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
