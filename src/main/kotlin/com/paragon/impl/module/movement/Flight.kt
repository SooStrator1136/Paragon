package com.paragon.impl.module.movement

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.util.anyNull

object Flight : Module("Flight", Category.MOVEMENT, "Allows you to fly in survival mode") {

    private val flySpeed = Setting("FlySpeed", 0.05f, 0.01f, 0.1f, 0.01f) describedBy "How fast you fly"

    override fun onDisable() {
        minecraft.player.capabilities.flySpeed = 0.05f
        minecraft.player.capabilities.isFlying = false
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        minecraft.player.capabilities.flySpeed = flySpeed.value
        minecraft.player.capabilities.isFlying = true
    }

}