package com.paragon.client.systems.ui.menu;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.client.systems.ui.animation.Animation;
import com.paragon.client.systems.ui.animation.Easing;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.glScalef;

/**
 * @author Wolfsurge
 */
public class ParagonMenu extends GuiScreen implements TextRenderer {

    // Credits expand animation
    private final Animation creditsAnimation = new Animation(() -> 500f, false, () -> Easing.EXPO_IN_OUT);

    // Whether the credits are displayed or not
    private boolean creditsExpanded = false;

    @Override
    public void initGui() {
        this.buttonList.add(new ParagonButton(0, this.width / 2 - 100, this.height / 2, 200, 20, "Singleplayer"));
        this.buttonList.add(new ParagonButton(1, this.width / 2 - 100, this.height / 2 + 25, 200, 20, "Multiplayer"));
        this.buttonList.add(new ParagonButton(2, this.width / 2 - 100, this.height / 2 + 50, 95, 20, "Options"));
        this.buttonList.add(new ParagonButton(3, this.width / 2 + 5, this.height / 2 + 50, 95, 20, "Exit"));
        this.buttonList.add(new ParagonButton(4, 3, this.height - 23, 60, 20, "Credits"));

        this.buttonList.add(new ParagonButton(5, this.width - 83, 3, 80, 20, "Minecraft Menu"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Get background offsets
        float xOffset = -1.0f * ((mouseX - this.width / 10.0f) / (this.width / 10.0f));
        float yOffset = -1.0f * ((mouseY - this.height / 10.0f) / (this.height / 10.0f));

        // Draw background
        mc.getTextureManager().bindTexture(new ResourceLocation("paragon", "textures/background.png"));
        RenderUtil.drawModalRectWithCustomSizedTexture(xOffset, yOffset, 0, 0, this.width + 10f, this.height + 10f, this.width + 10f, this.height + 10f);

        // Draw button background
        RenderUtil.drawRoundedRect((width / 2f) - 110, (height / 2f) - 50, 220, 130, 5, 5, 5, 5, 0x80000000);

        // Title
        glScalef(2.5f, 2.5f, 2.5f);
        {
            float scaleFactor = 1 / 2.5f;

            renderCenteredString("Paragon", (width / 2f) * scaleFactor, (height / 2f - 30) * scaleFactor, Colours.mainColour.getValue().getRGB(), true);

            glScalef(scaleFactor, scaleFactor, scaleFactor);
        }

        // Version
        glScalef(0.8f, 0.8f, 0.8f);
        {
            float scaleFactor = 1 / 0.8f;

            renderCenteredString("v" + Paragon.modVersion, (width / 2f) * scaleFactor, (height / 2f - 10) * scaleFactor, 0xFFFFFF, true);

            glScalef(scaleFactor, scaleFactor, scaleFactor);
        }

        // Credits
        RenderUtil.startGlScissor(5, 250, 200 * creditsAnimation.getAnimationFactor(), 300);

        // Rect
        RenderUtil.drawRoundedRect(5, 250, 200 * creditsAnimation.getAnimationFactor(), 60, 5, 5, 5, 5, 0x80000000);

        // Title
        renderText("Credits", 10, 255, -1);

        // Credits
        glScalef(0.65f, 0.65f, 0.65f);
        {
            float scaleFactor = 1 / 0.65f;

            float y = 270;
            for (String str : new String[]{"Created by Wolfsurge & Teletofu", "Animation class - Tigermouthbear, linustouchtips", "Shader OpenGL code - linustouchtips", "Font Renderer - Cosmos Client"}) {
                renderText(str, 10 * scaleFactor, y * scaleFactor, -1);
                y += 10;
            }

            glScalef(scaleFactor, scaleFactor, scaleFactor);
        }

        // End scissor
        RenderUtil.endGlScissor();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(new GuiWorldSelection(this));
                break;

            case 1:
                mc.displayGuiScreen(new GuiMultiplayer(this));
                break;

            case 2:
                mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                break;

            case 3:
                mc.shutdown();
                break;

            case 4:
                creditsExpanded = !creditsExpanded;
                creditsAnimation.setState(creditsExpanded);
                break;

            case 5:
                Paragon.INSTANCE.setParagonMainMenu(false);
                mc.displayGuiScreen(new GuiMainMenu());
                break;
        }

        super.actionPerformed(button);
    }
}
