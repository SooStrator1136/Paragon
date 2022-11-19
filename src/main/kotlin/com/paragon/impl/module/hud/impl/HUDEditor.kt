package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.Module
import com.paragon.impl.module.hud.HUDEditorGUI
import com.paragon.impl.module.Category
import com.paragon.impl.module.annotation.IgnoredByNotifications
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import org.lwjgl.input.Keyboard

/**
 * @author Surge
 */
@IgnoredByNotifications
object HUDEditor : Module("HUDEditor", Category.HUD, "Lets you edit the HUD module positions", Bind(Keyboard.KEY_P, Bind.Device.KEYBOARD)) {

    val snap = Setting("Snap", 5.0, 1.0, 25.0, 1.0) describedBy "How much to snap modules by"

    override fun onEnable() {
        minecraft.displayGuiScreen(HUDEditorGUI())
        toggle()
    }

}