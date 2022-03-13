package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.util.player.EntityFakePlayer;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;

public class FakePlayer extends Module {

    public FakePlayer() {
        super("FakePlayer", ModuleCategory.MISC, "Spawns a fake client side player");
    }

    private EntityFakePlayer fakePlayer;

    @Override
    public void onEnable() {
        if (nullCheck()) {
            return;
        }

        fakePlayer = new EntityFakePlayer();
    }

    @Override
    public void onDisable() {
        if (fakePlayer != null) {
            if (mc.world != null && mc.world.loadedEntityList.contains(fakePlayer))
            fakePlayer.despawn();
        }
    }
}
