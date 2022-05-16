package com.paragon.client.systems.module.hud.impl;

import com.paragon.Paragon;
import com.paragon.api.util.render.TextRenderer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.hud.HUDEditorGUI;

public class HUD extends Module implements TextRenderer {

    public HUD() {
        super("HUD", Category.HUD, "Render the client's HUD on screen");
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
