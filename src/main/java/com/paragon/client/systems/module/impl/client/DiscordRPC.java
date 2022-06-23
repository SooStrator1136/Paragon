package com.paragon.client.systems.module.impl.client;

import com.paragon.Paragon;
import com.paragon.client.systems.module.IgnoredByNotifications;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;

@IgnoredByNotifications
public class DiscordRPC extends Module {

    public DiscordRPC() {
        super("DiscordRPC", Category.CLIENT, "Changes your Discord presence to reflect the client's current state");
    }

    @Override
    public void onEnable() {
        Paragon.INSTANCE.getPresenceManager().startRPC();
    }

    @Override
    public void onTick() {
        Paragon.INSTANCE.getPresenceManager().updateRPC();
    }

    @Override
    public void onDisable() {
        Paragon.INSTANCE.getPresenceManager().stopRPC();
    }

}
