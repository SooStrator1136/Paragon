package com.paragon.api.event.client;

import com.paragon.api.module.Module;
import me.wolfsurge.cerauno.event.CancellableEvent;

/**
 * @author Wolfsurge
 */
public class ModuleToggleEvent extends CancellableEvent {

    private final Module module;

    public ModuleToggleEvent(Module module) {
        this.module = module;
    }

    /**
     * Gets the module
     *
     * @return The module
     */
    public Module getModule() {
        return module;
    }

}
