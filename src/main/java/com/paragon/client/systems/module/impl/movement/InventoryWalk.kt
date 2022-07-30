package com.paragon.client.systems.module.impl.movement

import com.paragon.api.event.input.KeybindingPressedEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.Wrapper
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiRepair
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.math.MathHelper
import org.lwjgl.input.Keyboard

/**
 * @author aesthetical, Surge
 * @since 07/14/2022
 */
class InventoryWalk : Module("InventoryWalk", Category.MOVEMENT, "Lets you walk around in your inventory") {

    private val rotate = Setting("Rotate", true)
        .setDescription("If you can use the arrow keys to rotate in your inventory")

    private val rotateSpeed = Setting("Speed", 5f, 1f, 45f, 1f)
        .setDescription("How fast to rotate")
        .setParentSetting(rotate)

    private val BINDS = arrayOf(
        minecraft.gameSettings.keyBindForward,
        minecraft.gameSettings.keyBindBack,
        minecraft.gameSettings.keyBindRight,
        minecraft.gameSettings.keyBindRight,
        minecraft.gameSettings.keyBindJump,
        minecraft.gameSettings.keyBindSneak,
        minecraft.gameSettings.keyBindSprint
    )

    override fun onTick() {
        if (nullCheck()) {
            return
        }

        if (isValidGUI) {
            for (binding in BINDS) {
                KeyBinding.setKeyBindState(binding.keyCode, Keyboard.isKeyDown(binding.keyCode))
            }

            if (rotate.value) {
                if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                    minecraft.player.rotationPitch -= rotateSpeed.value
                }

                else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                    minecraft.player.rotationPitch += rotateSpeed.value
                }

                else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                    minecraft.player.rotationYaw -= rotateSpeed.value
                }

                else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                    minecraft.player.rotationYaw += rotateSpeed.value
                }

                minecraft.player.rotationPitch = MathHelper.clamp(minecraft.player.rotationPitch, -90.0f, 90.0f)
            }
        }
    }

    @Listener
    fun onKeyBindingPressedOverride(event: KeybindingPressedEvent) {
        if (isValidGUI) {
            try {
                event.pressedState = Keyboard.isKeyDown(event.keyCode)
                event.cancel()
            } catch (ignored: IndexOutOfBoundsException) {
                // this is only thrown for mouse binds, which we'll ignore
            }
        }
    }

    private val isValidGUI: Boolean = minecraft.currentScreen != null && minecraft.currentScreen !is GuiChat && minecraft.currentScreen !is GuiRepair
}