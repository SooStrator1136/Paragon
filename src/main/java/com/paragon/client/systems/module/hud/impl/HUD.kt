package com.paragon.client.systems.module.hud.impl

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.Module

import com.paragon.client.systems.module.hud.HUDEditorGUI
import com.paragon.client.systems.module.hud.HUDModule

/**
 * @author Surge
 */
object HUD : Module("HUD", Category.HUD, "Render the client's HUD on screen")  {

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