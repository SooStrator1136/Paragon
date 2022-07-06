package com.paragon.client.systems.module.impl.render

import com.paragon.api.util.string.StringUtil
import com.paragon.client.systems.module.Category
import com.paragon.client.systems.module.Module
import com.paragon.client.systems.module.setting.Setting
import net.minecraft.init.MobEffects
import net.minecraft.potion.PotionEffect

/**
 * @author Wolfsurge
 * @since 06/07/22
 */
object Fullbright : Module("Fullbright", Category.RENDER, "Changes your brightness beyond vanilla values") {

    private val mode = Setting("Mode", Mode.GAMMA)
        .setDescription("The mode to use for the brightness")

    var originalGamma: Float = 0f

    override fun onEnable() {
        // Set original gamma to current
        this.originalGamma = mc.gameSettings.gammaSetting
    }

    override fun onDisable() {
        // Reset gamma to original
        mc.gameSettings.gammaSetting = this.originalGamma

        // Remove night vision
        if (!nullCheck()) {
            mc.player.removePotionEffect(MobEffects.NIGHT_VISION)
        }
    }

    override fun onTick() {
        if (nullCheck()) {
            return
        }

        when (mode.getValue()) {
            Mode.GAMMA -> {
                // Increase gamma
                mc.gameSettings.gammaSetting = 50000f
            }

            Mode.EFFECT -> {
                // Apply night vision
                if (!mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
                    mc.player.addPotionEffect(PotionEffect(MobEffects.NIGHT_VISION, 999999, 255))
                }
            }

            Mode.ANTI -> {
                // Decrease gamma
                mc.gameSettings.gammaSetting = -Float.MAX_VALUE
            }
        }
    }

    override fun getData(): String {
        return " " + StringUtil.getFormattedText(mode.getValue())
    }

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