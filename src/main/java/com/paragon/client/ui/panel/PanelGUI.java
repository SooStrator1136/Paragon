package com.paragon.client.ui.panel;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.module.Category;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.client.ui.animation.Animation;
import com.paragon.client.ui.animation.Easing;
import com.paragon.client.ui.panel.panel.CategoryPanel;
import com.paragon.client.ui.panel.panel.Panel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class PanelGUI extends GuiScreen {

    private final List<Panel> panels = new ArrayList<>();

    private final Animation openAnimation = new Animation(() ->
            // Linear is apparently slower than other easings, so we decrease the delay
            ClickGUI.getEasing().getValue().equals(Easing.LINEAR) ? 200f : 500f, false, ClickGUI.getEasing()::getValue);

    public PanelGUI() {
        float x = (RenderUtil.getScreenWidth() / 2) - ((Category.values().length * 110) / 2f);

        for (Category category : Category.values()) {
            panels.add(new CategoryPanel(category, x, 30, 105, 22, 22));
            x += 110;
        }
    }

    @Override
    public void initGui() {
        openAnimation.setState(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (ClickGUI.getDarkenBackground().getValue()) {
            drawDefaultBackground();
        }

        if (ClickGUI.getBackground().getValue()) {
            float[] topLeft = {182, 66, 245};
            float[] topRight = {236, 66, 245};
            float[] bottomRight = {245, 66, 141};
            float[] bottomLeft = {212, 11, 57};

            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.shadeModel(7425);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

            bufferbuilder.pos(width, 0, 0).color(topRight[0] / 360, topRight[1] / 360, topRight[2] / 360, (float) (0.5f * openAnimation.getAnimationFactor())).endVertex();
            bufferbuilder.pos(0, 0, 0).color(topLeft[0] / 360, topLeft[1] / 360, topLeft[2] / 360, (float) (0.5f * openAnimation.getAnimationFactor())).endVertex();
            bufferbuilder.pos(0, height, 0).color(bottomLeft[0] / 360, bottomLeft[1] / 360, bottomLeft[2] / 360, (float) (0.5f * openAnimation.getAnimationFactor())).endVertex();
            bufferbuilder.pos(width, height, 0).color(bottomRight[0] / 360, bottomRight[1] / 360, bottomRight[2] / 360, (float) (0.5f * openAnimation.getAnimationFactor())).endVertex();

            tessellator.draw();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }

        int dWheel = Mouse.getDWheel();

        glPushMatrix();

        // pop out
        glScaled(openAnimation.getAnimationFactor(), openAnimation.getAnimationFactor(), 1);
        glTranslated((width / 2f) * (1 - openAnimation.getAnimationFactor()), (height / 2f) * (1 - openAnimation.getAnimationFactor()), 0);

        Collections.reverse(panels);

        panels.forEach(panel -> panel.render(mouseX, mouseY, dWheel));

        Collections.reverse(panels);

        glColor4f(1, 1, 1, 1);

        glPopMatrix();

        if (ClickGUI.getCatgirl().getValue()) {
            ScaledResolution sr = new ScaledResolution(mc);

            mc.getTextureManager().bindTexture(new ResourceLocation("paragon", "textures/ew.png"));
            RenderUtil.drawModalRectWithCustomSizedTexture(0, sr.getScaledHeight() - 145, 0, 0, 100, 167.777777778f, 100, 167.777777778f);
        }

        glPushMatrix();
        glTranslated(0, 24 - (24 * openAnimation.getAnimationFactor()), 0);
        Paragon.INSTANCE.getTaskbar().drawTaskbar(mouseX, mouseY);
        glPopMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        Collections.reverse(panels);

        panels.forEach(panel -> panel.mouseClicked(mouseX, mouseY, Click.getClick(mouseButton)));

        Collections.reverse(panels);

        Paragon.INSTANCE.getTaskbar().mouseClicked(mouseX, mouseY);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        Collections.reverse(panels);

        panels.forEach(panel -> panel.mouseReleased(mouseX, mouseY, Click.getClick(state)));

        Collections.reverse(panels);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        Collections.reverse(panels);

        panels.forEach(panel -> panel.keyTyped(keyCode, typedChar));

        Collections.reverse(panels);
    }

    @Override
    public void onGuiClosed() {
        Paragon.INSTANCE.getStorageManager().saveModules("current");
        Paragon.INSTANCE.getStorageManager().saveOther();

        openAnimation.resetToDefault();
    }

    @Override
    public boolean doesGuiPauseGame() {
        // Pause the game if pause is enabled in the GUI settings
        return ClickGUI.getPause().getValue();
    }

    public Animation getAnimation() {
        return openAnimation;
    }

}
