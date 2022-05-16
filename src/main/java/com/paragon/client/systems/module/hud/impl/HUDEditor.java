package com.paragon.client.systems.module.hud.impl;

import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.IgnoredByNotifications;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.hud.HUDEditorGUI;
import org.lwjgl.input.Keyboard;

@IgnoredByNotifications
public class HUDEditor extends Module {

    public HUDEditor() {
        super("HUDEditor", Category.HUD, "Lets you edit the HUD module positions", Keyboard.KEY_P);
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(new HUDEditorGUI());
        toggle();
    }
}
