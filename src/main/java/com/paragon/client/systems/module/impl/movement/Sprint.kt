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

    private val mode = Setting("Mode", Mode.LEGIT).setDescription("The mode to sprint in")
    private val onlyWhenMoving = Setting("WhenMoving", true)
        .setDescription("Only omni sprint when actually moving")
        .setVisibility { mode.value === Mode.OMNI }

    override fun onDisable() {
        // Stop us sprinting when we disable
        minecraft.player?.isSprinting = false
    }

    override fun onTick() {
        minecraft?.player?.let { player ->
            when (mode.value) {
                Mode.OMNI -> {
                    // If we aren't moving, do not make us sprint
                    if (onlyWhenMoving.value && !PlayerUtil.isMoving()) {
                        return
                    }

                    // Make us sprint
                    player.isSprinting = true
                }
                Mode.LEGIT -> player.isSprinting = player.movementInput.moveForward > 0 // Make us sprint if we are pressing forward
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