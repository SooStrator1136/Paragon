package com.paragon.api.event.world.entity;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.entity.Entity;

public class EntityPushEvent extends CancellableEvent {

    private final Entity entity;

    public EntityPushEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }

}
