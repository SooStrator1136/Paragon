package com.paragon.client.systems.module.impl.movement

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull

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