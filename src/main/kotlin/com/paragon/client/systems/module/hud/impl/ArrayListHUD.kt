@file:Suppress("IncorrectFormatting")

package com.paragon.client.systems.module.hud.impl

import com.paragon.Paragon
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.systems.module.hud.HUDEditorGUI
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.impl.client.Colours
import me.surge.animation.Easing
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.text.TextFormatting.GRAY
import net.minecraft.util.text.TextFormatting.WHITE
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
object ArrayListHUD : HUDModule("ArrayList", "Renders the enabled modules on screen") {

    val animationSpeed = Setting(
        "Animation",
        200f,
        0f,
        1000f,
        10f
    ) describedBy "The speed of the animation"

    private val arrayListColour = Setting(
        "Colour",
        ArrayListColour.RAINBOW_WAVE
    ) describedBy "What colour to render the modules in"

    val easing = Setting(
        "Easing",
        Easing.EXPO_IN_OUT
    ) describedBy "The easing type of the animation"

    private val background = Setting(
        "Background",
        Background.Normal
    ) describedBy "Render a background behind the text"

    private var corner = Corner.TOP_LEFT

    // Value = width
    private var modules: Map<Module, Float> = HashMap()

    override fun render() {
        val scaledResolution = ScaledResolution(minecraft)

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

        if (minecraft.currentScreen is HUDEditorGUI) {
            RenderUtil.drawRect(x, y, width - 2, height - 2, -0x70000000)
            RenderUtil.drawBorder(x, y, width - 2, height - 2, 1f, Colours.mainColour.value.rgb)
        }

        modules = HashMap()
        
        for (module in Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it.isVisible() && it.animation.getAnimationFactor() > 0.0 }) {
            (modules as HashMap<Module, Float>)[module] = FontUtil.getStringWidth(module.name + (if (module.getData() == "") "" else "$GRAY[$WHITE ${module.getData()}$GRAY]"))
        }

        modules = modules.toSortedMap(compareBy<Module?> { modules[it] }.reversed())

        val scissorWidth = modules.firstNotNullOf { it.value }.toDouble()
        
