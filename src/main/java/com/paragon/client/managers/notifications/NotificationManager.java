package com.paragon.client.managers.notifications;

import com.paragon.api.util.Wrapper;
import net.minecraftforge.common.MinecraftForge;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager implements Wrapper {

    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public NotificationManager() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

}
