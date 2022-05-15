package com.paragon.client.managers.notifications;

import com.paragon.api.util.Wrapper;
import com.paragon.client.managers.CommandManager;
import com.paragon.client.systems.module.impl.misc.Notifier;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager implements Wrapper {

    private final List<Notification> notifications = new ArrayList<>();

    public NotificationManager() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent event) {
        if (!event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            return;
        }

        if (!notifications.isEmpty()) {
            Notification notification = notifications.get(0);

            if (Notifier.renderType.getValue().equals(Notifier.RenderType.DISPLAY)) {
                notification.render();

                if (notification.hasFinishedAnimating()) {
                    notifications.remove(notification);
                }
            } else if (Notifier.renderType.getValue().equals(Notifier.RenderType.CHAT)) {
                CommandManager.sendClientMessage(notification.getMessage(), false);

                notifications.remove(notification);
            }
        }
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);

        if (!Notifier.renderType.getValue().equals(Notifier.RenderType.CHAT)) {
            CommandManager.sendClientMessage(notification.getMessage(), true);
        }
    }

}
