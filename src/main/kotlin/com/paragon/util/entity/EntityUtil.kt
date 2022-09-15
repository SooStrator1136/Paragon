package com.paragon.util.entity

import com.paragon.util.Wrapper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityAgeable
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EnumCreatureType
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.monster.EntitySpider
import net.minecraft.entity.passive.EntityAmbientCreature
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityWolf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.TextFormatting


object EntityUtil : Wrapper {

    /**
     * Gets the interpolated position of a given entity
     *
     * @param entityIn The given entity
     * @return The interpolated position
     */
    fun getInterpolatedPosition(entityIn: Entity): Vec3d {
        return Vec3d(entityIn.lastTickPosX, entityIn.lastTickPosY, entityIn.lastTickPosZ).add(
            getInterpolatedAmount(
                entityIn, minecraft.renderPartialTicks
            )
        )
    }

    /**
     * Gets the interpolated amount of the entity
     *
     * @param entity       The entity in
     * @param partialTicks The render partial ticks
     * @return The interpolated amount
     */
    private fun getInterpolatedAmount(entity: Entity, partialTicks: Float): Vec3d {
        return Vec3d(
            (entity.posX - entity.lastTickPosX) * partialTicks, (entity.posY - entity.lastTickPosY) * partialTicks, (entity.posZ - entity.lastTickPosZ) * partialTicks
        )
    }

    /**
     * Gets the text formatting colour based on an entity's health
     *
     * @param entity The entity
     * @return The colour of the health
     */
    fun getTextColourFromEntityHealth(entity: EntityLivingBase): TextFormatting {
        val health = getEntityHealth(entity)
        if (health > 20) {
            return TextFormatting.YELLOW
        }
        else if (entity.health <= 20 && entity.health > 15) {
            return TextFormatting.GREEN
        }
        else if (entity.health <= 15 && entity.health > 10) {
            return TextFormatting.GOLD
        }
        else if (entity.health <= 10 && entity.health > 5) {
            return TextFormatting.RED
        }
        else if (entity.health <= 5) {
            return TextFormatting.DARK_RED
        }
        return TextFormatting.GRAY
    }

    /**
     * Gets the bounding box of an entity
     *
     * @param entity The entity
     * @return The bounding box of the entity
     */
    fun getEntityBox(entity: Entity): AxisAlignedBB {
        return AxisAlignedBB(
            entity.entityBoundingBox.minX - entity.posX + (entity.posX - minecraft.renderManager.viewerPosX), entity.entityBoundingBox.minY - entity.posY + (entity.posY - minecraft.renderManager.viewerPosY), entity.entityBoundingBox.minZ - entity.posZ + (entity.posZ - minecraft.renderManager.viewerPosZ), entity.entityBoundingBox.maxX - entity.posX + (entity.posX - minecraft.renderManager.viewerPosX), entity.entityBoundingBox.maxY - entity.posY + (entity.posY - minecraft.renderManager.viewerPosY), entity.entityBoundingBox.maxZ - entity.posZ + (entity.posZ - minecraft.renderManager.viewerPosZ)
        )
    }

    /**
     * Checks if a player's distance from us is further than the given maximum range
     *
     * @param maximumRange The maximum range they are allowed in
     * @return If the player is too far away from us
     */
    @JvmStatic
    fun Entity.isTooFarAwayFromSelf(maximumRange: Double): Boolean {
        return this.getDistance(minecraft.player) > maximumRange
    }

    @JvmStatic
    fun Entity.isEntityAllowed(players: Boolean, mobs: Boolean, passives: Boolean): Boolean {
        if (this is EntityPlayer && players && this !== minecraft.player) {
            return true
        }

        return if (this.isMonster() && mobs) {
            true
        }
        else this.isPassive() && passives
    }

    @JvmStatic
    fun Entity.isMonster(): Boolean {
        return this.isCreatureType(EnumCreatureType.MONSTER, false) && !(this is EntityPigZombie || this is EntityWolf || this is EntityEnderman) || this is EntitySpider
    }

    @JvmStatic
    fun Entity.isPassive(): Boolean {
        if (this is EntityWolf) {
            return !this.isAngry
        }

        return if (this is EntityIronGolem) {
            (this as EntityLivingBase).revengeTarget == null
        }
        else {
            this is EntityAgeable || this is EntityAmbientCreature || this is EntitySquid
        }
    }

    @JvmStatic
    fun getEntityHealth(entityLivingBase: EntityLivingBase): Float {
        return entityLivingBase.health + entityLivingBase.absorptionAmount
    }

}