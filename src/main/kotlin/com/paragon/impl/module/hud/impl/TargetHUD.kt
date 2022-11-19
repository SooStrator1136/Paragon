@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.client.Colours
import com.paragon.impl.module.combat.Aura
import com.paragon.impl.module.hud.HUDEditorGUI
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.RenderUtil.scaleTo
import com.paragon.util.render.font.FontUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import java.awt.Color

/**
 * @author SooStrator1136
 */
object TargetHUD : HUDModule("TargetHUD", "") {

    private val scale = Setting(
        "Size", 1.0, 0.1, 2.0, 0.1
    ) describedBy "Size of the module"

    private val color = Setting(
        "Color", Color.BLACK.integrateAlpha(75F)
    ) describedBy "Background color"

    private val clearDelay = Setting(
        "Delay", 100.0, 50.0, 2000.0, 50.0
    ) describedBy "Delay to switch/clear the target"

    private var target: Entity? = null

    private val clearTimer = Timer()

    override fun render() {
        // Target selection
        if (target == null) {
            val possibleTargets = arrayOf(
                if (Aura.isEnabled) Aura.lastTarget else null,
                //if (AutoCrystal.isEnabled) AutoCrystal.lastTarget else null
            )

            val newTarget = possibleTargets.indexOfFirst { it != null }
            if (newTarget == -1) {
                target = null
            } else {
                target = possibleTargets[newTarget]
                clearTimer.reset()
            }
        }

        @Suppress("SENSELESS_COMPARISON") if (target == null || minecraft.anyNull || minecraft.connection == null || minecraft.connection!!.getPlayerInfo(target!!.uniqueID) == null) {
            if (minecraft.currentScreen is HUDEditorGUI) { // Dummy for positioning
                RenderUtil.drawRect(
                    x, y, width, height, Color(255, 255, 255, 100)
                )
            }
            return
        }

        scaleTo(x, y, 0F, scale.value, scale.value, 1.0) {
            RenderUtil.drawRoundedRect(
                x, y, 38f + FontUtil.getStringWidth(target!!.name) + 5f, 38f, 5f, color.value
            )

            RenderUtil.drawRoundedOutline(
                x, y, 38 + FontUtil.getStringWidth(target!!.name) + 5f, 38f, 5f, 1f, Colours.mainColour.value
            )

            FontUtil.drawStringWithShadow(
                target!!.name, x + 38, y + 5, Color.WHITE
            )

            Minecraft.getMinecraft().textureManager.bindTexture(minecraft.connection!!.getPlayerInfo(target!!.uniqueID).locationSkin)
            Gui.drawModalRectWithCustomSizedTexture(x.toInt() + 5, y.toInt() + 5, 28f, 28f, 28, 28, 225F, 225F)

            scaleTo(x + 38F, y + 10F + FontUtil.getHeight(), 1F, 0.7, 0.7, 0.7) {
                target!!.armorInventoryList.forEachIndexed { i, armor ->
                    RenderUtil.renderItemStack(armor, x + 38F + (18F * i), y + 16F + FontUtil.getHeight(), true)
                }
            }

            val healthFactor = (target!! as EntityLivingBase).health / (target as EntityLivingBase).maxHealth

            RenderUtil.drawRoundedRect(x + 38f, y + 16f, 50f, 7f, 3f, Color(50, 50, 55))
            RenderUtil.drawRoundedRect(
                x + 38f, y + 16f, 50f * healthFactor, 7f, 3f, Color(255 - (255 * healthFactor).toInt(), (255 * healthFactor).toInt(), 0)
            )
        }
    }

    override var width = 43F
        get() = 53F + if (target != null) FontUtil.getStringWidth(target!!.name) else 0F

    override var height = 38F

}