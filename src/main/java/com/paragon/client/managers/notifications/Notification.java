package com.paragon.client.managers.notifications;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.hud.impl.Notifications;
import com.paragon.client.systems.ui.animation.Animation;
import static org.lwjgl.opengl.GL11.glScalef;

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

    public void render() {
        if (!started) {
            animation = new Animation(500, false, () -> Animation.Easing.EXPO_IN_OUT);
            animation.setState(true);
            started = true;
        }

        RenderUtil.startGlScissor(Notifications.INSTANCE.getX(), Notifications.INSTANCE.getY(), 300 * animation.getAnimationFactor(), 45);

        RenderUtil.drawRect(Notifications.INSTANCE.getX(), Notifications.INSTANCE.getY(), 300, 45, 0x90000000);

        glScalef(1.5f, 1.5f, 1.5f);
        {
            float scaleFactor = 1 / 1.5f;

            renderText(getTitle(), (Notifications.INSTANCE.getX() + 10) * scaleFactor, (Notifications.INSTANCE.getY() + 10) * scaleFactor, -1);

            glScalef(scaleFactor, scaleFactor, scaleFactor);
        }

        renderText(message, Notifications.INSTANCE.getX() + 10, Notifications.INSTANCE.getY() + 30, -1);

        RenderUtil.drawRect(Notifications.INSTANCE.getX(), Notifications.INSTANCE.getY(), 300, 1, type.getColour());

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
