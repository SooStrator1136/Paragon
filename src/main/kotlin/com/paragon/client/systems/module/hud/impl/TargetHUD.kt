@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.paragon.client.systems.module.hud.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.anyIndexed
import com.paragon.api.util.anyNull
import com.paragon.api.util.calculations.Timer
import com.paragon.api.util.entity.EntityUtil
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.RenderUtil.scaleTo
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.hud.HUDEditorGUI
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.combat.Aura
import com.paragon.client.systems.module.impl.combat.AutoCrystal
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
        "Size",
        1.0,
        0.1,
        2.0,
        0.1
    ) describedBy "Size of the module"

    private val color = Setting(
        "Color",
        Color.BLACK.integrateAlpha(75F)
    ) describedBy "Background color"

    private val clearDelay = Setting(
        "Delay",
        100.0,
        50.0,
        2000.0,
        50.0
    ) describedBy "Delay to switch/clear the target"

    private var target: Entity? = null

    private val clearTimer = Timer()

    override fun render() {
        //Target selection
        if (target == null || clearTimer.hasMSPassed(clearDelay.value)) {
            val possibleTargets = arrayOf(
                if (Aura.isEnabled) Aura.lastTarget else null,
                if (AutoCrystal.isEnabled) AutoCrystal.lastTarget else null
            )

            val newTarget = possibleTargets.anyIndexed { it != null }
            if (newTarget == -1) {
                target = null
            } else {
                target = possibleTargets[newTarget]
                clearTimer.reset()
            }
        }

        @Suppress("SENSELESS_COMPARISON")
        if (target == null
            || minecraft.anyNull
            || minecraft.connection == null
            || minecraft.connection!!.getPlayerInfo(target!!.uniqueID) == null
        ) {
            if (minecraft.currentScreen is HUDEditorGUI) { //Dummy for positioning
                RenderUtil.drawRect(
                    x,
                    y,
                    width,
                    height,
                    Color(255, 255, 255, 100).rgb
                )
            }
            return
        }

        scaleTo(x, y, 0F, scale.value, scale.value, 1.0) {
            RenderUtil.drawRoundedRect(
                x.toDouble(),
                y.toDouble(),
                38.0 + FontUtil.getStringWidth(target!!.name) + 5.0,
                38.0,
                5.0,
                5.0,
                5.0,
                5.0,
                color.value.rgb
            )

            FontUtil.drawStringWithShadow(
                target!!.name,
                x + 38,
                y + 5,
                -1
            )

            Minecraft.getMinecraft().textureManager.bindTexture(minecraft.connection!!.getPlayerInfo(target!!.uniqueID).locationSkin)
            Gui.drawModalRectWithCustomSizedTexture(x.toInt() + 5, y.toInt() + 5, 28f, 28f, 28, 28, 225F, 225F)

            scaleTo(x + 38F, y + 10F + FontUtil.getHeight(), 1F, 0.7, 0.7, 0.7) {
                target!!.armorInventoryList.forEachIndexed { i, armor ->
                    RenderUtil.renderItemStack(armor, x + 38F + (18F * i), y + 16F + FontUtil.getHeight(), true)
                }
            }

            val healthFactor = (target!! as EntityLivingBase).health / (target as EntityLivingBase).maxHealth

            RenderUtil.drawRoundedRect(x + 38.0, y + 16.0, 50.0, 7.0, 3.0, 3.0, 3.0, 3.0, Color(50, 50, 55).rgb)
            RenderUtil.drawRoundedRect(x + 38.0, y + 16.0, 50.0 * healthFactor, 7.0, 3.0, 3.0, 3.0, 3.0, Color(255 - (255 * healthFactor).toInt(), (255 * healthFactor).toInt(), 0).rgb)
        }
    }

    override var width = 43F
        get() = 43F + if (target != null) FontUtil.getStringWidth(target!!.name) else 0F

    override var height = 38F

}