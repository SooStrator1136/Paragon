package com.paragon.client.systems.module.impl.movement

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.player.PlayerUtil
import com.paragon.api.util.string.StringUtil

/**
 * @author Surge, SooStrator1136
 */
object Sprint : Module("Sprint", Category.MOVEMENT, "Automatically sprint") {

    private val mode = Setting(
        "Mode",
        Mode.LEGIT
    ) describedBy "The mode to sprint in"
    private val onlyWhenMoving = Setting(
        "WhenMoving",
        true
    ) describedBy "Only omni sprint when actually moving" visibleWhen { mode.value === Mode.OMNI }

    override fun onDisable() {
        // Stop sprinting when we disable
        minecraft.player?.isSprinting = false
    }

    override fun onTick() {
        minecraft?.player?.let {
            when (mode.value) {
                Mode.OMNI -> {
                    if (onlyWhenMoving.value && !PlayerUtil.isMoving) {
                        return
                    }

                    it.isSprinting = true
                }

                Mode.LEGIT -> it.isSprinting = it.movementInput.moveForward > 0
            }
        }
    }

    override fun getData() = StringUtil.getFormattedText(mode.value)

    enum class Mode {

        /**
         * Only sprint if you are walking forwards
         */
        LEGIT,

        /**
         * Always sprint
         */
        OMNI
    }

}