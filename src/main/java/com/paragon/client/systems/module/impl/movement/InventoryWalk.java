package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.input.KeybindingPressedEvent;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;

/**
 * @author aesthetical
 * @since 07/14/2022
 */
public class InventoryWalk extends Module {
    public static InventoryWalk INSTANCE;

    private static final KeyBinding[] BINDS = {
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindJump,
            mc.gameSettings.keyBindSneak,
            mc.gameSettings.keyBindSprint
    };

    public InventoryWalk() {
        super("InventoryWalk", Category.MOVEMENT, "Lets you walk around in your inventory");
        INSTANCE = this;
    }

    public static Setting<Boolean> rotate = new Setting<>("Rotate", true)
            .setDescription("If you can use the arrow keys to rotate in your inventory");

    @Override
    public void onTick() {

        if (nullCheck()) {
            return;
        }

        if (isValidGUI()) {

            for (KeyBinding binding : BINDS) {
                KeyBinding.setKeyBindState(binding.getKeyCode(), Keyboard.isKeyDown(binding.getKeyCode()));
            }

            if (rotate.getValue()) {

                float value = 5.0f;

                if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                    mc.player.rotationPitch -= value;
                } else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                    mc.player.rotationPitch += value;
                } else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                    mc.player.rotationYaw -= value;
                } else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                    mc.player.rotationYaw += value;
                }

                mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch, -90.0f, 90.0f);
            }
        }
    }

    @Listener
    public void onKeyBindingPressedOverride(KeybindingPressedEvent event) {

        if (isValidGUI()) {
            try {
                event.setPressedState(Keyboard.isKeyDown(event.getKeyCode()));
                event.cancel();
            } catch (IndexOutOfBoundsException ignored) {
                // this is only thrown for mouse binds, which we'll ignore
            }
        }
    }

    private boolean isValidGUI() {
        return mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat) && !(mc.currentScreen instanceof GuiRepair);
    }
}
