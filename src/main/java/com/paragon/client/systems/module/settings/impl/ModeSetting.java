package com.paragon.client.systems.module.settings.impl;

import com.paragon.client.systems.module.settings.Setting;

import java.util.Arrays;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class ModeSetting<T> extends Setting {

    private T currentMode;
    private int index;
    private Supplier<String> modeDescription = () -> "";

    public ModeSetting(String name, String description, T defaultMode) {
        setName(name);
        setDescription(description);
        setCurrentMode(defaultMode);
    }

    /**
     * Sets the mode to the next one
     */
    public void cycleMode() {
        Enum<?> enumVal = (Enum<?>) currentMode;

        String[] values = Arrays.stream(enumVal.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);
        index = index + 1 > values.length - 1 ? 0 : index + 1;

        T newMode =  (T) Enum.valueOf(enumVal.getClass(), values[index]);

        setCurrentMode(newMode);
    }

    /**
     * Gets the current mode
     * @return The current mode
     */
    public T getCurrentMode() {
        return currentMode;
    }

    /**
     * Sets the current mode
     * @param newMode The new mode
     */
    public void setCurrentMode(T newMode) {
        this.currentMode = newMode;
    }

    public Supplier<String> getModeDescription() {
        return modeDescription;
    }

    public ModeSetting<T> setModeDescription(Supplier<String> modeDescription) {
        this.modeDescription = modeDescription;
        return this;
    }
}
