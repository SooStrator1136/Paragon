package com.paragon.client.managers

import com.paragon.api.util.Wrapper
import com.paragon.client.managers.notifications.Notification
import net.minecraftforge.common.MinecraftForge
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Surge
 */
class NotificationManager : Wrapper {

    val notifications: MutableList<Notification> = CopyOnWriteArrayList()

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun addNotification(notification: Notification) {
        notifications.add(notification)
    }

}