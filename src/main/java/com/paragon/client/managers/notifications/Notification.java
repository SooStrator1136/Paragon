package com.paragon.client.managers.notifications;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.hud.impl.Notifications;
import com.paragon.client.systems.ui.animation.Animation;
import com.paragon.client.systems.ui.animation.Easing;
import net.minecraft.util.math.MathHelper;

import static org.lwjgl.opengl.GL11.glScalef;

/**
 * ew.
 */
public class Notification implements TextRenderer {

    private final String message;
    private final NotificationType type;

    private Animation animation;
    private boolean started;
    private boolean reachedFirst = false;
    private int renderTicks = 0;

    public Notification(String message, NotificationType type) {
        this.message = message;
        this.type = type;
    }

    public void render(float y) {
        if (!started) {
            animation = new Animation(() -> 500f, false, () -> Easing.EXPO_IN_OUT);
            animation.setState(true);
            started = true;
        }

        float width = getStringWidth(getMessage()) + 10;
        float x = Notifications.INSTANCE.getX();

        RenderUtil.startGlScissor(Notifications.INSTANCE.getX() + (150 - (150 * animation.getAnimationFactor())), y, 300 * animation.getAnimationFactor(), 45);

        RenderUtil.drawRect((x + 150) - (width / 2f), y, width, 30, 0x90000000);

        renderCenteredString(getMessage(), x + 150, y + 15f, -1, true);

        RenderUtil.drawRect((x + 150) - (width / 2f), y, width, 1, type.getColour());

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

    public String getMessage() {
        return message;
    }

    public Animation getAnimation() {
        return animation;
    }

}
