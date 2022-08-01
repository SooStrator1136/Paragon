package com.paragon.client.ui.configuration.zeroday;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.module.Category;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.client.ui.configuration.GuiImplementation;
import com.paragon.client.ui.configuration.zeroday.panel.CategoryPanel;
import com.paragon.client.ui.util.Click;
import com.paragon.client.ui.util.animation.Animation;
import com.paragon.client.ui.util.animation.Easing;
import com.paragon.client.ui.configuration.zeroday.panel.Panel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class ZerodayGUI extends GuiImplementation {

    private final List<Panel> panels = new ArrayList<>();

    private final Animation openAnimation = new Animation(() ->
            // Linear is apparently slower than other easings, so we decrease the delay
            ClickGUI.getEasing().getValue().equals(Easing.LINEAR) ? 200f : 500f, false, ClickGUI.getEasing()::getValue);

    public ZerodayGUI() {
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

    public void drawScreen(int mouseX, int mouseY, int mouseDelta) {
        openAnimation.setState(true);

        if (ClickGUI.getGradientBackground().getValue()) {
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

            bufferbuilder.pos(getWidth(), 0, 0).color(topRight[0] / 360, topRight[1] / 360, topRight[2] / 360, (float) (0.5f * openAnimation.getAnimationFactor())).endVertex();
            bufferbuilder.pos(0, 0, 0).color(topLeft[0] / 360, topLeft[1] / 360, topLeft[2] / 360, (float) (0.5f * openAnimation.getAnimationFactor())).endVertex();
            bufferbuilder.pos(0, getHeight(), 0).color(bottomLeft[0] / 360, bottomLeft[1] / 360, bottomLeft[2] / 360, (float) (0.5f * openAnimation.getAnimationFactor())).endVertex();
            bufferbuilder.pos(getWidth(), getHeight(), 0).color(bottomRight[0] / 360, bottomRight[1] / 360, bottomRight[2] / 360, (float) (0.5f * openAnimation.getAnimationFactor())).endVertex();

            tessellator.draw();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }

        glPushMatrix();

        // pop out
        glScaled(openAnimation.getAnimationFactor(), openAnimation.getAnimationFactor(), 1);
        glTranslated((getWidth() / 2f) * (1 - openAnimation.getAnimationFactor()), (getHeight() / 2f) * (1 - openAnimation.getAnimationFactor()), 0);

        Collections.reverse(panels);

        // grr lambdas
        String[] tooltip = {""};
        panels.forEach(panel -> {
            panel.render(mouseX, mouseY, mouseDelta);

            if (panel instanceof CategoryPanel && tooltip[0].isEmpty()) {
                if (((CategoryPanel) panel).getTooltip() != "") {
                    tooltip[0] = ((CategoryPanel) panel).getTooltip();
                }
            }
        });

        Collections.reverse(panels);

        glColor4f(1, 1, 1, 1);
        glPopMatrix();
        glPushMatrix();
        glTranslated(0, 24 - (24 * openAnimation.getAnimationFactor()), 0);

        Paragon.INSTANCE.getTaskbar().setTooltip(tooltip[0]);

        glPopMatrix();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        Collections.reverse(panels);

        panels.forEach(panel -> panel.mouseClicked(mouseX, mouseY, Click.getClick(mouseButton)));

        Collections.reverse(panels);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        Collections.reverse(panels);

        panels.forEach(panel -> panel.mouseReleased(mouseX, mouseY, Click.getClick(state)));

        Collections.reverse(panels);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
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
