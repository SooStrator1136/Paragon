package com.paragon.client.systems.module.hud;

import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;

public abstract class HUDModule extends Module implements TextRenderer {

    private float x = 50, y = 50;

    public HUDModule(String name, String description) {
        super(name, ModuleCategory.HUD, description);
    }

    public abstract void render();

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

}
