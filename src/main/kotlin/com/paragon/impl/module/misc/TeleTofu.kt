package com.paragon.impl.module.misc

import com.paragon.impl.module.Module
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer

/*
 * ehehehehhehehehehhe 🚎
 */
object TeleTofu : Module("TeleTofu", Category.MISC, "Tofu would be a lot cooler if he was 1% less gay") {

    private val timer = Timer()

    override fun onEnable() {
        if (minecraft.anyNull) {
            return
        }

        minecraft.player.sendChatMessage("I have had gay thoughts about TeleTofu")
    }

    override fun onDisable() {
        if (minecraft.anyNull) {
            return
        }

        minecraft.player.sendChatMessage("Have you ever had gay thoughts?")
    }

    override fun onTick() {
        if (!minecraft.anyNull && timer.hasMSPassed(2500.0)) {
            minecraft.player.sendChatMessage("I'M GAY FOR TELETOFU WOOOO")
            timer.reset()
        }
    }

}