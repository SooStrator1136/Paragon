package com.paragon.client.systems.module.impl.render;

import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class Fullbright extends Module {

    private Setting<Mode> mode = new Setting<>("Mode", Mode.GAMMA)
            .setDescription("What mode to use");

    public Fullbright() {
        super("Fullbright", Category.RENDER, "Makes the world appear brighter");
        this.addSettings(mode);
    }

    public void onTick() {
        if (nullCheck()) {
            return;
        }

        switch (mode.getValue()) {
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
