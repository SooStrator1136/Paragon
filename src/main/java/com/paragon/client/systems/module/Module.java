package com.paragon.client.systems.module;

import com.paragon.Paragon;
import com.paragon.api.event.client.ModuleToggleEvent;
import com.paragon.api.util.Wrapper;
import com.paragon.client.systems.feature.Feature;
import com.paragon.client.systems.module.hud.impl.HArrayList;
import com.paragon.client.systems.ui.animation.Animation;
import com.paragon.client.systems.module.impl.client.HUD;
import com.paragon.client.systems.module.settings.Setting;
import com.paragon.client.systems.module.settings.impl.KeybindSetting;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Module extends Feature implements Wrapper {

    // The category the module is in
    private ModuleCategory category;

    // Whether the module is enabled
    private boolean enabled;

    // Whether the module is visible in the Array List or not
    private boolean visible = true;

    // Module Settings
    private final List<Setting> settings = new ArrayList<>();
    private final KeybindSetting keyCode = new KeybindSetting("Keybind", "The keybind of the module", 0);

    // Arraylist animation
    public Animation animation = new Animation(100, false);

    public Module(String name, ModuleCategory category, String description) {
        super(name, description);
        this.category = category;
        addSettings(keyCode);
    }

    public Module(String name, ModuleCategory category, String description, int keyBind) {
        super(name, description);
        this.category = category;
        this.keyCode.setKeyCode(keyBind);
        addSettings(keyCode);
    }

    /**
     * Add settings to the module
     * @param settings An undefined amount of settings to add
     */
    public void addSettings(Setting... settings) {
        this.settings.addAll(Arrays.asList(settings)); // Add settings
        this.settings.sort(Comparator.comparingInt(s -> s == keyCode ? 1 : 0)); // Make keybind be last
    }

    public void onEnable() {}
    public void onDisable() {}

    public void onTick() {}
    public void onRender2D() {}
    public void onRender3D() {}

    /**
     * Toggles the module
     */
    public void toggle() {
        this.enabled = !enabled;

        ModuleToggleEvent moduleToggleEvent = new ModuleToggleEvent(this);
        Paragon.INSTANCE.getEventBus().post(moduleToggleEvent);

        if (moduleToggleEvent.isCancelled()) {
            return;
        }

        if (enabled) {
            // Register events
            MinecraftForge.EVENT_BUS.register(this);
            Paragon.INSTANCE.getEventBus().register(this);

            animation.time = HArrayList.animationSpeed.getValue();
            animation.setState(true);

            // Call onEnable
            onEnable();
        }
        else {
            // Unregister events
            MinecraftForge.EVENT_BUS.unregister(this);
            Paragon.INSTANCE.getEventBus().unregister(this);

            animation.time = HArrayList.animationSpeed.getValue();
            animation.setState(false);

            // Call onDisable
            onDisable();
        }
    }

    /**
     * Gets the module info for the array list
     * @return The module's info
     */
    public String getModuleInfo() {
        return "";
    }

    /**
     * Gets the module's category
     * @return The module's category
     */
    public ModuleCategory getCategory() {
        return category;
    }

    /**
     * Gets whether the module is enabled
     * @return Whether the module is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the module's visibility
     * @return The module's visibility
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the module's visibility
     * @param visible The module's new visibility
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Gets a list of the module's settings
     * @return The module's settings
     */
    public List<Setting> getSettings() {
        return settings;
    }

    /**
     * Gets the key code of the module
     * @return The key code of the module
     */
    public KeybindSetting getKeyCode() {
        return keyCode;
    }
}
