package com.paragon.client.ui.configuration.old.impl.module

import com.paragon.api.module.Module
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.client.systems.module.impl.client.ClickGUI.animationSpeed
import com.paragon.client.systems.module.impl.client.ClickGUI.easing
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.old.OldPanelGUI
import com.paragon.client.ui.configuration.old.OldPanelGUI.Companion.isInside
import com.paragon.client.ui.configuration.old.impl.Panel
import com.paragon.client.ui.configuration.old.impl.setting.*
import me.surge.animation.Animation
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.function.Consumer

/**
 * @author Wolfsurge
 */
class ModuleButton(

    // The parent panel
    val panel: Panel,

    // The module
    val module: Module,

    // The offset and height
    var offset: Float,

    var height: Float
) {
    // Opening / Closing animation
    private val animation: Animation

    // A list of all setting components
    private val settingComponents = ArrayList<SettingComponent<*>>()

    init {
        var settingOffset = height

        // Add settings. Please make a PR if you want to make this look better.
        for (setting in module.settings) {
            if (setting.value is Boolean) {
                settingComponents.add(BooleanComponent(this, setting as Setting<Boolean>, settingOffset, height))
                settingOffset += height
            }
            else if (setting.value is Bind) {
                settingComponents.add(KeybindComponent(this, setting as Setting<Bind?>, settingOffset, height))
                settingOffset += height
            }
            else if (setting.value is Number) {
                settingComponents.add(SliderComponent(this, setting as Setting<Number?>, settingOffset, height))
                settingOffset += height
            }
            else if (setting.value is Enum<*>) {
                settingComponents.add(ModeComponent(this, setting as Setting<Enum<*>>, settingOffset, height))
                settingOffset += height
            }
            else if (setting.value is Color) {
                settingComponents.add(ColourComponent(this, setting as Setting<Color>, settingOffset, height))
                settingOffset += height
            }
        }
        animation = Animation(animationSpeed::value, false, easing::value)
    }

    fun renderModuleButton(mouseX: Int, mouseY: Int) {
        // Header
        drawRect(panel.x, offset, panel.width, height, if (isMouseOver(mouseX, mouseY)) Color(23, 23, 23).brighter().rgb else Color(23, 23, 23).rgb)
        GL11.glPushMatrix()
        // Scale it
        GL11.glScalef(0.8f, 0.8f, 0.8f)
        val scaleFactor = 1.25f

        // Render the module's name
        drawStringWithShadow(module.name, (panel.x + 3) * scaleFactor, (offset + 4f) * scaleFactor, if (module.isEnabled) Colours.mainColour.value.rgb else -1)

        // Render some dots at the side if we have more settings than just the keybind
        if (module.settings.size > 1) {
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("...", (panel.x + panel.width - Minecraft.getMinecraft().fontRenderer.getStringWidth("...") - 1) * scaleFactor, (offset + 1.5f) * scaleFactor, -1)
        }
        GL11.glPopMatrix()

        // Refresh settings
        refreshSettingOffsets()
        if (animation.getAnimationFactor() > 0) {
            // Render settings
            settingComponents.forEach(Consumer { settingComponent: SettingComponent<*> ->
                if (settingComponent.setting.isVisible()) {
                    settingComponent.renderSetting(mouseX, mouseY)
                    drawRect(panel.x, offset + settingComponent.offset, 1f, if (settingComponent is ColourComponent) height else settingComponent.height, Colours.mainColour.value.rgb)
                }
            })
        }
        if (isMouseOver(mouseX, mouseY)) {
            OldPanelGUI.tooltip = module.description
        }
    }

    /**
     * Check if the mouse is over the module button
     *
     * @param mouseX The mouse's X
     * @param mouseY The mouse's Y
     * @return If the mouse is over the module button
     */
    fun isMouseOver(mouseX: Int, mouseY: Int): Boolean {
        return isInside(panel.x, offset, panel.x + panel.width, offset + height, mouseX, mouseY)
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            if (isMouseOver(mouseX, mouseY)) {
                // Toggle the module
                module.toggle()
            }
        }
        else if (mouseButton == 1) {
            if (isMouseOver(mouseX, mouseY)) {
                // Expand the settings
                animation.state = animation.getAnimationFactor() <= 0
            }
        }
        if (animation.getAnimationFactor() > 0) {
            // Mouse clicked
            for (settingComponent in settingComponents) {
                if (settingComponent.setting.isVisible()) {
                    settingComponent.mouseClicked(mouseX, mouseY, mouseButton)
                }
            }
        }
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (settingComponent in settingComponents) {
            if (settingComponent.setting.isVisible()) {
                settingComponent.mouseReleased(mouseX, mouseY, mouseButton)
            }
        }
    }

    fun keyTyped(keyTyped: Char, keyCode: Int) {
        for (settingComponent in settingComponents) {
            if (settingComponent.setting.isVisible()) {
                settingComponent.keyTyped(keyTyped, keyCode)
            }
        }
    }

    private fun refreshSettingOffsets() {
        var settingOffset = height

        // EW
        for (settingComponent in settingComponents) {
            if (settingComponent.setting.isVisible()) {
                settingComponent.offset = settingOffset
                settingOffset += (settingComponent.height * animation.getAnimationFactor()).toFloat()
                if (settingComponent.animation.getAnimationFactor() > 0) {
                    var subsettingOffset = settingComponent.offset + settingComponent.height
                    for (settingComponent1 in settingComponent.settingComponents) {
                        if (settingComponent1.setting.isVisible()) {
                            settingComponent1.offset = subsettingOffset
                            subsettingOffset += settingComponent1.height * settingComponent.animation.getAnimationFactor().toFloat()
                            settingOffset += (settingComponent1.height * settingComponent.animation.getAnimationFactor()).toFloat()
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the height of the button and it's settings
     *
     * @return The absolute height
     */
    val absoluteHeight: Float
        get() {
            var settingHeight = 0f
            for (settingComponent in settingComponents) {
                if (settingComponent.setting.isVisible()) {
                    settingHeight += settingComponent.height
                    if (settingComponent.animation.getAnimationFactor() > 0) {
                        for (settingComponent1 in settingComponent.settingComponents) {
                            if (settingComponent1.setting.isVisible()) {
                                settingHeight += (settingComponent1.height * settingComponent.animation.getAnimationFactor()).toFloat()
                            }
                        }
                    }
                }
            }
            return (height + settingHeight * animation.getAnimationFactor()).toFloat()
        }

    /**
     * Gets whether the component is expanded or not
     *
     * @return Whether the component is expanded or not
     */
    val isExpanded: Boolean
        get() = animation.getAnimationFactor() > 0
}