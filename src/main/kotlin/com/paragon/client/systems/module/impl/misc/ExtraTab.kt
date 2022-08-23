package com.paragon.client.systems.module.impl.misc

import com.paragon.api.event.render.gui.TabListEvent
import com.paragon.api.event.render.gui.TabOverlayEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.bus.listener.Listener

object ExtraTab : Module("ExtraTab", Category.MISC, "Extends the limit of players on the tab list") {

    private val limit = Setting(
        "Limit",
        500f,
        1f,
        500f,
        1f
    ) describedBy "The limit of players"

    @Listener
    fun onTabList(event: TabListEvent) {
        event.size = limit.value
        event.cancel()
    }

    @Listener
    fun onTabOverlay(event: TabOverlayEvent) {
        event.cancel()
    }

}