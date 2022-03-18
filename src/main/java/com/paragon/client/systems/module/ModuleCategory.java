package com.paragon.client.systems.module;

public enum ModuleCategory {

    /**
     * PVP related modules, e.g. Aura, AutoCrystal
     */
    COMBAT("Combat"),

    /**
     * Movement related modules, e.g. Step, ElytraFlight
     */
    MOVEMENT("Movement"),

    /**
     * Render related modules, e.g. Tracers, ESP
     */
    RENDER("Render"),

    /**
     * Modules that don't belong to a particular category, e.g. ChatModifications, FakePlayer
     */
    MISC("Misc"),

    /**
     * Client related modules, e.g. GUI, Colours
     */
    CLIENT("Client"),

    /**
     * HUD modules
     */
    HUD("HUD");

    private String name;

    ModuleCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
