@file:Suppress("IncorrectFormatting")

package com.paragon.client.systems.module.hud.impl

import com.paragon.Paragon
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.ITextRenderer
import com.paragon.api.util.render.RenderUtil
import com.paragon.client.systems.module.hud.HUDEditorGUI
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.util.animation.Easing
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.text.TextFormatting.*
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.SortedMap
import kotlin.collections.ArrayList

@SideOnly(Side.CLIENT)
object ArrayListHUD : HUDModule("ArrayList", "Renders the enabled modules on screen"), ITextRenderer {

    val animationSpeed = Setting("Animation", 200f, 0f, 1000f, 10f)
        .setDescription("The speed of the animation")

    private val arrayListColour = Setting("Colour", ArrayListColour.RAINBOW_WAVE)
        .setDescription("What colour to render the modules in")

    val easing = Setting("Easing", Easing.EXPO_IN_OUT)
        .setDescription("The easing type of the animation")

    private val background = Setting("Background", Background.Normal)
        .setDescription("Render a background behind the text")

    private var corner = Corner.TOP_LEFT

    // Value = width
    private var modules: Map<Module, Float> = HashMap()

    override fun render() {
        val scaledResolution = ScaledResolution(mc)
        if (x + width / 2 < scaledResolution.scaledWidth / 2f) {
            corner = if (y + height / 2 > scaledResolution.scaledHeight / 2f) {
                Corner.BOTTOM_LEFT
            } else {
                Corner.TOP_LEFT
            }
        } else if (x + width / 2 > scaledResolution.scaledWidth / 2f) {
            if (y + height / 2 < scaledResolution.scaledHeight / 2f) {
                corner = Corner.TOP_RIGHT
            } else if (y + height / 2 > scaledResolution.scaledHeight / 2f) {
                corner = Corner.BOTTOM_RIGHT
            }
        }

        if (mc.currentScreen is HUDEditorGUI) {
            RenderUtil.drawRect(x, y, width - 2, height - 2, -0x70000000)
            RenderUtil.drawBorder(x, y, width - 2, height - 2, 1f, Colours.mainColour.value.rgb)
        }

        modules = HashMap()
        
        for (module in Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it.isVisible() && it.animation.getAnimationFactor() > 0.0 }) {
            (modules as HashMap<Module, Float>)[module] = getStringWidth(module.name + (if (module.getData() == "") "" else "$GRAY[$WHITE ${module.getData()}$GRAY]"))
        }

        modules = modules.toSortedMap(compareBy<Module?> { modules[it] }.reversed())

        val scissorWidth: Double = modules.firstNotNullOf { it.value }.toDouble()
        
