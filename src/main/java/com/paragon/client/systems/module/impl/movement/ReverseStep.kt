package com.paragon.client.systems.module.impl.movement

import com.paragon.api.module.Category
import com.paragon.api.module.Module

/**
 * @author Wolfsurge, SooStrator1136
 */
object ReverseStep : Module("ReverseStep", Category.MOVEMENT, "Moves you down when you walk off of a block") {

    override fun onTick() {
        mc?.player?.let { player ->
            // Check that we want to fall
            if (player.onGround && !player.isInWater && !player.isInLava && !player.isOnLadder && !mc.gameSettings.keyBindJump.isKeyDown) {
                mc.player.motionY = -10.0
            }
        }
    }

}