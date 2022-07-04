package com.paragon.client.systems.module;

import com.paragon.Paragon;
import com.paragon.api.event.client.ModuleToggleEvent;
import com.paragon.api.util.Wrapper;
import com.paragon.client.systems.feature.Feature;
import com.paragon.client.systems.module.hud.impl.ArrayListHUD;
import com.paragon.client.systems.module.setting.Bind;
import com.paragon.client.systems.ui.animation.Animation;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Module extends Feature implements Wrapper {

    // The category the module is in
    private final Category category;

    // Whether the module is visible in the Array List or not
    private final Setting<Boolean> visible = new Setting<>("Visible", true)
            .setDescription("Whether the module is visible in the array list or not");

    // Whether the module is constantly enabled or not
    private final boolean constant = getClass().isAnnotationPresent(Constant.class);

    // Whether the module is ignored by notifications
    private final boolean ignoredByNotifications = getClass().isAnnotationPresent(IgnoredByNotifications.class);

    // Module Settings
    private final List<Setting<?>> settings = new ArrayList<>();
    private final Setting<Bind> bind = new Setting<>("Bind", new Bind(Keyboard.KEY_NONE, Bind.Device.KEYBOARD))
            .setDescription("The keybind of the module");

    // Arraylist animation
    public Animation animation = new Animation(() -> ArrayListHUD.animationSpeed.getValue(), false, ArrayListHUD.easing::getValue);

    // Whether the module is enabled
    private boolean enabled;

    public Module(String name, Category category, String description) {
        super(name, description);
        this.category = category;

        if (constant) {
            this.enabled = true;

            // Register events
            MinecraftForge.EVENT_BUS.register(this);
            Paragon.INSTANCE.getEventBus().register(this);
        }

        Arrays.stream(getClass().getDeclaredFields()).filter(field -> Setting.class.isAssignableFrom(field.getType())).forEach(field -> {
            field.setAccessible(true);

            try {
                Setting<?> setting = (Setting<?>) field.get(this);

                if (setting != null && setting.getParentSetting() == null) {
                    settings.add(setting);
                }
            } catch (IllegalAccessException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        });

        this.settings.add(this.visible);
        this.settings.add(this.bind);
    }

    public Module(String name, Category category, String description, Bind bind) {
        this(name, category, description);

        this.bind.setValue(bind);
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onTick() {
    }

    public void onRender2D() {
    }

    public void onRender3D() {
    }

    /**
     * Toggles the module
     */
    public void toggle() {
        // We don't want to toggle if the module is constant
        if (constant) {
            return;
        }

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

            animation.setState(true);

            // Call onEnable
            onEnable();
        } else {
            // Unregister events
            MinecraftForge.EVENT_BUS.unregister(this);
            Paragon.INSTANCE.getEventBus().unregister(this);

            animation.setState(false);

            // Call onDisable
            onDisable();
        }
    }

    /**
     * Gets the module info for the array list
     *
     * @return The module's info
     */
    public String getArrayListInfo() {
        return "";
    }

    /**
     * Gets the module's category
     *
     * @return The module's category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Gets whether the module is enabled
     *
     * @return Whether the module is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the module's visibility
     *
     * @return The module's visibility
     */
    public boolean isVisible() {
        return visible.getValue();
    }

    /**
     * Sets the module's visibility
     *
     * @param visible The module's new visibility
     */
    public void setVisible(boolean visible) {
        this.visible.setValue(visible);
    }

    /**
     * Checks if the module is constantly enabled
     *
     * @return Whether the module is constantly enabled
     */
    public boolean isConstant() {
        return constant;
    }

    /**
     * Gets whether the module is ignored by notifications or not
     *
     * @return Whether the module is ignored by notifications or not
     */
    public boolean isIgnored() {
        return ignoredByNotifications;
    }

    /**
     * Gets a list of the module's settings
     *
     * @return The module's settings
     */
    public List<Setting<?>> getSettings() {
        return settings;
    }

    /**
     * Gets the bind of the module
     *
     * @return The bind of the module
     */
    public Setting<Bind> getBind() {
        return bind;
    }
}
