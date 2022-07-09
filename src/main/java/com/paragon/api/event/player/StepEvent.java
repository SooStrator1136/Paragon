package com.paragon.api.event.player;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

public class StepEvent extends CancellableEvent {

    private final AxisAlignedBB bb;
    private final Entity entity;
    private float height;

    public StepEvent(AxisAlignedBB bb, Entity entity, float height) {
        this.bb = bb;
        this.entity = entity;
        this.height = height;
    }

    /**
     * Gets the bounding box
     * @return The bounding box
     */
    public AxisAlignedBB getBB() {
        return bb;
    }

    /**
     * Gets the entity
     * @return The entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the height
     * @return The height
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets the height
     * @param height The height
     */
    public void setHeight(float height) {
        this.height = height;
    }

}
