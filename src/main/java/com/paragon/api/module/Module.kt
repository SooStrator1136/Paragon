package com.paragon.api.module

import com.paragon.Paragon
import com.paragon.api.event.client.ModuleToggleEvent
import com.paragon.api.feature.Feature
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.Wrapper
import com.paragon.client.systems.module.hud.impl.ArrayListHUD
import me.surge.animation.Animation
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.input.Keyboard
import java.util.*

open class Module(name: String, val category: Category, description: String) : Feature(name, description), Wrapper {

    // Whether the module is visible in the Array List or not
    private val visible = Setting("Visible", true).setDescription("Whether the module is visible in the array list or not")

    val bind = Setting("Bind", Bind(Keyboard.KEY_NONE, Bind.Device.KEYBOARD)).setDescription("The keybind of the module")

    // Whether the module is constantly enabled or not
    private val isConstant = javaClass.isAnnotationPresent(Constant::class.java)

    // Whether the module is ignored by notifications
    val isIgnored = javaClass.isAnnotationPresent(IgnoredByNotifications::class.java)

    // Module Settings
    val settings: MutableList<Setting<*>> = ArrayList()

    // Arraylist animation
    val animation = Animation({ ArrayListHUD.animationSpeed.value }, false) { ArrayListHUD.easing.value }

    // Whether the module is enabled
    var isEnabled = false
        private set

    init {
        if (isConstant) {
            isEnabled = true

            // Register events
            MinecraftForge.EVENT_BUS.register(this)
            Paragon.INSTANCE.eventBus.register(this)
        }
    }

    constructor(name: String, category: Category, description: String, bind: Bind) : this(name, category, description) {
        this.bind.setValue(bind)
    }

    // TEMPORARY
    fun reflectSettings() {
        Arrays.stream(javaClass.declaredFields)
            .filter { Setting::class.java.isAssignableFrom(it.type) }
            .forEach {
                it.isAccessible = true
                try {
                    val setting = it[this] as Setting<*>
                    if (setting.parentSetting == null) {
                        settings.add(setting)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }

        settings.add(visible)
        settings.add(bind)
    }

    open fun onEnable() {}
    open fun onDisable() {}
    open fun onTick() {}
    open fun onRender2D() {}
    open fun onRender3D() {}

    open fun getData() = ""

    open fun isActive() = isEnabled

    /**
     * Toggles the module
     */
    fun toggle() {
        // We don't want to toggle if the module is constant
        if (isConstant) {
            return
        }

        isEnabled = !isEnabled

        Paragon.INSTANCE.eventBus.post(ModuleToggleEvent(this))

        if (isEnabled) {
            // Register events
            MinecraftForge.EVENT_BUS.register(this)
            Paragon.INSTANCE.eventBus.register(this)
            animation.state = true

            // Call onEnable
            onEnable()
        } else {
            // Unregister events
            MinecraftForge.EVENT_BUS.unregister(this)
            Paragon.INSTANCE.eventBus.unregister(this)
            animation.state = false

            // Call onDisable
            onDisable()
        }
    }

    /**
     * Gets the module's visibility
     *
     * @return The module's visibility
     */
    fun isVisible() = visible.value

    /**
     * Sets the module's visibility
     *
     * @param visible The module's new visibility
     */
    fun setVisible(visible: Boolean) {
        this.visible.setValue(visible)
    }

}