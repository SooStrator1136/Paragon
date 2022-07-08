package com.paragon.api.setting

import com.paragon.api.util.render.ColourUtil
import com.paragon.client.systems.module.impl.client.Colours
import java.awt.Color
import java.util.*
import java.util.function.Supplier

class Setting<T> {

    val subsettings = ArrayList<Setting<*>>()

    // Name and description of setting
    var name: String
        private set

    var description = ""
        private set

    // Value of the setting
    private var value: T

    // For numeric settings
    var min: T? = null
        private set

    var max: T? = null
        private set

    var incrementation: T? = null
        private set

    // For mode settings
    var index = 0

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

    constructor(name: String, value: T) {
        this.name = name
        this.value = value
        if (value is Color) {
            alpha = (value as Color).alpha.toFloat()
        }
    }

    constructor(name: String, value: T, min: T, max: T, incrementation: T) {
        this.name = name
        this.value = value
        this.min = min
        this.max = max
        this.incrementation = incrementation
    }

    /**
     * Sets the description of the setting.
     *
     * @param description the description of the setting.
     * @return this setting
     */
    fun setDescription(description: String): Setting<T> {
        this.description = description
        return this
    }

    /**
     * Gets the value of the setting.
     *
     * @return the value of the setting.
     */
    fun getValue(): T {
        if (value is Color) {
            if (isSync && this !== Colours.mainColour) {
                return Colours.mainColour.value as T
            }

            if (isRainbow) {
                return ColourUtil.integrateAlpha(Color(ColourUtil.getRainbow(rainbowSpeed, rainbowSaturation / 100, 0)), alpha) as T
            }
        }
        return value
    }

    /**
     * Sets the value of the setting.
     *
     * @param value the value of the setting.
     */
    fun setValue(value: T) {
        this.value = value
    }

    /**
     * Sets the minimum value of the setting.
     *
     * @param min the minimum value of the setting.
     */
    fun setMin(min: T) {
        this.min = min
    }

    /**
     * Sets the maximum value of the setting.
     *
     * @param max the maximum value of the setting.
     */
    fun setMax(max: T) {
        this.max = max
    }

    /**
     * Sets the incrementation of the setting.
     *
     * @param incrementation the incrementation of the setting.
     */
    fun setIncrementation(incrementation: T) {
        this.incrementation = incrementation
    }

    /**
     * Sets the parent setting of the setting.
     *
     * @param parentSetting the parent setting of the setting.
     * @return this setting
     */
    fun setParentSetting(parentSetting: Setting<*>?): Setting<T> {
        this.parentSetting = parentSetting
        this.parentSetting!!.subsettings.add(this)
        return this
    }

    /**
     * Gets the visibility of the setting.
     *
     * @return the visibility of the setting.
     */
    fun isVisible(): Boolean {
        return isVisible.get()
    }

    /**
     * Sets the visibility of the setting.
     *
     * @param isVisible the visibility of the setting.
     * @return this setting
     */
    fun setVisibility(isVisible: Supplier<Boolean>): Setting<T> {
        this.isVisible = isVisible
        return this
    }

    /**
     * Gets the next mode of the setting.
     *
     * @return the next mode of the setting.
     * @author linustouchtips
     */
    val nextMode: T
        get() {
            val enumeration: Enum<*> = value as Enum<*>
            val values: Array<String> =
                enumeration.javaClass.enumConstants.map { obj: Enum<*> -> obj.name }.toTypedArray()

            index = if (index + 1 > values.size - 1) 0 else index + 1

            return java.lang.Enum.valueOf(enumeration::class.java, values[index]) as T
        }
}