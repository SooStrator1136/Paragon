package com.paragon.api.util.player;

import com.paragon.api.util.Wrapper;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.world.World;

import java.util.UUID;

public final class EntityFakePlayer extends EntityOtherPlayerMP implements Wrapper {

    public EntityFakePlayer(final World worldIn) {
        super(worldIn, mc.player.getGameProfile());
        this.copyLocationAndAnglesFrom(mc.player);
        this.inventory.copyInventory(mc.player.inventory);
        mc.world.addEntityToWorld(-Integer.MAX_VALUE, this);
        this.entityUniqueID = UUID.randomUUID();
    }

    public void despawn() {
        mc.world.removeEntity(mc.world.getEntityByID(-Integer.MAX_VALUE));
    }

}
