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
import java.util.*
import java.util.function.Function
import kotlin.collections.ArrayList


@SideOnly(Side.CLIENT)
object ArrayListHUD : HUDModule("ArrayList", "Renders the enabled modules on screen"), ITextRenderer {

    val animationSpeed = Setting("Animation", 200f, 0f, 1000f, 10f)
        .setDescription("The speed of the animation")

    private val arrayListColour = Setting("Colour", ArrayListColour.RAINBOW_WAVE)
        .setDescription("What colour to render the modules in")

    val easing = Setting("Easing", Easing.EXPO_IN_OUT)
        .setDescription("The easing type of the animation")

    private val background = Setting("Background", false)
        .setDescription("Render a background behind the text")

    // Creating a new list, so we can sort these, but not the module manager list
    private var enabledModules: ArrayList<Module> = ArrayList()
    private var corner = Corner.TOP_LEFT

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

        val enabledModules: ArrayList<Module> = ArrayList()

        for (module in Paragon.INSTANCE.moduleManager.modules.sortedBy { getStringWidth(it.name + if (it.getData() == "") it.getData() else "") }.reversed()) {
            if (module.animation.getAnimationFactor() > 0 && module.isVisible()) {
                enabledModules.add(module)
            }
        }

        val scissorWidth: Double = getStringWidth(enabledModules[0].name + (if (enabledModules[0].getData() == "") "" else "$GRAY[$WHITE ${enabledModules[0].getData()}$GRAY]")).toDouble()

        when (corner) {
            Corner.TOP_LEFT -> {
                RenderUtil.pushScissor(x.toDouble(), y.toDouble(), scissorWidth, enabledModules.size * 13.0)
                var yOffset = y

                for (module in enabledModules) {
                    val moduleData = module.name + (if (module.getData() == "") "" else "$GRAY[$WHITE${module.getData()}$GRAY]")

                    val origin = x - getStringWidth(moduleData)

                    if (background.value) {
                        RenderUtil.drawRect((origin + getStringWidth(moduleData) * module.animation.getAnimationFactor()).toFloat(), yOffset, getStringWidth(moduleData), 13f, 0x70000000)
                    }

                    renderText(moduleData, (origin + getStringWidth(moduleData) * module.animation.getAnimationFactor()).toFloat(), yOffset + 2, arrayListColour.value.getColour(yOffset.toInt() / 13))

                    yOffset += 13f * module.animation.getAnimationFactor().toFloat()
                }

                RenderUtil.popScissor()
            }

            Corner.TOP_RIGHT -> {
                RenderUtil.pushScissor(((x + width) - scissorWidth), y.toDouble(), scissorWidth, enabledModules.size * 13.0)

                var yOffset = y

                for (module in enabledModules) {
                    val moduleData = module.name + (if (module.getData() == "") "" else " $GRAY[$WHITE${module.getData()}$GRAY]")

                    val origin = x + width

                    if (background.value) {
                        RenderUtil.drawRect((origin - getStringWidth(moduleData) * module.animation.getAnimationFactor()).toFloat(), yOffset, getStringWidth(moduleData), 13f, 0x70000000)
                    }

                    renderText(moduleData, (origin - getStringWidth(moduleData) * module.animation.getAnimationFactor()).toFloat(), yOffset + 2, arrayListColour.value.getColour(yOffset.toInt() / 13))

                    yOffset += 13f * module.animation.getAnimationFactor().toFloat()
                }

                RenderUtil.popScissor()
            }

            Corner.BOTTOM_LEFT -> {
                RenderUtil.pushScissor(x.toDouble(), (y + height) - enabledModules.size * 13.0, scissorWidth,enabledModules.size * 13.0)
                var yOffset = y + height - 13f

                for (module in enabledModules) {
                    val moduleData = module.name + (if (module.getData() == "") "" else " $GRAY[$WHITE${module.getData()}$GRAY]")
                    val width = getStringWidth(moduleData) + 4

                    val origin = x - getStringWidth(moduleData)

                    if (background.value) {
                        RenderUtil.drawRect((origin + width * module.animation.getAnimationFactor()).toFloat(), yOffset, width, 13f, 0x70000000)
                    }

                    renderText(moduleData, (origin + width * module.animation.getAnimationFactor()).toFloat(), yOffset + 2, arrayListColour.value.getColour(yOffset.toInt() / 13))

                    yOffset -= 13f * module.animation.getAnimationFactor().toFloat()
                }

                RenderUtil.popScissor()
            }

            Corner.BOTTOM_RIGHT -> {
                RenderUtil.pushScissor(((x + width) - scissorWidth), (y + height) - enabledModules.size * 13.0, scissorWidth,enabledModules.size * 13.0)

                var yOffset = y + height - 13f

                for (module in enabledModules) {
                    val moduleData = module.name + (if (module.getData() == "") "" else " $GRAY[$WHITE${module.getData()}$GRAY]")

                    val origin = x + width

                    if (background.value) {
                        RenderUtil.drawRect((origin - getStringWidth(moduleData) * module.animation.getAnimationFactor()).toFloat(), yOffset, getStringWidth(moduleData), 13f, 0x70000000)
                    }

                    renderText(moduleData, (origin - getStringWidth(moduleData) * module.animation.getAnimationFactor()).toFloat(), yOffset + 2, arrayListColour.value.getColour(yOffset.toInt() / 13))

                    yOffset -= 13f * module.animation.getAnimationFactor().toFloat()
                }

                RenderUtil.popScissor()
            }
        }
    }

    override fun getWidth(): Float {
        return 56f
    }

    override fun getHeight(): Float {
        return 56f
    }

    enum class ArrayListColour(private val colour: Function<Int, Int>) {
        /**
         * The colour is slightly different for each module in the array list
         */
        RAINBOW_WAVE(Function { addition: Int? ->
            ColourUtil.getRainbow(
                Colours.mainColour.rainbowSpeed,
                Colours.mainColour.rainbowSaturation / 100f,
                addition!!
            )
        }),

        /**
         * Permanent static colour
         */
        SYNC(Function { addition: Int? -> Colours.mainColour.value.rgb });

        /**
         * Gets the colour
         *
         * @param addition The addition to the colour
         * @return The colour
         */
        fun getColour(addition: Int): Int {
            return colour.apply(addition)
        }
    }

    enum class Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
}