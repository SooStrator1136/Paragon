package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.util.player.EntityFakePlayer;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;

/**
 * @author Surge
 */
public class FakePlayer extends Module {

    private EntityFakePlayer fakePlayer;

    public FakePlayer() {
        super("FakePlayer", Category.MISC, "Spawns a fake client side player");
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            return;
        }

        // Create new fake player
        fakePlayer = new EntityFakePlayer(getMinecraft().world);
    }

    @Override
    public void onDisable() {
        // If we can despawn the player, do so
        if (fakePlayer != null) {
            if (mc.world != null && mc.world.loadedEntityList.contains(fakePlayer))
                fakePlayer.despawn();
        }
    }
}
