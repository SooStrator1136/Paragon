package com.paragon.client.systems.module.hud.impl;

import com.paragon.Paragon;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.managers.CommandManager;
import com.paragon.client.managers.notifications.Notification;
import com.paragon.client.systems.module.Constant;
import com.paragon.client.systems.module.hud.HUDEditorGUI;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.misc.Notifier;

import java.awt.*;

@Constant
public class Notifications extends HUDModule {

    public static Notifications INSTANCE;

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
            if (!Paragon.INSTANCE.getNotificationManager().getNotifications().isEmpty()) {
                Notification notification = Paragon.INSTANCE.getNotificationManager().getNotifications().get(0);

                if (Notifier.renderType.getValue().equals(Notifier.RenderType.DISPLAY)) {
                    notification.render();

                    if (notification.hasFinishedAnimating()) {
                        Paragon.INSTANCE.getNotificationManager().getNotifications().remove(notification);
                    }
                } else if (Notifier.renderType.getValue().equals(Notifier.RenderType.CHAT)) {
                    CommandManager.sendClientMessage(notification.getMessage(), false);

                    Paragon.INSTANCE.getNotificationManager().getNotifications().remove(notification);
                }
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
}
