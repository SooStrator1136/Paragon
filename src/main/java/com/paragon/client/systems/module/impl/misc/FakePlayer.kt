package com.paragon.client.systems.module.impl.misc

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.util.Wrapper
import com.paragon.api.util.player.EntityFakePlayer

/**
 * @author Surge
 */
object FakePlayer : Module("FakePlayer", Category.MISC, "Spawns a fake client side player") {
    
    private var fakePlayer: EntityFakePlayer? = null
    
    override fun onEnable() {
        if (nullCheck()) {
            return
        }

        // Create new fake player
        fakePlayer = EntityFakePlayer(minecraft.world)
    }

    override fun onDisable() {
        // If we can despawn the player, do so
        if (fakePlayer != null) {
            if (minecraft.world != null && minecraft.world.loadedEntityList.contains(fakePlayer)) fakePlayer!!.despawn()
        }
    }
}