        when (corner) {
            Corner.TOP_LEFT -> {
                RenderUtil.pushScissor(x.toDouble(), y.toDouble(), scissorWidth, modules.size * 13.0)
                var yOffset = y

                for (value in modules) {
                    val moduleData = value.key.name + (if (value.key.getData() == "") "" else "$GRAY[$WHITE${value.key.getData()}$GRAY]")

                    val origin = x - getStringWidth(moduleData)

                    if (background.value == Background.Normal) {
                        RenderUtil.drawRect((origin + getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, getStringWidth(moduleData), 13f, 0x70000000)
                    } else if (background.value == Background.Win98) {
                        RenderUtil.drawHorizontalGradientRect((origin + getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, getStringWidth(moduleData) + 1F, 14f, -12171706, -7039852)
                        RenderUtil.drawRect((origin + getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, getStringWidth(moduleData), 13f, -7039852)
                    }

                    renderText(moduleData, (origin + getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset + 2, arrayListColour.value.getColour(yOffset.toInt() / 13))

                    yOffset += 13f * value.key.animation.getAnimationFactor().toFloat()
                }
            }

            Corner.TOP_RIGHT -> {
                RenderUtil.pushScissor(((x + width) - scissorWidth), y.toDouble(), scissorWidth, modules.size * 13.0)

                var yOffset = y

                for (value in modules) {
                    val moduleData = value.key.name + (if (value.key.getData() == "") "" else " $GRAY[$WHITE${value.key.getData()}$GRAY]")

                    val origin = x + width

                    if (background.value == Background.Normal) {
                        RenderUtil.drawRect((origin - getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, getStringWidth(moduleData), 13f, 0x70000000)
                    } else if (background.value == Background.Win98) {
                        RenderUtil.drawHorizontalGradientRect((origin - getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat() - 1F, yOffset, getStringWidth(moduleData) + 1F, 14f, -7039852, -12171706)
                        RenderUtil.drawRect((origin - getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, getStringWidth(moduleData), 13f, -7039852)
                    }

                    renderText(moduleData, (origin - getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset + 2, arrayListColour.value.getColour(yOffset.toInt() / 13))

                    yOffset += 13f * value.key.animation.getAnimationFactor().toFloat()
                }
            }

            Corner.BOTTOM_LEFT -> {
                RenderUtil.pushScissor(x.toDouble(), (y + height) - modules.size * 13.0, scissorWidth,modules.size * 13.0)
                var yOffset = y + height - 13f

                for (value in modules) {
                    val moduleData = value.key.name + (if (value.key.getData() == "") "" else " $GRAY[$WHITE${value.key.getData()}$GRAY]")
                    val width = getStringWidth(moduleData) + 4

                    val origin = x - getStringWidth(moduleData)

                    if (background.value == Background.Normal) {
                        RenderUtil.drawRect((origin + width * value.key.animation.getAnimationFactor()).toFloat(), yOffset, width, 13f, 0x70000000)
                    } else if (background.value == Background.Win98) {
                        RenderUtil.drawHorizontalGradientRect((origin + width * value.key.animation.getAnimationFactor()).toFloat(), yOffset, width + 1F, 14f, -12171706, -7039852)
                        RenderUtil.drawRect((origin + width * value.key.animation.getAnimationFactor()).toFloat(), yOffset, width, 13f, -7039852)
                    }

                    renderText(moduleData, (origin + width * value.key.animation.getAnimationFactor()).toFloat(), yOffset + 2, arrayListColour.value.getColour(yOffset.toInt() / 13))

                    yOffset -= 13f * value.key.animation.getAnimationFactor().toFloat()
                }
            }

            Corner.BOTTOM_RIGHT -> {
                RenderUtil.pushScissor(((x + width) - scissorWidth), (y + height) - modules.size * 13.0, scissorWidth,modules.size * 13.0)

                var yOffset = y + height - 13f

                for (value in modules) {
                    val moduleData = value.key.name + (if (value.key.getData() == "") "" else " $GRAY[$WHITE${value.key.getData()}$GRAY]")

                    val origin = x + width

                    if (background.value == Background.Normal) {
                        RenderUtil.drawRect((origin - getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, getStringWidth(moduleData), 13f, 0x70000000)
                    } else if (background.value == Background.Win98) {
                        RenderUtil.drawHorizontalGradientRect((origin - getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat() - 1F, yOffset, getStringWidth(moduleData) + 1F, 14f, -7039852, -12171706)
                        RenderUtil.drawRect((origin - getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, getStringWidth(moduleData), 13f, -7039852)
                    }

                    renderText(moduleData, (origin - getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset + 2, arrayListColour.value.getColour(yOffset.toInt() / 13))

                    yOffset -= 13f * value.key.animation.getAnimationFactor().toFloat()
                }
            }
        }
        RenderUtil.popScissor()
    }

    override fun getWidth() = 56f

    override fun getHeight() = 56f

    enum class ArrayListColour(private val colour: (Int) -> Int) {
        /**
         * The colour is slightly different for each module in the array list
         */
        RAINBOW_WAVE({ addition: Int ->
            ColourUtil.getRainbow(
                Colours.mainColour.rainbowSpeed,
                Colours.mainColour.rainbowSaturation / 100f,
                addition
            )
        }),

        /**
         * Permanent static colour
         */
        SYNC({ Colours.mainColour.value.rgb });

        /**
         * Gets the colour
         *
         * @param addition The addition to the colour
         * @return The colour
         */
        fun getColour(addition: Int) = colour.invoke(addition)

    }

    enum class Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    enum class Background {
        NONE, Normal, Win98
    }

}