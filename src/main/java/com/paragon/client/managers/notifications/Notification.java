package com.paragon.client.managers.notifications;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.hud.impl.Notifications;
import com.paragon.client.systems.ui.animation.Animation;
import com.paragon.client.systems.ui.animation.Easing;

import static org.lwjgl.opengl.GL11.glScalef;

/**
 * ew.
 */
public class Notification implements TextRenderer {

    private final String title;
    private final String message;
    private NotificationType type;

    private Animation animation;
    private boolean started;
    private boolean reachedFirst = false;
    private int renderTicks = 0;

    public Notification(String title, String message, NotificationType type) {
        this.title = title;
        this.message = message;
        this.type = type;
    }

    public void render(float y) {
        if (!started) {
            animation = new Animation(500, false, () -> Easing.EXPO_IN_OUT);
            animation.setState(true);
            started = true;
        }

        RenderUtil.startGlScissor(Notifications.INSTANCE.getX() + (150 - (150 * animation.getAnimationFactor())), y, 300 * animation.getAnimationFactor(), 45);

        RenderUtil.drawRect(Notifications.INSTANCE.getX(), y, 300, 45, 0x90000000);

        glScalef(1.5f, 1.5f, 1.5f);
        {
            float scaleFactor = 1 / 1.5f;

            renderText(getTitle(), (Notifications.INSTANCE.getX() + 10) * scaleFactor, (y + 10) * scaleFactor, -1);

            glScalef(scaleFactor, scaleFactor, scaleFactor);
        }

        renderText(message, Notifications.INSTANCE.getX() + 10, y + 30, -1);

        RenderUtil.drawRect(Notifications.INSTANCE.getX(), y, 300, 1, type.getColour());

        RenderUtil.endGlScissor();

        if (animation.getAnimationFactor() == 1 && !reachedFirst) {
            reachedFirst = true;
        }

        if (reachedFirst) {
            renderTicks++;
        }

        if (renderTicks == 300) {
            animation.setState(false);
        }

    }

    public boolean hasFinishedAnimating() {
        return animation == null || animation.getAnimationFactor() == 0 && reachedFirst;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Animation getAnimation() {
        return animation;
    }

}
