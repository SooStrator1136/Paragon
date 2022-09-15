package com.paragon.impl.module.misc

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.IMinecraft
import com.paragon.mixins.accessor.ITimer
import com.paragon.util.anyNull

/**
 * @author Surge
 */
object TimerModule : Module("Timer", Category.MISC, "Modifies how long each tick takes") {

    private val timer = Setting(
        "TimerSpeed", 1.25f, 0.01f, 4f, 0.01f
    ) describedBy "How much to multiply the timer speed by"

    override fun onDisable() {
        ((minecraft as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50f)
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        ((minecraft as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50 / timer.value)
    }

}