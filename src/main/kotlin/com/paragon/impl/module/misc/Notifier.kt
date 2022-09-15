package com.paragon.impl.module.misc

import com.paragon.Paragon
import com.paragon.impl.event.client.ModuleToggleEvent
import com.paragon.impl.event.combat.PlayerDeathEvent
import com.paragon.impl.event.combat.TotemPopEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.managers.notifications.Notification
import com.paragon.impl.managers.notifications.NotificationType
import com.paragon.impl.module.Category

/**
 * @author Surge
 */
object Notifier : Module("Notifier", Category.MISC, "Notifies you when events happen") {

    private val moduleEnabled = Setting(
        "ModuleToggle", false
    ) describedBy "Notifies you when you toggle a module"

    private val pop = Setting(
        "Pop", true
    ) describedBy "Notifies you when a player pops a totem"

    private val death = Setting(
        "Death", true
    ) describedBy "Notifies you when a player dies"

    private val noPops = Setting(
        "NoPops", true
    ) describedBy "Notifies you even if the player hasn't popped any totems" subOf death

    @Listener
    fun onModuleToggle(moduleToggleEvent: ModuleToggleEvent) {
        if (!moduleEnabled.value || !(!moduleToggleEvent.module.isIgnored && moduleToggleEvent.module !is HUDModule)) {
            return
        }

        Paragon.INSTANCE.notificationManager.addNotification(
            Notification(
                moduleToggleEvent.module.name + " was " + if (moduleToggleEvent.module.isEnabled) "Enabled" else "Disabled", NotificationType.INFO
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
                event.entityPlayer.name + " has died after popping " + event.pops + " totems!", NotificationType.INFO
            )
        )
    }

}