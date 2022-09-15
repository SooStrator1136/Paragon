package com.paragon.impl.managers

import com.paragon.impl.managers.notifications.Notification
import com.paragon.util.Wrapper
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