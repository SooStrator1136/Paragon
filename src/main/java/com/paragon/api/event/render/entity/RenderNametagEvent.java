package com.paragon.api.event.render.entity;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.entity.Entity;

/**
 * @author Wolfsurge
 */
public class RenderNametagEvent extends CancellableEvent {

    private final Entity entity;

    public RenderNametagEvent(Entity entity) {
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
