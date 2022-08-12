package com.paragon.client.systems.module.hud.impl

import com.google.common.collect.Ordering
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.RenderUtil.scaleTo
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.hud.HUDEditorGUI
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion
import java.awt.Color

/**
 * @author SooStrator1136
 */
object PotionHUD : HUDModule("PotionHUD", "Shows active potion effects") {

    private val scale = Setting(
        "Size",
        1.0,
        0.5,
        5.0,
        0.1
    ) describedBy "The size of the PotionHUD"

    private val mode = Setting("Mode", Mode.INVENTORY)

    private val rainbowSpeed = Setting(
        "Rainbow speed",
        20F,
        5F,
        50F,
        2.5F
    ) visibleWhen { mode.value == Mode.PYRO }
    private val showBg = Setting(
        "Background",
        true
    ) visibleWhen { mode.value == Mode.PYRO }
    private val syncTextColor = Setting(
        "Sync text",
        false
    ) visibleWhen { mode.value == Mode.PYRO }

    private val offset = Setting(
        "Offset",
        0F,
        0F,
        10F,
        1F
    ) describedBy "The offset between the effects"

    override fun render() {
        val activeEffects = minecraft.player.activePotionEffects

        if (activeEffects.isEmpty()) {
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
            when (mode.value) {
                Mode.INVENTORY -> { //Pasted from InventoryEffectRenderer
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                    GlStateManager.disableLighting()

                    var effectY = y.toInt()

                    var entryHeight = (((FontUtil.getHeight() * 2.5) + 3) + offset.value).toInt()
                    if (activeEffects.size > 5) {
                        entryHeight = ((132 / (activeEffects.size - 1)) + offset.value).toInt()
                    }

                    for (effect in Ordering.natural<Comparable<*>>().sortedCopy(activeEffects)) {
                        val potion = effect.potion

                        if (!potion.shouldRender(effect)) {
                            continue
                        }

                        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                        minecraft.textureManager.bindTexture(GuiContainer.INVENTORY_BACKGROUND) //Idfk but this is needed to the icon renders properly
                        FontUtil.drawStringWithShadow("", x, y, -1) //Why? Idfk it makes no sense

                        if (potion.hasStatusIcon()) {
                            val iconIndex = potion.statusIconIndex
                            drawTexturedModalRect(
                                x.toInt(),
                                effectY + 7,
                                0 + iconIndex % 8 * 18,
                                198 + iconIndex / 8 * 18,
                                18,
                                18
                            )
                        }

                        if (!potion.shouldRenderInvText(effect)) {
                            effectY += entryHeight
                            continue
                        }

                        val effectName = I18n.format(potion.name) + " " + I18n.format("enchantment.level.${effect.amplifier + 1}")
                        FontUtil.drawStringWithShadow(effectName, (x + 22), (effectY + 6).toFloat(), 16777215)

                        FontUtil.drawStringWithShadow(
                            Potion.getPotionDurationString(effect, 1F),
                            (x + 22),
                            (effectY + 6 + 10).toFloat(),
                            8355711
                        )

                        effectY += entryHeight
                    }

                    height = (entryHeight * activeEffects.size).toFloat()
                }

                Mode.PYRO -> {
                    var effectY = y
                    val maxWidth = activeEffects.maxWith(
                        Comparator.comparingDouble {
                            FontUtil.getStringWidth(
                                I18n.format(it.potion.name) + " " + I18n.format("enchantment.level.${it.amplifier + 1}")
                                        + " ${Potion.getPotionDurationString(it, 1F)}"
                            ).toDouble()
                        }
                    ).let {
                        FontUtil.getStringWidth(
                            I18n.format(it.potion.name) + " " + I18n.format("enchantment.level.${it.amplifier + 1}")
                                    + " ${Potion.getPotionDurationString(it, 1F)}"
                        ) + FontUtil.getHeight() + 1F
                    } //💀

                    for (effect in activeEffects) {
                        val color = Color(
                            ColourUtil.getRainbow(
                                rainbowSpeed.value,
                                Colours.mainColour.rainbowSaturation / 100f,
                                (effectY * effectY).toInt()
                            )
                        )

                        if (showBg.value) {
                            RenderUtil.drawRect(
                                x,
                                effectY,
                                maxWidth,
                                FontUtil.getHeight() + 1F,
                                color.integrateAlpha(100F).rgb
                            )
                            RenderUtil.drawBorder(
                                x,
                                effectY,
                                maxWidth,
                                FontUtil.getHeight() + 1F,
                                1F,
                                color.darker().rgb
                            )
                        }

                        val scaleFac = FontUtil.getHeight() / 18.0
                        scaleTo(x, effectY, 0F, scaleFac, scaleFac, 1.0) {
                            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                            GlStateManager.disableLighting()
                            minecraft.textureManager.bindTexture(GuiContainer.INVENTORY_BACKGROUND) //Idfk but this is needed to the icon renders properly
                            FontUtil.drawStringWithShadow("", x, y, -1) //How? Idfk it makes no sense

                            if (effect.potion.hasStatusIcon()) {
                                val iconIndex = effect.potion.statusIconIndex
                                drawTexturedModalRect(
                                    x.toInt(),
                                    effectY.toInt(),
                                    0 + iconIndex % 8 * 18,
                                    198 + iconIndex / 8 * 18,
                                    18,
                                    18
                                )
                            }
                        }

                        FontUtil.drawStringWithShadow(
                            I18n.format(effect.potion.name) + " " + I18n.format(
                                "enchantment.level.${effect.amplifier + 1}"
                            ) + " ${Potion.getPotionDurationString(effect, 1F)}",
                            x + FontUtil.getHeight() + 1F,
                            effectY + 1F,
                            if (syncTextColor.value) color.rgb else -1
                        )

                        effectY += FontUtil.getHeight() + offset.value + 2F
                    }

                    height = (FontUtil.getHeight() + offset.value + 3F) * activeEffects.size
                }
            }
        }
    }

    //Pasted from Gui class
    private fun drawTexturedModalRect(x: Int, y: Int, textureX: Int, textureY: Int, width: Int, height: Int) {
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
        bufferbuilder.pos((x + 0).toDouble(), (y + height).toDouble(), 0.0).tex(
            ((textureX + 0).toFloat() * 0.00390625f).toDouble(),
            ((textureY + height).toFloat() * 0.00390625f).toDouble()
        ).endVertex()
        bufferbuilder.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(
            ((textureX + width).toFloat() * 0.00390625f).toDouble(),
            ((textureY + height).toFloat() * 0.00390625f).toDouble()
        ).endVertex()
        bufferbuilder.pos((x + width).toDouble(), (y + 0).toDouble(), 0.0).tex(
            ((textureX + width).toFloat() * 0.00390625f).toDouble(),
            ((textureY + 0).toFloat() * 0.00390625f).toDouble()
        ).endVertex()
        bufferbuilder.pos((x + 0).toDouble(), (y + 0).toDouble(), 0.0).tex(
            ((textureX + 0).toFloat() * 0.00390625f).toDouble(),
            ((textureY + 0).toFloat() * 0.00390625f).toDouble()
        ).endVertex()
        tessellator.draw()
    }

    override var width = (140F * scale.value).toFloat()

    override var height = (10F * scale.value).toFloat()

    internal enum class Mode {
        INVENTORY, PYRO
    }

}