package com.paragon.client.systems.module.hud.impl;

import com.paragon.Paragon;
import com.paragon.api.setting.Setting;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.render.font.FontUtil;
import com.paragon.client.managers.notifications.Notification;
import com.paragon.client.systems.module.hud.HUDEditorGUI;
import com.paragon.client.systems.module.hud.HUDModule;

import java.awt.*;

public class Notifications extends HUDModule {

    public static Notifications INSTANCE;

    private static final Setting<RenderType> renderType = new Setting<>("Render Type", RenderType.DISPLAY)
            .setDescription("The way to render the notifications");

    private static final Setting<Direction> direction = new Setting<>("Direction", Direction.DOWN)
            .setDescription("The vertical direction of the notifications");

    private static final Setting<Float> limit = new Setting<>("Limit", 3f, 1f, 20f, 1f)
            .setDescription("The limit to the amount of notifications displayed")
            .setVisibility(() -> renderType.getValue().equals(RenderType.DISPLAY));

    public Notifications() {
        super("Notifications", "Where the notifications will render");

        INSTANCE = this;
    }

    @Override
    public void render() {
        if (mc.currentScreen instanceof HUDEditorGUI) {
            RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), new Color(23, 23, 23, 200).getRGB());
            FontUtil.drawStringWithShadow("[Notifications]", getX() + 5, getY() + 5, - 1);
        } else {
            if (renderType.getValue().equals(RenderType.DISPLAY)) {
                float y = Notifications.INSTANCE.getY();

                for (Notification notification : Paragon.INSTANCE.getNotificationManager().getNotifications()) {
                    if (Paragon.INSTANCE.getNotificationManager().getNotifications().size() >= limit.getValue() + 1 && Paragon.INSTANCE.getNotificationManager().getNotifications().get(limit.getValue().intValue()) == notification) {
                        break;
                    }

                    notification.render(y);

                    switch (direction.getValue()) {
                        case UP:
                            y += -35 * notification.getAnimation().getAnimationFactor();
                            break;

                        case DOWN:
                            y += 35 * notification.getAnimation().getAnimationFactor();
                            break;
                    }
                }

                // bad code 2: electric boogaloo
                for (int i = 0; i < limit.getValue() && i < Paragon.INSTANCE.getNotificationManager().getNotifications().size() - 1; i++) {
                    if (Paragon.INSTANCE.getNotificationManager().getNotifications().get(i).hasFinishedAnimating()) {
                        Paragon.INSTANCE.getNotificationManager().getNotifications().remove(Paragon.INSTANCE.getNotificationManager().getNotifications().get(i));
                    }
                }
            } else if (renderType.getValue().equals(RenderType.CHAT)) {
                for (Notification notification : Paragon.INSTANCE.getNotificationManager().getNotifications()) {
                    Paragon.INSTANCE.getCommandManager().sendClientMessage(notification.getMessage(), false);
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
        return 30;
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