        when (corner) {
            Corner.TOP_LEFT -> {
                RenderUtil.pushScissor(x.toDouble() + 1, y.toDouble(), scissorWidth, modules.size * 13.0)
                var yOffset = y

                for (value in modules) {
                    val moduleData = value.key.name + (if (value.key.getData() == "") "" else "$GRAY[$WHITE${value.key.getData()}$GRAY]")

                    val origin = x - FontUtil.getStringWidth(moduleData)

                    if (background.value == Background.Normal) {
                        RenderUtil.drawRect((origin + FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, FontUtil.getStringWidth(moduleData), 13f, 0x70000000)
                    } else if (background.value == Background.Win98) {
                        RenderUtil.drawHorizontalGradientRect((origin + FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, FontUtil.getStringWidth(moduleData) + 1F, 14f, -12171706, -7039852)
                        RenderUtil.drawRect((origin + FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, FontUtil.getStringWidth(moduleData), 13f, -7039852)
                    }

                    FontUtil.drawStringWithShadow(moduleData, (origin + FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset + 2, arrayListColour.value.getColour(yOffset.toInt() / 13))

                    yOffset += 13f * value.key.animation.getAnimationFactor().toFloat()
                }
            }

            Corner.TOP_RIGHT -> {
                RenderUtil.pushScissor(((x + width) - scissorWidth), y.toDouble(), scissorWidth + 1, modules.size * 13.0)

                var yOffset = y

                for (value in modules) {
                    val moduleData = value.key.name + (if (value.key.getData() == "") "" else " $GRAY[$WHITE${value.key.getData()}$GRAY]")

                    val origin = x + width

                    if (background.value == Background.Normal) {
                        RenderUtil.drawRect((origin - FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, FontUtil.getStringWidth(moduleData), 13f, 0x70000000)
                    } else if (background.value == Background.Win98) {
                        RenderUtil.drawHorizontalGradientRect((origin - FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat() - 1F, yOffset, FontUtil.getStringWidth(moduleData) + 1F, 14f, -7039852, -12171706)
                        RenderUtil.drawRect((origin - FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, FontUtil.getStringWidth(moduleData), 13f, -7039852)
                    }

                    FontUtil.drawStringWithShadow(moduleData, (origin - FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset + 2, arrayListColour.value.getColour(yOffset.toInt() / 13))

                    yOffset += 13f * value.key.animation.getAnimationFactor().toFloat()
                }
            }

            Corner.BOTTOM_LEFT -> {
                RenderUtil.pushScissor(x.toDouble() + 1, (y + height) - modules.size * 13.0, scissorWidth,modules.size * 13.0)
                var yOffset = y + height - 13f

                for (value in modules) {
                    val moduleData = value.key.name + (if (value.key.getData() == "") "" else " $GRAY[$WHITE${value.key.getData()}$GRAY]")
                    val width = FontUtil.getStringWidth(moduleData) + 4

                    val origin = x - FontUtil.getStringWidth(moduleData)

                    if (background.value == Background.Normal) {
                        RenderUtil.drawRect((origin + width * value.key.animation.getAnimationFactor()).toFloat(), yOffset, width, 13f, 0x70000000)
                    } else if (background.value == Background.Win98) {
                        RenderUtil.drawHorizontalGradientRect((origin + width * value.key.animation.getAnimationFactor()).toFloat(), yOffset, width + 1F, 14f, -12171706, -7039852)
                        RenderUtil.drawRect((origin + width * value.key.animation.getAnimationFactor()).toFloat(), yOffset, width, 13f, -7039852)
                    }

                    FontUtil.drawStringWithShadow(moduleData, (origin + FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset + 2, arrayListColour.value.getColour(yOffset.toInt() / 13))

                    yOffset -= 13f * value.key.animation.getAnimationFactor().toFloat()
                }
            }

            Corner.BOTTOM_RIGHT -> {
                RenderUtil.pushScissor(((x + width) - scissorWidth), (y + height) - modules.size * 13.0, scissorWidth + 1,modules.size * 13.0)

                var yOffset = y + height - 13f

                for (value in modules) {
                    val moduleData = value.key.name + (if (value.key.getData() == "") "" else " $GRAY[$WHITE${value.key.getData()}$GRAY]")

                    val origin = x + width

                    if (background.value == Background.Normal) {
                        RenderUtil.drawRect((origin - FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, FontUtil.getStringWidth(moduleData), 13f, 0x70000000)
                    } else if (background.value == Background.Win98) {
                        RenderUtil.drawHorizontalGradientRect((origin - FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat() - 1F, yOffset, FontUtil.getStringWidth(moduleData) + 1F, 14f, -7039852, -12171706)
                        RenderUtil.drawRect((origin - FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset, FontUtil.getStringWidth(moduleData), 13f, -7039852)
                    }

                    FontUtil.drawStringWithShadow(moduleData, (origin - FontUtil.getStringWidth(moduleData) * value.key.animation.getAnimationFactor()).toFloat(), yOffset + 2, arrayListColour.value.getColour(yOffset.toInt() / 13))

                    yOffset -= 13f * value.key.animation.getAnimationFactor().toFloat()
                }
            }
        }

        RenderUtil.popScissor()
    }

    override var width = 56F
    override var height = 56F

    @Suppress("unused")
    enum class ArrayListColour(private val colour: (Int) -> Int) {
        /**
         * The colour is slightly different for each module in the array list
         */
        RAINBOW_WAVE({
            ColourUtil.getRainbow(
                Colours.mainColour.rainbowSpeed,
                Colours.mainColour.rainbowSaturation / 100f,
                it
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

    @Suppress("unused")
    enum class Background {
        NONE, Normal, Win98
    }

}