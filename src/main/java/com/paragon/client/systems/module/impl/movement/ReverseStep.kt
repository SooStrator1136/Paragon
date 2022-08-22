package com.paragon.client.systems.module.impl.movement

import com.paragon.api.module.Category
import com.paragon.api.module.Module

/**
 * @author Surge, SooStrator1136
 */
object ReverseStep : Module("ReverseStep", Category.MOVEMENT, "Moves you down when you walk off of a block") {

    override fun onTick() {
        minecraft?.player?.let { player ->
            // Check that we want to fall
            if (player.onGround && !player.isInWater && !player.isInLava && !player.isOnLadder && !minecraft.gameSettings.keyBindJump.isKeyDown) {
                minecraft.player.motionY = -1.0 //shouldnt be 10
            }
        }
    }
//i dont like java this shit is pure aids
}