package com.paragon.client.systems.module.setting;

import com.paragon.api.util.render.ColourUtil;
import com.paragon.client.systems.module.impl.client.Colours;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class Setting<T> {

    // Name and description of setting
    private String name;
    private String description = "";

    // Value of the setting
    private T value;

    // For numeric settings
    private T min;
    private T max;
    private T incrementation;

    // For mode settings
    private int index;

    // For colour settings
    private float alpha;
    private boolean rainbow;
    private float rainbowSpeed = 4;
    private float rainbowSaturation = 100;
    private boolean sync = false;

    // Subsettings
    private Setting<?> parentSetting;
    private final ArrayList<Setting<?>> subsettings = new ArrayList<>();

    // GUI Visibility
    private Supplier<Boolean> isVisible = () -> true;

    public Setting(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public Setting(String name, T value, T min, T max, T incrementation) {
        this.name = name;
        this.value = value;
        this.min = min;
        this.max = max;
        this.incrementation = incrementation;
    }

    /**
     * Gets the name of the setting.
     * @return the name of the setting.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description of the setting.
     * @return the description of the setting.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the setting.
     * @param description the description of the setting.
     * @return this setting
     */
    public Setting<T> setDescription(String description) {
        this.description = description;

        return this;
    }

    /**
     * Gets the value of the setting.
     * @return the value of the setting.
     */
    public T getValue() {
        if (value instanceof Color) {
            if (sync && this != Colours.mainColour) {
                return (T) Colours.mainColour.getValue();
            }

            if (rainbow) {
                return (T) ColourUtil.integrateAlpha(new Color(ColourUtil.getRainbow(rainbowSpeed, rainbowSaturation / 100, 0)), alpha);
            }
        }

        return value;
    }

    /**
     * Sets the value of the setting.
     * @param value the value of the setting.
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Gets the minimum value of the setting.
     * @return the minimum value of the setting.
     */
    public T getMin() {
        return min;
    }

    /**
     * Sets the minimum value of the setting.
     * @param min the minimum value of the setting.
     */
    public void setMin(T min) {
        this.min = min;
    }

    /**
     * Gets the maximum value of the setting.
     * @return the maximum value of the setting.
     */
    public T getMax() {
        return max;
    }

    /**
     * Sets the maximum value of the setting.
     * @param max the maximum value of the setting.
     */
    public void setMax(T max) {
        this.max = max;
    }

    /**
     * Gets the incrementation of the setting.
     * @return the incrementation of the setting.
     */
    public T getIncrementation() {
        return incrementation;
    }

    /**
     * Sets the incrementation of the setting.
     * @param incrementation the incrementation of the setting.
     */
    public void setIncrementation(T incrementation) {
        this.incrementation = incrementation;
    }

    /**
     * Gets the index of the setting.
     * @return the index of the setting.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index of the setting.
     * @param index the index of the setting.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Gets the parent setting of the setting.
     * @return the parent setting of the setting.
     */
    public Setting<?> getParentSetting() {
        return parentSetting;
    }

    /**
     * Sets the parent setting of the setting.
     * @param parentSetting the parent setting of the setting.
     * @return this setting
     */
    public Setting<T> setParentSetting(Setting<?> parentSetting) {
        this.parentSetting = parentSetting;

        this.parentSetting.getSubsettings().add(this);

        return this;
    }

    /**
     * Gets the children settings of the setting.
     * @return the children settings of the setting.
     */
    public ArrayList<Setting<?>> getSubsettings() {
        return subsettings;
    }

    /**
     * Gets the visibility of the setting.
     * @return the visibility of the setting.
     */
    public boolean isVisible() {
        return isVisible.get();
    }

    /**
     * Sets the visibility of the setting.
     * @param isVisible the visibility of the setting.
     * @return this setting
     */
    public Setting<T> setVisibility(Supplier<Boolean> isVisible) {
        this.isVisible = isVisible;

        return this;
    }

    /**
     * Gets the next mode of the setting.
     * @return the next mode of the setting.
     * @author linustouchtips
     */
    public T getNextMode() {
        Enum<?> enumeration = (Enum<?>) value;

        String[] values = Arrays.stream(enumeration.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);
        index = index + 1 > values.length - 1 ? 0 : index + 1;

        return (T) Enum.valueOf(enumeration.getClass(), values[index]);
    }

    /**
     * Gets the alpha of the setting
     * @return the alpha of the setting
     */
    public float getAlpha() {
        return alpha;
    }

    /**
     * Sets the alpha of the setting
     * @param alpha the alpha of the setting
     */
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    /**
     * Gets whether the colour is rainbow
     * @return Whether the colour is rainbow
     */
    public boolean isRainbow() {
        return rainbow;
    }

    /**
     * Gets whether the colour is synced to the client's colour
     * @return Whether the colour is synced
     */
    public boolean isSync() {
        return sync;
    }

    /**
     * Gets the rainbow's saturation
     * @return The rainbow's saturation
     */
    public float getRainbowSaturation() {
        return rainbowSaturation;
    }

    /**
     * Gets the rainbow's speed
     * @return Gets the rainbow's speed
     */
    public float getRainbowSpeed() {
        return rainbowSpeed;
    }

    /**
     * Sets the current colour to rainbow
     * @param rainbow Whether the colour is a rainbow
     */
    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    /**
     * Sets the speed of the rainbow
     * @param speed The speed
     */
    public void setRainbowSpeed(float speed) {
        this.rainbowSpeed = speed;
    }

    /**
     * Sets the saturation of the rainbow
     * @param saturation The saturation
     */
    public void setRainbowSaturation(float saturation) {
        this.rainbowSaturation = saturation;
    }

    /**
     * Sets whether the colour is synced to the client colour
     * @param sync Whether the colour is synced
     */
    public void setSync(boolean sync) {
        this.sync = sync;
    }
}
