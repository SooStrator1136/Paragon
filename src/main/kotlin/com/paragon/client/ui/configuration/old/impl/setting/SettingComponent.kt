package com.paragon.client.ui.configuration.old.impl.setting

import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.client.systems.module.impl.client.ClickGUI.animationSpeed
import com.paragon.client.systems.module.impl.client.ClickGUI.easing
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.old.OldPanelGUI
import com.paragon.client.ui.configuration.old.OldPanelGUI.Companion.isInside
import com.paragon.client.ui.configuration.old.impl.module.ModuleButton
import me.surge.animation.Animation
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.function.Consumer

/**
 * @author Wolfsurge
 */
open class SettingComponent<T>(val moduleButton: ModuleButton, val setting: Setting<T>, var offset: Float, val height: Float) {

    /**
     * Gets the list of setting components
     *
     * @return The setting components
     */
    val settingComponents = ArrayList<SettingComponent<*>>()
    var animation: Animation

    /**
     * Gets the offset
     *
     * @return The offset
     */

    init {
        var settingOffset = offset
        if (!setting.subsettings.isEmpty()) {
            for (setting1 in setting.subsettings) {
                if (setting1.value is Boolean) {
                    settingComponents.add(BooleanComponent(moduleButton, setting1 as Setting<Boolean>, settingOffset, height))
                    settingOffset += height
                }
                else if (setting1.value is Bind) {
                    settingComponents.add(KeybindComponent(moduleButton, setting1 as Setting<Bind?>, settingOffset, height))
                    settingOffset += height
                }
                else if (setting1.value is Number) {
                    settingComponents.add(SliderComponent(moduleButton, setting1 as Setting<Number?>, settingOffset, height))
                    settingOffset += height
                }
                else if (setting1.value is Enum<*>) {
                    settingComponents.add(ModeComponent(moduleButton, setting1 as Setting<Enum<*>>, settingOffset, height))
                    settingOffset += height
                }
                else if (setting1.value is Color) {
                    settingComponents.add(ColourComponent(moduleButton, setting1 as Setting<Color>, settingOffset, height))
                    settingOffset += height
                }
            }
        }
        animation = Animation(animationSpeed::value, false, easing::value)
    }

    open fun renderSetting(mouseX: Int, mouseY: Int) {
        if (!settingComponents.isEmpty() && hasVisibleSubsettings() && this !is ModeComponent) {
            GL11.glPushMatrix()
            GL11.glScalef(0.5f, 0.5f, 0.5f)
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("...", (moduleButton.panel.x + moduleButton.panel.width - 6.5f) * 2, (moduleButton.offset + offset + 4f) * 2, -1)
            GL11.glPopMatrix()
        }
        if (animation.getAnimationFactor() > 0) {
            settingComponents.forEach(Consumer { settingComponent: SettingComponent<*> ->
                if (settingComponent.setting.isVisible()) {
                    settingComponent.renderSetting(mouseX, mouseY)
                }
            })
            for (settingComponent in settingComponents) {
                if (settingComponent.setting.isVisible()) {
                    drawRect(moduleButton.panel.x, moduleButton.offset + settingComponent.offset, 2f, if (settingComponent is ColourComponent) height else settingComponent.height, Colours.mainColour.value.rgb)
                }
            }
        }
        if (isMouseOver(mouseX, mouseY) && this !is ColourComponent) {
            OldPanelGUI.tooltip = setting.description
        }
        else if (this is ColourComponent) {
            if (isInside(moduleButton.panel.x, moduleButton.offset + offset, moduleButton.panel.x + moduleButton.panel.width, moduleButton.offset + offset + 13, mouseX, mouseY)) {
                OldPanelGUI.tooltip = setting.description
            }
        }
    }

    fun hasVisibleSubsettings(): Boolean {
        for (settingComponent in settingComponents) {
            if (settingComponent.setting.isVisible()) {
                return true
            }
        }
        return false
    }

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 1) {
            if (isMouseOver(mouseX, mouseY)) {
                animation.state = !isExpanded
            }
        }
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {}
    open fun keyTyped(typedChar: Char, keyCode: Int) {
        for (settingComponent in settingComponents) {
            if (settingComponent.setting.isVisible()) {
                settingComponent.keyTyped(typedChar, keyCode)
            }
        }
    }

    fun isMouseOver(mouseX: Int, mouseY: Int): Boolean {
        return isInside(moduleButton.panel.x, moduleButton.offset + offset, moduleButton.panel.x + moduleButton.panel.width, moduleButton.offset + offset + height, mouseX, mouseY)
    }


    open val isExpanded: Boolean
        get() = animation.getAnimationFactor() > 0// return getHeight() + (expanded ? subsettingHeight : 0);


    open val absoluteHeight: Float
        get() {
            var subsettingHeight = 0f

            for (settingComponent in settingComponents) {
                subsettingHeight += settingComponent.height
            }

            return if (isExpanded) height + subsettingHeight else height
        }
}