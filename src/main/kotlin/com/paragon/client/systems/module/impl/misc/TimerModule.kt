package com.paragon.client.systems.module.impl.misc

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.mixins.accessor.IMinecraft
import com.paragon.mixins.accessor.ITimer

/**
 * @author Surge
 */
object TimerModule : Module("Timer", Category.MISC, "Modifies how long each tick takes") {

    private val timer = Setting(
        "TimerSpeed",
        1.25f,
        0.01f,
        4f,
        0.01f
    ) describedBy "How much to multiply the timer speed by"

    override fun onDisable() {
        ((minecraft as IMinecraft).timer as ITimer).tickLength = 50f
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        ((minecraft as IMinecraft).timer as ITimer).tickLength = 50 / timer.value
    }

}