package com.paragon.client.systems.module.impl.misc

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.util.anyNull
import com.paragon.api.util.calculations.Timer

/*
 * ehehehehhehehehehhe
 */
object TeleTofu : Module("TeleTofu", Category.MISC, "Tofu would be a lot cooler if he was 1% less gay") {

    private val timer = Timer()

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        if (timer.hasMSPassed(2500.0)) {
            minecraft.player.sendChatMessage("I'M GAY FOR TELETOFU WOOOO")
            timer.reset()
        }
    }

}