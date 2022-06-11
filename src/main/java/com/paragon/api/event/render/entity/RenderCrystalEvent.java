package com.paragon.api.event.render.entity;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;

/**
 * @author Wolfsurge
 * @since 2/2/22
 */
public class RenderCrystalEvent extends CancellableEvent {

    // The entity being rendered
    private final EntityEnderCrystal entity;

    // The model of the entity
    private final ModelRenderer base;

    private final ModelRenderer glass;

    private final ModelRenderer cube;

    // Required variables
    private final float limbSwing;
    private final float limbSwingAmount;
    private final float ageInTicks;
    private final float netHeadYaw;
    private final float headPitch;
    private float scale;

    public RenderCrystalEvent(ModelRenderer modelRenderer, ModelRenderer glass, ModelRenderer cube, EntityEnderCrystal entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.base = modelRenderer;
        this.glass = glass;
        this.cube = cube;
        this.entity = entityIn;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        this.scale = scale;
    }

    /**
     * Gets the model renderer
     *
     * @return The model renderer
     */
    public ModelRenderer getBase() {
        return base;
    }

    /**
     * Gets the glass renderer
     *
     * @return The glass renderer
     */
    public ModelRenderer getGlass() {
        return glass;
    }

    /**
     * Gets the cube renderer
     *
     * @return The cube renderer
     */
    public ModelRenderer getCube() {
        return cube;
    }

    /**
     * Gets the entity
     *
     * @return The entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the limb swing
     *
     * @return The limb swing
     */
    public float getLimbSwing() {
        return limbSwing;
    }

    /**
     * Gets the limb swing amount
     *
     * @return The limb swing amount
     */
    public float getLimbSwingAmount() {
        return limbSwingAmount;
    }

    /**
     * Gets the age in ticks
     *
     * @return The age in ticks
     */
    public float getAgeInTicks() {
        return ageInTicks;
    }

    /**
     * Gets the net head yaw
     *
     * @return The net head yaw
     */
    public float getNetHeadYaw() {
        return netHeadYaw;
    }

    /**
     * Gets the head pitch
     *
     * @return The head pitch
     */
    public float getHeadPitch() {
        return headPitch;
    }

    /**
     * Gets the scale
     *
     * @return The scale
     */
    public float getScale() {
        return scale;
    }

    /**
     * Sets the scale
     *
     * @param newScale The new scale
     */
    public void setScale(float newScale) {
        this.scale = newScale;
    }
}
