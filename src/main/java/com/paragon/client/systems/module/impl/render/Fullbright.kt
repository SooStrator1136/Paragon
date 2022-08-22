package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.string.StringUtil
import net.minecraft.init.MobEffects
import net.minecraft.potion.PotionEffect

/**
 * @author Surge
 * @since 06/07/22
 */
object Fullbright : Module("Fullbright", Category.RENDER, "Changes your brightness beyond vanilla values") {

    private val mode = Setting(
        "Mode",
        Mode.GAMMA
    ) describedBy "The mode to use for the brightness"

    private var originalGamma = 0f

    override fun onEnable() {
        // Set original gamma to current
        this.originalGamma = minecraft.gameSettings.gammaSetting
    }

    override fun onDisable() {
        // Reset gamma to original
        minecraft.gameSettings.gammaSetting = this.originalGamma

        // Remove night vision
        if (!minecraft.anyNull) {
            minecraft.player.removePotionEffect(MobEffects.NIGHT_VISION)
        }
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        when (mode.value) {
            Mode.GAMMA -> minecraft.gameSettings.gammaSetting = 50000f // Increase gamma //why is this so fucking high - xello
            Mode.EFFECT -> {
                // Apply night vision
                if (!minecraft.player.isPotionActive(MobEffects.NIGHT_VISION)) {
                    minecraft.player.addPotionEffect(PotionEffect(MobEffects.NIGHT_VISION, 999999, 255))
                }
            }

            Mode.ANTI -> minecraft.gameSettings.gammaSetting = -Float.MAX_VALUE // Decrease gamma
        }
    }

    override fun getData(): String = StringUtil.getFormattedText(mode.value)

    enum class Mode {
        /**
         * Change gamma value
         */
        GAMMA,

        /**
         * Apply night vision effect
         */
        EFFECT,

        /**
         * Reverse fullbright
         */
        ANTI
    }

}