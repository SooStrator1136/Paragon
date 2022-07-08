package com.paragon.client.ui.menu;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.ui.animation.Animation;
import com.paragon.client.ui.animation.Easing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ParagonButton extends GuiButton implements TextRenderer {

    private final Animation animation = new Animation(() -> 300f, false, () -> Easing.EXPO_IN_OUT);

    public ParagonButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    public void drawButton(@NotNull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            if (!(animation.getAnimationFactor() > 0) && this.hovered) {
                animation.setState(true);
            } else if (animation.getAnimationFactor() == 1 && !this.hovered) {
                animation.setState(false);
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            RenderUtil.drawRect(this.x, this.y, this.width, this.height, this.hovered ? 0x90000000 : 0x80000000);
            RenderUtil.drawRect(this.x + ((width / 2f) - ((width / 2f) * (float) animation.getAnimationFactor())), this.y + height - 1, (float) (this.width * animation.getAnimationFactor()), 1, new Color(1, 1, 1, (float) MathHelper.clamp(1 * animation.getAnimationFactor(), 0, 1)).getRGB());

            this.mouseDragged(mc, mouseX, mouseY);

            renderCenteredString(this.displayString, this.x + this.width / 2f, this.y + ((this.height - 1) / 2f), 0xFFFFFF, true);
        }

    }
}
