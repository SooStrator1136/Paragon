package com.paragon.impl.module.hud.impl

import com.paragon.Paragon
import com.paragon.impl.module.Module
import com.paragon.impl.module.hud.HUDEditorGUI
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.module.Category
import com.paragon.util.render.RenderUtil

/**
 * @author Surge
 */
object HUD : Module("HUD", Category.HUD, "Render the client's HUD on screen") {

    override fun onRender2D() {
        if (minecraft.currentScreen !is HUDEditorGUI) {
            Paragon.INSTANCE.moduleManager.getModulesThroughPredicate {
                it is HUDModule && it.animation.getAnimationFactor() > 0
            }.forEach {
                it as HUDModule

                RenderUtil.scaleTo(it.x + (it.width / 2), it.y + (it.height / 2), 0f, it.animation.getAnimationFactor(), it.animation.getAnimationFactor(), 0.0) {
                    it.render()
                }
            }
        }
    }

}