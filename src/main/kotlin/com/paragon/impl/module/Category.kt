package com.paragon.impl.module

import net.minecraft.init.Items
import net.minecraft.item.Item

/**
 * @author Surge, SooStrator1136
 */
enum class Category(val Name: String, val indicator: Item) { //Uppercase "Name" because kotlin is special?

    COMBAT("Combat", Items.END_CRYSTAL),

    /**
     * Movement related modules, e.g. Step, ElytraFlight
     */
    MOVEMENT("Movement", Items.DIAMOND_BOOTS),

    /**
     * Render related modules, e.g. Tracers, ESP
     */
    RENDER("Render", Items.ENDER_EYE),

    /**
     * Modules that don't belong to a particular category, e.g. ChatModifications, FakePlayer
     */
    MISC("Misc", Items.LAVA_BUCKET),

    /**
     * Client related modules, e.g. GUI, Colours
     */
    CLIENT("Client", Items.WRITABLE_BOOK),

    /**
     * HUD modules
     */
    HUD("HUD", Items.COMPASS)

}