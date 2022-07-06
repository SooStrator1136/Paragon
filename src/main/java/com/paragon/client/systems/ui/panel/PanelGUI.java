package com.paragon.client.systems.ui.panel;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.impl.client.ClickGUI;
import com.paragon.client.systems.ui.panel.panel.CategoryPanel;
import com.paragon.client.systems.ui.panel.panel.Panel;
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

import static org.lwjgl.opengl.GL11.glColor4f;

public class PanelGUI extends GuiScreen {

    private final List<Panel> panels = new ArrayList<>();

    public PanelGUI() {
        float x = (RenderUtil.getScreenWidth() / 2) - ((Category.values().length * 110) / 2f);

        for (Category category : Category.values()) {
            panels.add(new CategoryPanel(category, x, 30, 105, 22, 22));
            x += 110;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (ClickGUI.darkenBackground.getValue()) {
            drawDefaultBackground();
        }

        if (ClickGUI.background.getValue()) {
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

            bufferbuilder.pos(width, 0, 0).color(topRight[0] / 360, topRight[1] / 360, topRight[2] / 360, 0.5f).endVertex();
            bufferbuilder.pos(0, 0, 0).color(topLeft[0] / 360, topLeft[1] / 360, topLeft[2] / 360, 0.5f).endVertex();
            bufferbuilder.pos(0, height, 0).color(bottomLeft[0] / 360, bottomLeft[1] / 360, bottomLeft[2] / 360, 0.5f).endVertex();
            bufferbuilder.pos(width, height, 0).color(bottomRight[0] / 360, bottomRight[1] / 360, bottomRight[2] / 360, 0.5f).endVertex();

            tessellator.draw();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }

        int dWheel = Mouse.getDWheel();

        Collections.reverse(panels);

        panels.forEach(panel -> {
            panel.render(mouseX, mouseY, dWheel);
        });

        Collections.reverse(panels);

        glColor4f(1, 1, 1, 1);

        if (ClickGUI.catgirl.getValue()) {
            ScaledResolution sr = new ScaledResolution(mc);

            mc.getTextureManager().bindTexture(new ResourceLocation("paragon", "textures/ew.png"));
            RenderUtil.drawModalRectWithCustomSizedTexture(0, sr.getScaledHeight() - 145, 0, 0, 100, 167.777777778f, 100, 167.777777778f);
        }

        Paragon.INSTANCE.getTaskbar().drawTaskbar(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        Collections.reverse(panels);

        panels.forEach(panel -> {
            panel.mouseClicked(mouseX, mouseY, Click.getClick(mouseButton));
        });

        Collections.reverse(panels);

        Paragon.INSTANCE.getTaskbar().mouseClicked(mouseX, mouseY);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        Collections.reverse(panels);

        panels.forEach(panel -> {
            panel.mouseReleased(mouseX, mouseY, Click.getClick(state));
        });

        Collections.reverse(panels);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        Collections.reverse(panels);

        panels.forEach(panel -> {
            panel.keyTyped(keyCode, typedChar);
        });

        Collections.reverse(panels);
    }

    @Override
    public void onGuiClosed() {
        Paragon.INSTANCE.getStorageManager().saveModules("current");
    }

    @Override
    public boolean doesGuiPauseGame() {
        // Pause the game if pause is enabled in the GUI settings
        return ClickGUI.pause.getValue();
    }

}
