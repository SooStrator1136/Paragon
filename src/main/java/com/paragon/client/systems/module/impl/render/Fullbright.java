package com.paragon.client.systems.module.impl.render;

import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class Fullbright extends Module {

    private ModeSetting<Mode> mode = new ModeSetting<>("Mode", "What mode to use", Mode.GAMMA);

    public Fullbright() {
        super("Fullbright", ModuleCategory.RENDER, "Makes the world appear brighter");
        this.addSettings(mode);
    }

    public void onTick() {
        if (nullCheck()) {
            return;
        }

        switch (mode.getCurrentMode()) {
            case GAMMA:
                // Increase gamma
                mc.gameSettings.gammaSetting = 50000;
                break;
            case EFFECT:
                // Apply night vision if it isn't already active
                if (!mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
                    mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 999999, 255));
                }
                break;
        }
    }

    public enum Mode {
        /**
         * Change gamma value
         */
        GAMMA,

        /**
         * Apply night vision effect
         */
        EFFECT
    }

}
