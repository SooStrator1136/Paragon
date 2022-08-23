package com.paragon.client.ui.configuration.zeroday.panel

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.util.render.RenderUtil.drawHorizontalGradientRoundedRect
import com.paragon.api.util.render.RenderUtil.drawRoundedRect
import com.paragon.api.util.render.RenderUtil.popScissor
import com.paragon.api.util.render.RenderUtil.pushScissor
import com.paragon.api.util.render.RenderUtil.renderItemStack
import com.paragon.api.util.render.RenderUtil.screenWidth
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.systems.module.impl.client.ClickGUI.animationSpeed
import com.paragon.client.systems.module.impl.client.ClickGUI.easing
import com.paragon.client.systems.module.impl.client.ClickGUI.icon
import com.paragon.client.systems.module.impl.client.ClickGUI.radius
import com.paragon.client.ui.configuration.zeroday.element.Element
import com.paragon.client.ui.configuration.zeroday.element.module.ModuleElement
import com.paragon.client.ui.configuration.zeroday.element.setting.*
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import net.minecraft.item.ItemStack
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.TextFormatting
import org.lwjgl.opengl.GL11.glScalef
import org.lwjgl.opengl.GL11.glTranslated
import java.awt.Color
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

class CategoryPanel(private val category: Category, x: Float, y: Float, width: Float, height: Float, private val barHeight: Float) : Panel(x, y, width, height) {
    private val elements: MutableList<Element> = ArrayList()

    var leftHue = 0f
        private set

    private val expand = Animation(animationSpeed::value, true, easing::value)

    private var open = true
    private var scrollFactor = 0f

    var moduleHeight = 0f
        private set

    var scissorHeight = 0f
        private set

    private var dragging = false

    init {
        this.parent = this

        var offset = y + barHeight
        for (module in Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { module: Module -> module.category == category }) {
            elements.add(ModuleElement(this, module, x, offset, width, 16f))
            offset += 16f
        }
    }

