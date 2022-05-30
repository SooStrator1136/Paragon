package com.paragon.client.systems.module.hud.impl;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.managers.CommandManager;
import com.paragon.client.managers.notifications.Notification;
import com.paragon.client.systems.module.Constant;
import com.paragon.client.systems.module.hud.HUDEditorGUI;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.misc.Notifier;
import com.paragon.client.systems.module.setting.Setting;

import java.awt.*;

@Constant
public class Notifications extends HUDModule {

    public static Notifications INSTANCE;

    public static Setting<RenderType> renderType = new Setting<>("Render Type", RenderType.DISPLAY)
            .setDescription("The way to render the notifications");

    public static Setting<Direction> direction = new Setting<>("Direction", Direction.DOWN)
            .setDescription("The vertical direction of the notifications");

    public Notifications() {
        super("Notifications", "Where the notifications will render");

        INSTANCE = this;
    }

    @Override
    public void render() {
        if (mc.currentScreen instanceof HUDEditorGUI) {
            RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), new Color(23, 23, 23, 200).getRGB());
            renderText("[Notifications]" , getX() + 5, getY() + 5, -1);
        }

        else {
            if (renderType.getValue().equals(RenderType.DISPLAY)) {
                float y = Notifications.INSTANCE.getY();

                for (Notification notification : Paragon.INSTANCE.getNotificationManager().getNotifications()) {
                    notification.render(y);

                    switch (direction.getValue()) {
                        case UP:
                            y += -50 * notification.getAnimation().getAnimationFactor();
                            break;

                        case DOWN:
                            y += 50 * notification.getAnimation().getAnimationFactor();
                            break;
                    }
                }

                Paragon.INSTANCE.getNotificationManager().getNotifications().removeIf(Notification::hasFinishedAnimating);
            } else if (renderType.getValue().equals(RenderType.CHAT)) {
                for (Notification notification : Paragon.INSTANCE.getNotificationManager().getNotifications()) {
                    CommandManager.sendClientMessage(notification.getMessage(), false);
                }

                Paragon.INSTANCE.getNotificationManager().getNotifications().clear();
            }
        }
    }

    @Override
    public float getWidth() {
        return 300;
    }

    @Override
    public float getHeight() {
        return 45;
    }

    public enum RenderType {
        /**
         * Displays a rect
         */
        DISPLAY,

        /**
         * Sends a chat message
         */
        CHAT
    }

    public enum Direction {
        /**
         * Notification Y will decrease
         */
        UP,

        /**
         * Notification Y will increase
         */
        DOWN
    }
}
