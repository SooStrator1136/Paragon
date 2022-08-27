package com.paragon.client.systems.module.impl.misc

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.mixins.accessor.IKeyBinding
import net.minecraft.client.settings.KeyBinding

/**
 * @author Surge
 * @since 14/05/22
 */
object AutoWalk : Module("AutoWalk", Category.MISC, "Makes you constantly walk") {

    private val direction = Setting(
        "Direction",
        Direction.FORWARD
    ) describedBy "The direction to walk in"

    override fun onDisable() {
        if (minecraft.anyNull) {
            return
        }

        // Reset the key on disable
        (direction.value.key as IKeyBinding).setPressed(false)
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        // Set the key to pressed
        (direction.value.key as IKeyBinding).setPressed(true)
    }

    enum class Direction(val key: KeyBinding) {
        /**
         * Walk forward
         */
        FORWARD(minecraft.gameSettings.keyBindForward),

        /**
         * Walk backward
         */
        BACKWARD(minecraft.gameSettings.keyBindBack),

        /**
         * Walk left
         */
        LEFT(minecraft.gameSettings.keyBindLeft),

        /**
         * Walk right
         */
        RIGHT(minecraft.gameSettings.keyBindRight);

    }

}