package com.paragon.client.systems.module;

import net.minecraft.init.Items;
import net.minecraft.item.Item;

public enum Category {

    /**
     * PVP related modules, e.g. Aura, AutoCrystal
     */
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
    HUD("HUD", Items.COMPASS);

    private final String name;
    private final Item indicator;

    Category(String name, Item indicator) {
        this.name = name;
        this.indicator = indicator;
    }

    public String getName() {
        return this.name;
    }

    public Item getIndicator() {
        return this.indicator;
    }
}