    override fun render(mouseX: Int, mouseY: Int, dWheel: Int) {
        hover.state = isHoveredOverBar(mouseX, mouseY)

        if (dragging) {
            x = mouseX - lastX
            y = mouseY - lastY
            scrollFactor = (if (y < lastY) 0 else 1).toFloat()
        }

        leftHue = x / screenWidth * 360
        var rightHue = (x + width) / screenWidth * 360

        leftHue /= 5f
        leftHue += 270f
        rightHue /= 5f
        rightHue += 270f
        leftHue = MathHelper.clamp(leftHue, 0f, 360f)
        rightHue = MathHelper.clamp(rightHue, 0f, 360f)

        drawHorizontalGradientRoundedRect(x.toDouble(), y.toDouble(), width.toDouble(), barHeight.toDouble(), radius.value.toDouble(), radius.value.toDouble(), 1.0, 1.0, Color.HSBtoRGB(leftHue / 360, 1f, (0.75f + 0.25f * hover.getAnimationFactor()).toFloat()), Color.HSBtoRGB(rightHue / 360, 1f, (0.75f + 0.25f * hover.getAnimationFactor()).toFloat()))

        var titleOffset = 5f
        if (icon.value != ClickGUI.Icon.NONE) {
            if (icon.value == ClickGUI.Icon.BACKGROUND) {
                drawRoundedRect(x.toDouble(), y.toDouble(), barHeight.toDouble(), barHeight.toDouble(), radius.value.toDouble(), 1.0, 1.0, 1.0, -0x70000000)
            }

            // Eye of ender is offset weirdly...
            if (this.category === Category.RENDER) {
                glTranslated(-0.5, -0.5, 0.0)
            }

            renderItemStack(ItemStack(this.category.indicator), x + 3.5f, y + 3.5f, false)

            if (this.category === Category.RENDER) {
                glTranslated(0.5, 0.5, 0.0)
            }

            titleOffset = 30f
        }

        glScalef(1.25f, 1.25f, 1.25f)
        run {
            val scaleFactor = 1 / 1.25f
            drawStringWithShadow(category.Name, (x + titleOffset) * scaleFactor, (y + barHeight * scaleFactor / 2 - 2f) * scaleFactor, -0x1)
            glScalef(scaleFactor, scaleFactor, scaleFactor)
        }

        val lastModuleElement = elements[elements.size - 1]
        val lastElement = if (lastModuleElement.animation.getAnimationFactor() > 0) lastModuleElement.subElements[lastModuleElement.subElements.size - 1] else lastModuleElement
        var height = 0f

        for (element in elements) {
            height += element.getTotalHeight()
        }

        moduleHeight = height
        if (dWheel != 0 && isHovered(x, y + barHeight, width, moduleHeight, mouseX, mouseY)) {
            scrollFactor = (if (dWheel > 0) -2 else 2).toFloat()
        }

        else {
            if (scrollFactor != 0f) {
                scrollFactor *= 0.9f
                if (scrollFactor < 0.1 && scrollFactor > -0.1) {
                    scrollFactor = 0f
                }
            }
        }

        scissorHeight = MathHelper.clamp(MathHelper.clamp(moduleHeight, 0f, lastElement.y + lastElement.height - (y + barHeight)) * expand.getAnimationFactor(), 0.0, 352.0).toFloat()

        if (scrollFactor != 0f) {
            if (lastElement.y + lastElement.getTotalHeight() > y + barHeight + scissorHeight) {
                elements.forEach(Consumer { element: Element -> element.y = element.y + scrollFactor })
            }

            else {
                scrollFactor = 0f
                for (element in elements) {
                    element.y = element.y + 1
                }
            }
        }

        if (lastElement.y + lastElement.getTotalHeight() < y + barHeight + scissorHeight) {
            for (element in elements) {
                element.y = element.y + 1
            }
        }

        if (lastElement.y + lastElement.height < y + barHeight + moduleHeight && moduleHeight <= 352 && !isElementVisible(elements[0])) {
            elements[0].y = elements[0].y + 1
        }

        if (isElementVisible(lastElement) && lastElement.y < scissorHeight) {
            for (element in elements) {
                element.y = element.y + 1
            }
        }

        if (lastElement.y + lastElement.height < y + barHeight + MathHelper.clamp(moduleHeight, 0f, 352f) && moduleHeight > 352) {
            scrollFactor = 0f
            elements[0].y = elements[0].y + 1
        }

        if (isElementVisible(elements[0]) && isElementVisible(lastElement) || elements[0].y > y + barHeight) {
            scrollFactor = 0f
            elements[0].y = y + barHeight
        }

        var offset = elements[0].y
        for (element in elements) {
            element.x = x
            element.y = offset
            offset += element.getTotalHeight()
        }

        drawHorizontalGradientRoundedRect(x.toDouble(), (y + barHeight + scissorHeight).toDouble(), width.toDouble(), 2.0, 1.0, 1.0, MathHelper.clamp(radius.value, 1f, 2f).toDouble(), MathHelper.clamp(radius.value, 1f, 2f).toDouble(), Color.HSBtoRGB(leftHue / 360, 1f, (0.75f + 0.25f * hover.getAnimationFactor()).toFloat()), Color.HSBtoRGB(rightHue / 360, 1f, (0.75f + 0.25f * hover.getAnimationFactor()).toFloat()))

        pushScissor(x.toDouble(), y * Paragon.INSTANCE.zerodayGUI.animation.getAnimationFactor() + barHeight * Paragon.INSTANCE.zerodayGUI.animation.getAnimationFactor(), width.toDouble(), scissorHeight.toDouble())

        for (element in elements) {
            element.render(mouseX, mouseY, dWheel)
        }

        popScissor()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: Click) {
        if (isHoveredOverBar(mouseX, mouseY)) {
            if (click == Click.LEFT) {
                lastX = mouseX - x
                lastY = mouseY - y
                dragging = true
            }
            else if (click == Click.RIGHT) {
                open = !open
                expand.state = open
            }
        }
        if (open) {
            elements.forEach(Consumer { element: Element -> element.mouseClicked(mouseX, mouseY, click) })
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, click: Click) {
        dragging = false
        if (open) {
            elements.forEach(Consumer { element: Element -> element.mouseReleased(mouseX, mouseY, click) })
        }
    }

    override fun keyTyped(keyCode: Int, keyChar: Char) {
        if (open) {
            elements.forEach(Consumer { element: Element -> element.keyTyped(keyCode, keyChar) })
        }
    }

    fun isElementVisible(element: Element): Boolean {
        return element.y >= y + barHeight - 1 && element.y + element.height < y + 16 + barHeight + MathHelper.clamp(moduleHeight, 0f, 352f)
    }

    private fun isHoveredOverBar(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + barHeight
    }

    val tooltip: String
        get() {
            val tooltip = AtomicReference("")
            if (expand.getAnimationFactor() > 0) {
                elements.forEach { element ->
                    if (element is ModuleElement) {
                        run {
                            if (element.hover.getAnimationFactor() > 0 && isElementVisible(element)) {
                                val description = element.module.description + TextFormatting.GRAY + "   [Bind: " + element.module.bind.value.getButtonName() + "]"
                                tooltip.set(description)
                            }
                        }

                        element.subElements.forEach(Consumer { subElement: Element ->
                            if (!isElementVisible(subElement)) {
                                return@Consumer
                            }

                            var hover = 0.0
                            var description = ""
                            var visible = true

                            if (subElement is BooleanElement) {
                                val sElement = subElement
                                description = sElement.setting.description
                                hover = sElement.hover.getAnimationFactor()
                                visible = sElement.setting.isVisible()
                            }
                            else if (subElement is SliderElement) {
                                val sElement = subElement
                                description = sElement.setting.description
                                hover = sElement.hover.getAnimationFactor()
                                visible = sElement.setting.isVisible()
                            }
                            else if (subElement is EnumElement) {
                                val sElement = subElement
                                description = sElement.setting.description
                                hover = sElement.hover.getAnimationFactor()
                                visible = sElement.setting.isVisible()
                            }
                            else if (subElement is ColourElement) {
                                val sElement = subElement
                                description = sElement.setting.description
                                hover = sElement.hover.getAnimationFactor()
                                visible = sElement.setting.isVisible()
                            }
                            else if (subElement is BindElement) {
                                val sElement = subElement
                                description = sElement.setting.description
                                hover = sElement.hover.getAnimationFactor()
                                visible = sElement.setting.isVisible()
                            }

                            if (hover > 0 && visible) {
                                tooltip.set(description)
                            }

                            subElement.subElements.forEach(Consumer { subSubElement: Element? ->
                                var subHover = 0.0
                                var subDesc = ""
                                var subVisible = true

                                if (subSubElement is BooleanElement) {
                                    val ssElement = subSubElement
                                    subDesc = ssElement.setting.description
                                    subHover = ssElement.hover.getAnimationFactor()
                                    subVisible = ssElement.setting.isVisible()
                                }
                                else if (subSubElement is SliderElement) {
                                    val ssElement = subSubElement
                                    subDesc = ssElement.setting.description
                                    subHover = ssElement.hover.getAnimationFactor()
                                    subVisible = ssElement.setting.isVisible()
                                }
                                else if (subSubElement is EnumElement) {
                                    val ssElement = subSubElement
                                    subDesc = ssElement.setting.description
                                    subHover = ssElement.hover.getAnimationFactor()
                                    subVisible = ssElement.setting.isVisible()
                                }
                                else if (subSubElement is ColourElement) {
                                    val ssElement = subSubElement
                                    subDesc = ssElement.setting.description
                                    subHover = ssElement.hover.getAnimationFactor()
                                    subVisible = ssElement.setting.isVisible()
                                }
                                else if (subSubElement is BindElement) {
                                    val ssElement = subSubElement
                                    subDesc = ssElement.setting.description
                                    subHover = ssElement.hover.getAnimationFactor()
                                    subVisible = ssElement.setting.isVisible()
                                }

                                if (subHover == 0.0 || !subVisible) {
                                    return@Consumer
                                }

                                tooltip.set(subDesc)
                            })
                        })
                    }
                }
            }
            return tooltip.get()
        }
}