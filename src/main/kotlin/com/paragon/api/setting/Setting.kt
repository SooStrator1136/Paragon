package com.paragon.api.setting

import com.paragon.api.util.render.ColourUtil
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.client.systems.module.impl.client.Colours
import java.awt.Color
import java.util.function.Supplier

class Setting<T>(val name: String, value: T, val min: T = value, val max: T = value, val incrementation: T = value) {

    val subsettings = ArrayList<Setting<*>>()

    var description = ""
        private set

    // Value of the setting
    var value: T = value
        private set
        get() {
            if (field is Color) {
                if (isSync && this !== Colours.mainColour) {
                    return Colours.mainColour.value.integrateAlpha(alpha) as T
                }

                if (isRainbow) {
                    return Color(
                        ColourUtil.getRainbow(
                            rainbowSpeed,
                            rainbowSaturation / 100,
                            0
                        )
                    ).integrateAlpha(alpha) as T
                }

                return (field as Color).integrateAlpha(alpha) as T
            }

            return field
        }

    // For mode settings
    var index = -1

    // For colour settings
    var alpha = 0f

    var isRainbow = false

    var rainbowSpeed = 4f

    var rainbowSaturation = 100f

    var isSync = false

    // Subsettings
    var parentSetting: Setting<*>? = null
        private set

    // GUI Visibility
    private var isVisible = Supplier { true }

    init {
        if (value is Color) {
            alpha = (value as Color).alpha.toFloat()
        }
    }

    /**
     * Sets the value of the setting.
     *
     * @param value the value of the setting.
     */
    fun setValue(value: T) {
        if (value is Enum<*>) {
            index = nextIndex
        }

        this.value = value
    }

    /**
     * Sets the value without updating anything else
     *
     * @param value the value of the setting.
     */
    fun setValueRaw(value: T) {
        this.value = value
    }

    /**
     * Gets the visibility of the setting.
     *
     * @return the visibility of the setting.
     */
    fun isVisible() = isVisible.get()

    /**
     * Gets the next mode of the setting.
     *
     * @return the next mode of the setting.
     * @author linustouchtips
     */
    val nextMode: T
        get() {
            val enumeration = value as Enum<*>
            val values = enumeration.javaClass.enumConstants.map { it.name }.toTypedArray()

            return java.lang.Enum.valueOf(enumeration::class.java, values[nextIndex]) as T
        }

    private val nextIndex: Int
        get() {
            val enumeration = value as Enum<*>
            val values = enumeration.javaClass.enumConstants.map { it.name }.toTypedArray()

            return if (index + 1 > values.size - 1) 0 else index + 1
        }

    infix fun describedBy(description: String): Setting<T> {
        this.description = description
        return this
    }

    infix fun visibleWhen(visibility: Supplier<Boolean>): Setting<T> {
        this.isVisible = visibility
        return this
    }

    infix fun subOf(parent: Setting<*>?): Setting<T> {
        this.parentSetting = parent
        this.parentSetting!!.subsettings.add(this)
        return this
    }

}