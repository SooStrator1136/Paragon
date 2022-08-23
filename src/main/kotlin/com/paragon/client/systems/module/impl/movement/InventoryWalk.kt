package com.paragon.client.systems.module.impl.movement

import com.paragon.api.event.input.KeybindingPressedEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.bus.listener.Listener
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiRepair
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.math.MathHelper
import org.lwjgl.input.Keyboard

/**
 * @author aesthetical, Surge
 * @since 07/14/2022
 */
object InventoryWalk : Module("InventoryWalk", Category.MOVEMENT, "Lets you walk around in your inventory") {

    private val rotate = Setting(
        "Rotate",
        true
    ) describedBy "If you can use the arrow keys to rotate in your inventory"

    private val rotateSpeed = Setting(
        "Speed",
        5f,
        1f,
        45f,
        1f
    ) describedBy "How fast to rotate" subOf rotate

    private val bindings = arrayOf(
        minecraft.gameSettings.keyBindForward,
        minecraft.gameSettings.keyBindBack,
        minecraft.gameSettings.keyBindRight,
        minecraft.gameSettings.keyBindRight,
        minecraft.gameSettings.keyBindJump,
        minecraft.gameSettings.keyBindSneak,
        minecraft.gameSettings.keyBindSprint
    )

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        if (isValidGUI) {
            for (binding in bindings) {
                KeyBinding.setKeyBindState(binding.keyCode, Keyboard.isKeyDown(binding.keyCode))
            }

            if (rotate.value) {
                if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                    minecraft.player.rotationPitch -= rotateSpeed.value
                } else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                    minecraft.player.rotationPitch += rotateSpeed.value
                } else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                    minecraft.player.rotationYaw -= rotateSpeed.value
                } else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                    minecraft.player.rotationYaw += rotateSpeed.value
                }

                minecraft.player.rotationPitch = MathHelper.clamp(minecraft.player.rotationPitch, -90.0f, 90.0f)
            }
        }
    }

    @Listener
    fun onKeyBindingPressedOverride(event: KeybindingPressedEvent) {
        if (isValidGUI) {
            runCatching {
                event.pressedState = Keyboard.isKeyDown(event.keyCode)
                event.cancel()
            } //This only throws for mouse binds, which we'll ignore
        }
    }

    private val isValidGUI: Boolean
        get() = minecraft.currentScreen != null && minecraft.currentScreen !is GuiChat && minecraft.currentScreen !is GuiRepair

}