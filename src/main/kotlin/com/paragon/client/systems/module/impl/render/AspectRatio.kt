package com.paragon.client.systems.module.impl.render

import com.paragon.api.event.render.AspectEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.bus.listener.Listener

/**
 * @author Surge
 * @since 23/05/22
 */
object AspectRatio : Module("AspectRatio", Category.RENDER, "Changes the aspect ratio of the game") {

    private val ratio = Setting(
        "Ratio",
        1f,
        0.5f,
        10f,
        0.01f
    ) describedBy "The ratio of the screen"

    @Listener
    fun onAspectRatioEvent(event: AspectEvent) {
        event.cancel()
        event.ratio = ratio.value
    }

}