package com.paragon.api.event.world.entity;

import me.wolfsurge.cerauno.event.Event;
import net.minecraft.entity.Entity;

/**
 * @author Wolfsurge
 */
public class EntityRemoveFromWorldEvent extends Event {

    // The entity that is removed
    private Entity entity;

    public EntityRemoveFromWorldEvent(Entity entity) {
        this.entity = entity;
    }

    /**
     * Gets the entity
     *
     * @return The entity
     */
    public Entity getEntity() {
        return entity;
    }

}
