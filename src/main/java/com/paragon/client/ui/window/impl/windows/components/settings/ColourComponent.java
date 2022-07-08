package com.paragon.client.ui.window.impl.windows.components.settings;

import com.paragon.api.util.calculations.MathsUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.setting.Setting;
import com.paragon.client.ui.window.impl.Window;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ColourComponent extends SettingComponent<Color> {

    private boolean expanded;

    private final Setting<Float> hue;
    private final Setting<Float> alpha;
    private final Setting<Boolean> rainbow;
    private final Setting<Float> rainbowSpeed;
    private final Setting<Float> rainbowSaturation;
    private final Setting<Boolean> sync;

    private Color finalColour;
    private final List<SettingComponent<?>> components = new ArrayList<>();
    private boolean dragging = false;

    public ColourComponent(Window window, Setting<Color> setting, float x, float y, float width, float height) {
        super(window, setting, x, y, width, height);

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
        settings.add(sync);
        settings.add(rainbow);
        settings.add(rainbowSpeed);
        settings.add(rainbowSaturation);

        // I hate this btw
        for (Setting<?> setting1 : settings) {
            if (setting1.getValue() instanceof Boolean) {
                components.add(new BooleanComponent(window, (Setting<Boolean>) setting1, 0, 0, 140, 15));
            }

            else if (setting1.getValue() instanceof Float || setting1.getValue() instanceof Double) {
                components.add(new SliderComponent(window, (Setting<Number>) setting1, 0, 0, 140, 30));
            }
        }

        finalColour = setting.getValue();
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), 0x90000000);
        renderText(getSetting().getName(), getX() + 4, getY() + 4, -1);

        if (!Mouse.isButtonDown(0)) {
            dragging = false;
        }

        if (expanded) {
            getSetting().setAlpha(this.alpha.getValue());
            getSetting().setRainbow(this.rainbow.getValue());
            getSetting().setRainbowSaturation(this.rainbowSaturation.getValue());
            getSetting().setRainbowSpeed(this.rainbowSpeed.getValue());
            getSetting().setSync(this.sync.getValue());

            float hue = this.hue.getValue();

            float x = getX() + 2;
            float y = getY() + 19;
            float dimension = 100;

            float compX = x + dimension + 4;
            float compY = y + 10;

            int count = 0;
            for (SettingComponent<?> component : components) {
                component.setX(compX);
                component.setY(compY);

                component.drawComponent(mouseX, mouseY);

                compY += component.getTotalHeight() + 2;

                count++;

                if (count % 3 == 0) {
                    compX += 145;
                    compY = y + 10;
                }
            }

            Color colour = Color.getHSBColor(hue / 360, 1, 1);

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
            bufferbuilder.pos(x, y + dimension, 0).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(x + dimension, y + dimension, 0).color(0, 0, 0, 255).endVertex();

            // Draw rect
            tessellator.draw();

            // GL shit pt 2
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();

            RenderUtil.drawBorder(x, y, dimension, dimension, 0.5f, -1);

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

                float brightDiff = Math.min(dimension, Math.max(0, y + dimension - mouseY));

                if (brightDiff == 0) {
                    brightness = 0;
                } else {
                    brightness = (float) MathsUtil.roundDouble(((brightDiff / dimension) * 100), 0);
                }

                finalColour = new Color(Color.HSBtoRGB(hue / 360, saturation / 100, brightness / 100));
            }

            // Get final HSB colours
            float[] finHSB = Color.RGBtoHSB(finalColour.getRed(), finalColour.getGreen(), finalColour.getBlue(), null);

            // Picker X and Y
            float pickerX = x + (finHSB[1]) * dimension;
            float pickerY = y + (1 - (finHSB[2])) * dimension;

            // Draw picker highlight
            RenderUtil.drawRect(pickerX - 1.5f, pickerY - 1.5f, 3, 3, -1);
            RenderUtil.drawRect(pickerX - 1, pickerY - 1, 2, 2, finalColour.getRGB());
        }

        getSetting().setValue(ColourUtil.integrateAlpha(finalColour, alpha.getValue()));

        super.drawComponent(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && isWithinWindowBounds(getWindow().getY() + 40, getWindow().getY() + getWindow().getHeight())) {
            expanded = !expanded;
        }

        if (expanded) {
            for (SettingComponent<?> settingComponent : components) {
                settingComponent.mouseClicked(mouseX, mouseY, button);
            }

            float x = getX() + 2;
            float y = getY() + 19;

            if (isHovered(x, y, 100, 100, mouseX, mouseY)) {
                dragging = true;
            }
        }
    }

    @Override
    public float getTotalHeight() {
        return expanded ? getHeight() * 8 : getHeight();
    }
}
