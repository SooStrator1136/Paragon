package com.paragon.client.systems.module.impl.misc

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull

/**
 * @author Surge
 */
object RotationLock : Module("RotationLock", Category.MISC, "Locks your rotation") {

    private val yaw = Setting(
        "Yaw",
        0f,
        -180f,
        180f,
        1f
    ) describedBy "The yaw to lock to"

    private val pitch = Setting(
        "Pitch",
        0f,
        -180f,
        180f,
        1f
    ) describedBy "The pitch to lock to"

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        minecraft.player.rotationYaw = yaw.value
        minecraft.player.rotationYawHead = yaw.value
        minecraft.player.rotationPitch = pitch.value
    }

}