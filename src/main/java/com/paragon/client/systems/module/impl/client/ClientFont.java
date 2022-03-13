package com.paragon.client.systems.module.impl.client;

import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;

public class ClientFont extends Module {

    public static ClientFont INSTANCE;

    public ClientFont() {
        super("Font", ModuleCategory.CLIENT, "Use the client's custom font");
        INSTANCE = this;
    }

}
