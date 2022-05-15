package com.paragon.client.managers.notifications;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.ui.animation.Animation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import static org.lwjgl.opengl.GL11.glScalef;

public class Notification implements TextRenderer {

    private final String title;
    private final String message;
    private NotificationType type;

    private final Minecraft mc = Minecraft.getMinecraft();

    private final Animation animation = new Animation(200, false, () -> Animation.Easing.EXPO_IN_OUT);
    private boolean reachedFirst = false;
    private int renderTicks = 0;

    public Notification(String title, String message, NotificationType type) {
        this.title = title;
        this.message = message;
        this.type = type;

        animation.time = 500;
        animation.setState(true);
    }

    public void render() {
        ScaledResolution res = new ScaledResolution(mc);

        RenderUtil.startGlScissor((res.getScaledWidth() / 2f) - 150, 100, 300 * animation.getAnimationFactor(), 45);

        RenderUtil.drawRect((res.getScaledWidth() / 2f) - 150, 100, 300, 45, 0x90000000);

        glScalef(1.5f, 1.5f, 1.5f);
        {
            float scaleFactor = 1 / 1.5f;

            renderText(title, ((res.getScaledWidth() / 2f) - 140) * scaleFactor, 110 * scaleFactor, -1);

            glScalef(scaleFactor, scaleFactor, scaleFactor);
        }

        renderText(message, (res.getScaledWidth() / 2f) - 140, 130, -1);

        RenderUtil.drawRect((res.getScaledWidth() / 2f) - 150, 100, 300, 1, type.getColour());

        RenderUtil.endGlScissor();

        if (animation.getAnimationFactor() == 1 && !reachedFirst) {
            reachedFirst = true;
        }

        if (reachedFirst) {
            renderTicks++;
        }

        if (renderTicks == 400) {
            animation.setState(false);
        }

    }

    public boolean hasFinishedAnimating() {
        return animation.getAnimationFactor() == 0 && reachedFirst;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

}
