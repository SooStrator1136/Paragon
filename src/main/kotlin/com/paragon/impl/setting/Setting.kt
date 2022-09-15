package com.paragon.impl.setting

import com.paragon.impl.module.client.Colours
import com.paragon.util.render.ColourUtil
import com.paragon.util.render.ColourUtil.integrateAlpha
import java.awt.Color

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
                            rainbowSpeed, rainbowSaturation / 100, 0
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
    private var isVisible = { true }

    init {
        if (value is Color) {
            alpha = (value as Color).alpha.toFloat()
        }
    }

    /**
     * Sets the description of the setting.
     *
     * @param description the description of the setting.
     * @return this setting
     */
    private fun setDescription(description: String): Setting<T> {
        this.description = description
        return this
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
     * Sets the parent setting of the setting.
     *
     * @param parentSetting the parent setting of the setting.
     * @return this setting
     */
    private fun setParentSetting(parentSetting: Setting<*>?): Setting<T> {
        this.parentSetting = parentSetting
        this.parentSetting!!.subsettings.add(this)
        return this
    }

    /**
     * Gets the visibility of the setting.
     *
     * @return the visibility of the setting.
     */
    fun isVisible() = isVisible.invoke()

    /**
     * Sets the visibility of the setting.
     *
     * @param isVisible the visibility of the setting.
     * @return this setting
     */
    private fun setVisibility(isVisible: () -> Boolean): Setting<T> {
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

    infix fun describedBy(description: String) = setDescription(description)
    infix fun visibleWhen(isVisible: () -> Boolean) = setVisibility(isVisible)
    infix fun subOf(parent: Setting<*>?) = setParentSetting(parent)

}