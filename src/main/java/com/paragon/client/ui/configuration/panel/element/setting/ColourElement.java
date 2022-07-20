package com.paragon.client.ui.configuration.panel.element.setting;

import com.paragon.api.util.calculations.MathsUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.api.setting.Setting;
import com.paragon.client.ui.util.Click;
import com.paragon.client.ui.configuration.panel.element.Element;
import com.paragon.client.ui.configuration.panel.element.module.ModuleElement;
import com.paragon.client.ui.util.animation.Animation;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public final class ColourElement extends Element {

    private final Setting<Color> setting;

    private final Animation animation = new Animation(ClickGUI.getAnimationSpeed()::getValue, false, ClickGUI.getEasing()::getValue);
    private boolean open;

    private final Setting<Float> hue = new Setting<>("Hue", 0f, 0f, 360f, 1f)
            .setDescription("The hue of the colour");

    private final Setting<Float> alpha = new Setting<>("Alpha", 0f, 0f, 255f, 1f)
            .setDescription("The alpha of the colour");

    private final Setting<Boolean> rainbow = new Setting<>("Rainbow", false)
            .setDescription("Whether the colour is a rainbow");

    private final Setting<Float> rainbowSpeed = new Setting<>("Speed", 4f, 0f, 10f, 0.1f)
            .setDescription("The speed of the rainbow");

    private final Setting<Float> rainbowSaturation = new Setting<>("Saturation", 100f, 0f, 100f, 1f)
            .setDescription("The saturation of the rainbow");

    private final Setting<Boolean> sync = new Setting<>("Sync", false)
            .setDescription("Whether the colour is synced with the client's colour");

    private Color finalColour;
    private boolean dragging = false;

    public ColourElement(int layer, Setting<Color> setting, ModuleElement moduleElement, float x, float y, float width, float height) {
        super(layer, x, y, width, height);

        setParent(moduleElement.getParent());
        this.setting = setting;

        float[] hsbColour = Color.RGBtoHSB(setting.getValue().getRed(), setting.getValue().getGreen(), setting.getValue().getBlue(), null);

        hue.setValue((float) ((int) (hsbColour[0] * 360f)));
        alpha.setValue((float) setting.getValue().getAlpha());
        rainbow.setValue(setting.isRainbow());
        rainbowSpeed.setValue(setting.getRainbowSpeed());
        rainbowSaturation.setValue(setting.getRainbowSaturation());
        sync.setValue(setting.isSync());

        List<Setting<?>> settings = new ArrayList<>();
        settings.add(hue);
        settings.add(alpha);
        settings.add(rainbow);
        settings.add(rainbowSpeed);
        settings.add(rainbowSaturation);
        settings.add(sync);

        // I hate this btw
        for (Setting<?> setting1 : settings) {
            if (setting1.getValue() instanceof Boolean) {
                getSubElements().add(new BooleanElement(layer + 1, (Setting<Boolean>) setting1, moduleElement, getX(), getY(), getWidth(), height));
            } else if (setting1.getValue() instanceof Number) {
                getSubElements().add(new SliderElement(layer + 1, (Setting<Number>) setting1, moduleElement, getX(), getY(), getWidth(), height));
            }
        }

        finalColour = setting.getValue();
    }

    @Override
    public void render(int mouseX, int mouseY, int dWheel) {
        if (setting.isVisible()) {
            getHover().setState(isHovered(mouseX, mouseY));

            RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), new Color(40, 40, 45).getRGB());
            RenderUtil.drawRect(getX() + getLayer(), getY(), getWidth() - getLayer() * 2, getHeight(), new Color((int) (40 + (30 * getHover().getAnimationFactor())), (int) (40 + (30 * getHover().getAnimationFactor())), (int) (45 + (30 * getHover().getAnimationFactor()))).getRGB());
            RenderUtil.drawRect(getX() + getLayer(), getY(), (float) MathHelper.clamp(((getWidth() - getLayer() * 2) * animation.getAnimationFactor()), 1, getWidth()), getHeight(), getSetting().getValue().getRGB());

            renderText(setting.getName(), getX() + (getLayer() * 2) + 5, getY() + getHeight() / 2 - 3.5f, 0xFFFFFFFF);

            // ???
            // why doesnt it stop dragging when mouseReleased is called
            if (!Mouse.isButtonDown(0)) {
                dragging = false;
            }

            if (animation.getAnimationFactor() > 0) {
                float offset = getY() + getHeight();
                for (Element subElement : getSubElements()) {
                    subElement.setX(getX());
                    subElement.setY(offset);

                    subElement.render(mouseX, mouseY, dWheel);

                    offset += subElement.getTotalHeight();
                }

                setting.setAlpha(this.alpha.getValue());
                setting.setRainbow(this.rainbow.getValue());
                setting.setRainbowSaturation(this.rainbowSaturation.getValue());
                setting.setRainbowSpeed(this.rainbowSpeed.getValue());
                setting.setSync(this.sync.getValue());

                float hue = this.hue.getValue();

                float x = getX() + getLayer() + 2;
                float y = getY() + getHeight() + getSubElementsHeight() + 3;
                float dimension = getWidth() - 6;

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
                bufferbuilder.pos(x, y + dimension + 2, 0).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(x + dimension, y + dimension + 2, 0).color(0, 0, 0, 255).endVertex();

                // Draw rect
                tessellator.draw();

                // GL shit pt 2
                GlStateManager.shadeModel(7424);
                GlStateManager.enableAlpha();
                GlStateManager.enableTexture2D();
                GlStateManager.popMatrix();

                RenderUtil.drawBorder(x, y, dimension, dimension + 2, 0.5f, -1);

                // awful thing to check if we are dragging the hue slider
                for (Element settingComponent : getSubElements()) {
                    if (settingComponent instanceof SliderElement && ((SliderElement) settingComponent).isDragging()) {
                        hue = this.hue.getValue();

                        float[] hsb2 = Color.RGBtoHSB(finalColour.getRed(), finalColour.getGreen(), finalColour.getBlue(), null);
                        finalColour = new Color(Color.HSBtoRGB(hue / 360, hsb2[1], hsb2[2]));
                    }

                    // If we are dragging a slider, we don't want to pick a colour
                    if (settingComponent instanceof SliderElement && ((SliderElement) settingComponent).isDragging()) {
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

            getSetting().setValue(finalColour);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, Click click) {
        if (setting.isVisible()) {
            if (isHovered(mouseX, mouseY) && getParent().isElementVisible(this) && click.equals(Click.RIGHT)) {
                open = !open;
                animation.setState(open);
            }

            float x = getX() + 1;
            float y = getY() + getHeight() + getSubElementsHeight();
            float dimension = getWidth() - 6;

            if (isHovered(x, y, dimension, dimension, mouseX, mouseY)) {
                dragging = true;
            }

            if (open) {
                getSubElements().forEach(subelement -> subelement.mouseClicked(mouseX, mouseY, click));
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, Click click) {
        if (setting.isVisible()) {
            getSubElements().forEach(subelement -> subelement.mouseReleased(mouseX, mouseY, click));

            super.mouseReleased(mouseX, mouseY, click);
        }
    }

    @Override
    public void keyTyped(int keyCode, char keyChar) {
        if (setting.isVisible()) {
            super.keyTyped(keyCode, keyChar);
        }
    }

    @Override
    public float getHeight() {
        return getSetting().isVisible() ? super.getHeight() : 0;
    }

    @Override
    public float getSubElementsHeight() {
        return getSetting().isVisible() ? super.getSubElementsHeight() : 0;
    }

    @Override
    public float getTotalHeight() {
        return getSetting().isVisible() ? (float) (getHeight() + ((getSubElementsHeight() + 112) * animation.getAnimationFactor())) : 0;
    }

    public Setting<Color> getSetting() {
        return setting;
    }

}
