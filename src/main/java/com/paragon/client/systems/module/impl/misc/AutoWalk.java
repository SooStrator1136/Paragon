package com.paragon.client.systems.module.impl.misc;

import com.paragon.asm.mixins.accessor.IKeyBinding;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.client.settings.KeyBinding;

/**
 * @author Wolfsurge
 * @since 14/05/22
 */
public class AutoWalk extends Module {

    private final Setting<Direction> direction = new Setting<>("Direction", Direction.FORWARD)
            .setDescription("The direction to walk in");

    public AutoWalk() {
        super("AutoWalk", Category.MISC, "Makes you constantly walk");
        this.addSettings(direction);
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }

        // Reset the key on disable
        ((IKeyBinding) direction.getValue().getKey()).setPressed(false);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        // Set the key to pressed
        ((IKeyBinding) direction.getValue().getKey()).setPressed(true);
    }

    public enum Direction {
        /**
         * Walk forward
         */
        FORWARD(mc.gameSettings.keyBindForward),

        /**
         * Walk backward
         */
        BACKWARD(mc.gameSettings.keyBindBack),

        /**
         * Walk left
         */
        LEFT(mc.gameSettings.keyBindLeft),

        /**
         * Walk right
         */
        RIGHT(mc.gameSettings.keyBindRight);

        // The keybind that we want to "press"
        private final KeyBinding key;

        Direction(KeyBinding keyBinding) {
            this.key = keyBinding;
        }

        /**
         * Gets the keybind
         *
         * @return The keybind
         */
        public KeyBinding getKey() {
            return key;
        }
    }
}
