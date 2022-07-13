package com.paragon.api.util.player;

import com.paragon.api.util.Wrapper;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.world.World;

public class EntityFakePlayer extends EntityOtherPlayerMP implements Wrapper {

    public EntityFakePlayer(final World world) {
        super(world, mc.player.getGameProfile());
        this.copyLocationAndAnglesFrom(mc.player);
        this.inventory.copyInventory(mc.player.inventory);
        mc.world.addEntityToWorld(-Integer.MAX_VALUE, this);
    }

    public void despawn() {
        mc.world.removeEntity(mc.world.getEntityByID(-Integer.MAX_VALUE));
    }

}
