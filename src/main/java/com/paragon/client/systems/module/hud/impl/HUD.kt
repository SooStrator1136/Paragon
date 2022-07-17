package com.paragon.client.systems.module.hud.impl

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.util.Wrapper
import com.paragon.api.util.render.ITextRenderer
import com.paragon.client.systems.module.hud.HUDEditorGUI
import com.paragon.client.systems.module.hud.HUDModule
import java.util.function.Consumer

/**
 * @author Wolfsurge
 */
object HUD : Module("HUD", Category.HUD, "Render the client's HUD on screen"), ITextRenderer {

    override fun onRender2D() {
        if (minecraft.currentScreen !is HUDEditorGUI) {
            Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { module ->
                module is HUDModule && module.isEnabled
            }.forEach { module ->
                (module as HUDModule).render()
            }
        }
    }

}