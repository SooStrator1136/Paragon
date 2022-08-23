package com.paragon.client.systems.module.impl.render

import com.paragon.api.event.render.gui.RenderChatEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.bus.listener.Listener
import java.awt.Color

/**
 * @author Surge
 * @since 19/06/22
 */
object ClearChat : Module("ClearChat", Category.RENDER, "Removes the chat background") {

    private val colour = Setting(
        "Colour",
        Color(0, 0, 0, 0)
    ) describedBy "The colour of the chat background"

    @Listener
    fun onRenderChatBackground(event: RenderChatEvent) {
        event.colour = colour.value.rgb
    }

}