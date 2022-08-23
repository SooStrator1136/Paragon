package com.paragon.client.systems.module.impl.misc

import com.paragon.Paragon
import com.paragon.api.event.client.ModuleToggleEvent
import com.paragon.api.event.combat.PlayerDeathEvent
import com.paragon.api.event.combat.TotemPopEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.client.managers.notifications.Notification
import com.paragon.client.managers.notifications.NotificationType
import com.paragon.client.systems.module.hud.HUDModule

/**
 * @author Surge
 */
object Notifier : Module("Notifier", Category.MISC, "Notifies you when events happen") {

    private val moduleEnabled = Setting(
        "ModuleToggle",
        false
    ) describedBy "Notifies you when you toggle a module"

    private val pop = Setting(
        "Pop",
        true
    ) describedBy "Notifies you when a player pops a totem"

    private val death = Setting(
        "Death",
        true
    ) describedBy "Notifies you when a player dies"

    private val noPops = Setting(
        "NoPops",
        true
    ) describedBy "Notifies you even if the player hasn't popped any totems" subOf death

    @Listener
    fun onModuleToggle(moduleToggleEvent: ModuleToggleEvent) {
        if (!moduleEnabled.value || !(!moduleToggleEvent.module.isIgnored && moduleToggleEvent.module !is HUDModule)) {
            return
        }

        Paragon.INSTANCE.notificationManager.addNotification(
            Notification(
                moduleToggleEvent.module.name + " was " + if (moduleToggleEvent.module.isEnabled) "Enabled" else "Disabled",
                NotificationType.INFO
            )
        )
    }

    @Listener
    fun onTotemPop(event: TotemPopEvent) {
        if (!pop.value) {
            return
        }

        Paragon.INSTANCE.notificationManager.addNotification(
            Notification(
                event.player.name + " has popped " + Paragon.INSTANCE.popManager.getPops(
                    event.player
                ) + " totems!", NotificationType.INFO
            )
        )
    }

    @Listener
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (!death.value || (!noPops.value && event.pops == 0)) {
            return
        }
        Paragon.INSTANCE.notificationManager.addNotification(
            Notification(
                event.entityPlayer.name + " has died after popping " + event.pops + " totems!",
                NotificationType.INFO
            )
        )
    }

}