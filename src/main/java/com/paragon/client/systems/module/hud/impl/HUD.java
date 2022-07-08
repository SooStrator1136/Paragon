package com.paragon.client.systems.module.hud.impl;

import com.paragon.Paragon;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.client.systems.module.hud.HUDEditorGUI;

public class HUD extends Module implements TextRenderer {

    public static HUD INSTANCE;

    public HUD() {
        super("HUD", Category.HUD, "Render the client's HUD on screen");

        INSTANCE = this;
    }

    @Override
    public void onRender2D() {
        if (mc.currentScreen instanceof HUDEditorGUI) {
            return;
        }

        Paragon.INSTANCE.getModuleManager().getHUDModules().forEach(hudModule -> {
            if (hudModule.isEnabled()) {
                hudModule.render();
            }
        });
    }
}
