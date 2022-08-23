package com.paragon.client.systems.module.hud.impl

import com.paragon.api.module.Category
import com.paragon.api.module.IgnoredByNotifications
import com.paragon.api.module.Module
import com.paragon.api.setting.Bind
import com.paragon.client.systems.module.hud.HUDEditorGUI
import org.lwjgl.input.Keyboard

/**
 * @author Surge
 */
@IgnoredByNotifications
object HUDEditor : Module("HUDEditor", Category.HUD, "Lets you edit the HUD module positions", Bind(Keyboard.KEY_P, Bind.Device.KEYBOARD)) {

    override fun onEnable() {
        minecraft.displayGuiScreen(HUDEditorGUI())
        toggle()
    }

}