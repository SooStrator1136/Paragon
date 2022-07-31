package com.paragon.client.ui.configuration.discord.settings.impl

import com.paragon.api.setting.Setting
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.ui.configuration.discord.settings.DiscordSetting
import com.paragon.client.ui.util.Click
import org.lwjgl.util.Rectangle

/**
 * @author SooStrator1136
 */
class DiscordNumber(val setting: Setting<Number>) : DiscordSetting(setting) {

    private val minusRect = Rectangle()
    private val valueRect = Rectangle()
    private val plusRect = Rectangle()

    var dragging = false

    init {
        bounds.height = (fontHeight + msgStyleHeight).toInt()
    }

    override fun render(mouseX: Int, mouseY: Int) {
        super.render(mouseX, mouseY)

        val y = bounds.y + msgStyleHeight

        //Basic bounds
        run {
            minusRect.setBounds(
                bounds.x,
                y.toInt(),
                getStringWidth("<").toInt(),
                fontHeight.toInt()
            )
            valueRect.setBounds(
                minusRect.x + minusRect.width + 2,
                y.toInt(),
                getStringWidth(setting.value.toString()).toInt(),
                fontHeight.toInt()
            )
            plusRect.setBounds(
                valueRect.x + valueRect.width + 2,
                y.toInt(),
                getStringWidth(">").toInt(),
                fontHeight.toInt()
            )
        }

        //Render < value >
        run {
            renderText(
                "<",
                minusRect.x.toFloat(),
                minusRect.y.toFloat(),
                if (minusRect.contains(mouseX, mouseY)) Colours.mainColour.value.rgb else -1
            )

            renderText(
                setting.value.toString(),
                valueRect.x.toFloat(),
                valueRect.y.toFloat(),
                -1
            )

            renderText(
                ">",
                plusRect.x.toFloat(),
                plusRect.y.toFloat(),
                if (plusRect.contains(mouseX, mouseY)) Colours.mainColour.value.rgb else -1
            )
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (!bounds.contains(mouseX, mouseY) || button != Click.LEFT.button) {
            return
        }

        if (minusRect.contains(mouseX, mouseY)) {
            val newVal = setting.value - setting.incrementation!!
            setting.setValue(if (newVal > setting.min!!) newVal else setting.min!!)
        } else if (plusRect.contains(mouseX, mouseY)) {
            val newVal = setting.value + setting.incrementation!!
            setting.setValue(if (newVal < setting.max!!) newVal else setting.max!!)
        }
    }

    override fun onKey(keyCode: Int) {}

}

private operator fun Number.compareTo(compareTo: Number): Int {
    return when (this) {
        is Float -> this.toFloat().compareTo(compareTo.toFloat())
        is Double -> this.toDouble().compareTo(compareTo.toDouble())
        else -> 0 //Shouldn't be reached since setting are supposed to me float or double
    }
}

private operator fun Number.minus(toSubtract: Number): Number {
    return when (this) {
        is Float -> this.toFloat().minus(toSubtract.toFloat())
        is Double -> this.toDouble().minus(toSubtract.toFloat())
        else -> 0 //Shouldn't be reached since setting are supposed to me float or double
    }
}

private operator fun Number.plus(toAdd: Number): Number {
    return when (this) {
        is Float -> this.toFloat().plus(toAdd.toFloat())
        is Double -> this.toDouble().plus(toAdd.toFloat())
        else -> 0 //Shouldn't be reached since setting are supposed to me float or double
    }
}
