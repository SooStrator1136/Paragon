package com.paragon.client.systems.module.hud.impl;

import com.paragon.api.module.Category;
import com.paragon.api.module.IgnoredByNotifications;
import com.paragon.api.module.Module;
import com.paragon.client.systems.module.hud.HUDEditorGUI;
import com.paragon.api.setting.Bind;
import org.lwjgl.input.Keyboard;

@IgnoredByNotifications
public class HUDEditor extends Module {

    public static HUDEditor INSTANCE;

    public HUDEditor() {
        super("HUDEditor", Category.HUD, "Lets you edit the HUD module positions", new Bind(Keyboard.KEY_P, Bind.Device.KEYBOARD));

        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(new HUDEditorGUI());
        toggle();
    }
}
