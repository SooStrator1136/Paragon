package com.paragon.impl.module.movement

import com.paragon.impl.module.Module
import com.paragon.impl.module.Category

/**
 * @author Surge, SooStrator1136
 */
object ReverseStep : Module("ReverseStep", Category.MOVEMENT, "Moves you down when you walk off of a block") {

    override fun onTick() {
        minecraft.player?.let {
            // Check that we want to fall
            if (it.onGround && !it.isInWater && !it.isInLava && !it.isOnLadder && !minecraft.gameSettings.keyBindJump.isKeyDown) {
                minecraft.player.motionY = -10.0
            }
        }
    }

}