package com.paragon.client.systems.module.impl.render

import com.paragon.api.event.render.entity.EntityHighlightOnHitEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.bus.listener.Listener
import java.awt.Color

/**
 * @author Surge
 * @since 22/05/22
 */
object HitColour : Module("HitColour", Category.RENDER, "Change the colour entities are rendered in when hit") {

    private val colour = Setting(
        "Colour",
        Color(185, 17, 255, 85)
    ) describedBy "The highlight colour"

    @Listener
    fun onEntityHighlight(event: EntityHighlightOnHitEvent) {
        event.cancel()
        event.colour = colour.value
    }

}