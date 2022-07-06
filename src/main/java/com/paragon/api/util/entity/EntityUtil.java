package com.paragon.api.util.entity;

import com.paragon.api.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;

public class EntityUtil implements Wrapper {

    /**
     * Gets the interpolated position of a given entity
     *
     * @param entityIn The given entity
     * @return The interpolated position
     */
    public static Vec3d getInterpolatedPosition(Entity entityIn) {
        return new Vec3d(entityIn.lastTickPosX, entityIn.lastTickPosY, entityIn.lastTickPosZ).add(getInterpolatedAmount(entityIn, mc.getRenderPartialTicks()));
    }

    /**
     * Gets the interpolated amount of the entity
     *
     * @param entity       The entity in
     * @param partialTicks The render partial ticks
     * @return The interpolated amount
     */
    public static Vec3d getInterpolatedAmount(Entity entity, float partialTicks) {
        return new Vec3d((entity.posX - entity.lastTickPosX) * partialTicks, (entity.posY - entity.lastTickPosY) * partialTicks, (entity.posZ - entity.lastTickPosZ) * partialTicks);
    }

    /**
     * Gets the text formatting colour based on an entity's health
     *
     * @param entity The entity
     * @return The colour of the health
     */
    public static TextFormatting getTextColourFromEntityHealth(EntityLivingBase entity) {
        float health = getEntityHealth(entity);

        if (health > 20) {
            return TextFormatting.YELLOW;
        } else if (entity.getHealth() <= 20 && entity.getHealth() > 15) {
            return TextFormatting.GREEN;
        } else if (entity.getHealth() <= 15 && entity.getHealth() > 10) {
            return TextFormatting.GOLD;
        } else if (entity.getHealth() <= 10 && entity.getHealth() > 5) {
            return TextFormatting.RED;
        } else if (entity.getHealth() <= 5) {
            return TextFormatting.DARK_RED;
        }

        return TextFormatting.GRAY;
    }

    /**
     * Gets the bounding box of an entity
     *
     * @param entity The entity
     * @return The bounding box of the entity
     */
    public static AxisAlignedBB getEntityBox(Entity entity) {
        return new AxisAlignedBB(
                entity.getEntityBoundingBox().minX - entity.posX + (entity.posX - mc.getRenderManager().viewerPosX),
                entity.getEntityBoundingBox().minY - entity.posY + (entity.posY - mc.getRenderManager().viewerPosY),
                entity.getEntityBoundingBox().minZ - entity.posZ + (entity.posZ - mc.getRenderManager().viewerPosZ),
                entity.getEntityBoundingBox().maxX - entity.posX + (entity.posX - mc.getRenderManager().viewerPosX),
                entity.getEntityBoundingBox().maxY - entity.posY + (entity.posY - mc.getRenderManager().viewerPosY),
                entity.getEntityBoundingBox().maxZ - entity.posZ + (entity.posZ - mc.getRenderManager().viewerPosZ)
        );
    }

    /**
     * Checks if a player's distance from us is further than the given maximum range
     *
     * @param entity       The player to check
     * @param maximumRange The maximum range they are allowed in
     * @return If the player is too far away from us
     */
    public static boolean isTooFarAwayFromSelf(Entity entity, float maximumRange) {
        return !(entity.getDistance(mc.player) <= maximumRange);
    }

    /**
     * Checks if an entity can see a block pos
     *
     * @param entity   The entity
     * @param position The position to check
     * @return Whether the entity can see the block pos
     */
    public static boolean canEntitySeePosition(Entity entity, BlockPos position) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5), false, true, false) != null;
    }

    public static boolean isEntityAllowed(Entity entity, boolean players, boolean mobs, boolean passives) {
        if (entity instanceof EntityPlayer && players && entity != mc.player) {
            return true;
        }

        if (isMonster(entity) && mobs) {
            return true;
        }

        if (isPassive(entity) && passives) {
            return true;
        }

        return false;
    }

    public static boolean isMonster(Entity entity) {
        return entity.isCreatureType(EnumCreatureType.MONSTER, false) && !(entity instanceof EntityPigZombie || entity instanceof EntityWolf || entity instanceof EntityEnderman) || entity instanceof EntitySpider;
    }

    public static boolean isPassive(Entity entity) {
        if (entity instanceof EntityWolf) {
            return !((EntityWolf) entity).isAngry();
        }

        if (entity instanceof EntityIronGolem) {
            return ((EntityIronGolem) entity).getRevengeTarget() == null;
        }

        return entity instanceof EntityAgeable || entity instanceof EntityAmbientCreature || entity instanceof EntitySquid;
    }

    public static float getEntityHealth(EntityLivingBase entityLivingBase) {
        return entityLivingBase.getHealth() + entityLivingBase.getAbsorptionAmount();
    }
}
