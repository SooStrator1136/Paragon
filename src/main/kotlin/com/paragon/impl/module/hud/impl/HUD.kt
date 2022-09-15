package com.paragon.impl.module.hud.impl

import com.paragon.Paragon
import com.paragon.impl.module.Module
import com.paragon.impl.module.hud.HUDEditorGUI
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.module.Category

/**
 * @author Surge
 */
object HUD : Module("HUD", Category.HUD, "Render the client's HUD on screen") {

    override fun onRender2D() {
        if (minecraft.currentScreen !is HUDEditorGUI) {
            Paragon.INSTANCE.moduleManager.getModulesThroughPredicate {
                it is HUDModule && it.isEnabled
            }.forEach {
                (it as HUDModule).render()
            }
        }
    }

}