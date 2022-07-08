package com.paragon.client.systems.module.impl.client;

import com.paragon.api.module.IgnoredByNotifications;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;

@IgnoredByNotifications
public class ClientFont extends Module {

    public static ClientFont INSTANCE;

    public ClientFont() {
        super("Font", Category.CLIENT, "Use the client's custom font");

        INSTANCE = this;
    }

}
