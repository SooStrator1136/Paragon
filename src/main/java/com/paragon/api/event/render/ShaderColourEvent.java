package com.paragon.api.event.render;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.entity.Entity;

import java.awt.*;

/**
 * @author Surge
 */
public class ShaderColourEvent extends CancellableEvent {

    private Color colour;
    private Entity entity;

    public ShaderColourEvent(Entity entity) {
        this.entity = entity;
    }

    /**
     * Gets the colour of the shader
     *
     * @return The colour of the shader
     */
    public Color getColour() {
        return colour;
    }

    /**
     * Sets the colour of the shader
     *
     * @param colour The colour of the shader
     */
    public void setColour(Color colour) {
        this.colour = colour;
    }

    /**
     * Gets the entity that is being rendered
     *
     * @return The entity that is being rendered
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Sets the entity that is being rendered
     *
     * @param entity The entity that is being rendered
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

}
