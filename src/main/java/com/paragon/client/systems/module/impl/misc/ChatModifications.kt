package com.paragon.client.systems.module.impl.misc

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import net.minecraftforge.client.event.ClientChatEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * @author Surge
 */
object ChatModifications : Module("ChatModifications", Category.MISC, "Changes the way you send messages") {

    private val coloured = Setting(
        "Coloured",
        false
    ) describedBy "Adds a '>' before the message"

    private val suffix = Setting(
        "Suffix",
        true
    ) describedBy "Adds a Paragon suffix to the end of the message"

    @SubscribeEvent
    fun onChat(event: ClientChatEvent) {
        if (event.message.startsWith("/") || Paragon.INSTANCE.commandManager.startsWithPrefix(event.message)) {
            return
        }

        if (coloured.value) {
            event.message = "> " + event.message
        }

        if (suffix.value) {
            event.message = event.message + " | Paragon"
        }
    }

}