package com.paragon.client.systems.module.impl.client

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.IgnoredByNotifications
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting

/**
 * @author Surge
 */
@IgnoredByNotifications
object DiscordRPC : Module("DiscordRPC", Category.CLIENT, "Changes your Discord presence to reflect the client's current state") {

    val showServer = Setting(
        "Show Server",
        true
    ) describedBy "Show the servers ip in the RPC"

    override fun onEnable() {
        Paragon.INSTANCE.presenceManager.startRPC()
    }

    override fun onTick() {
        Paragon.INSTANCE.presenceManager.updateRPC()
    }

    override fun onDisable() {
        Paragon.INSTANCE.presenceManager.stopRPC()
    }

}