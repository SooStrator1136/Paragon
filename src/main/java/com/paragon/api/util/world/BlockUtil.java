package com.paragon.api.util.world;

import com.paragon.api.util.Wrapper;
import com.paragon.api.util.calculations.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class BlockUtil implements Wrapper {

    /**
     * Gets blocks around the entity
     * @param entity The entity
     * @param blockRange The range to get blocks
     * @return A list of blocks
     */
    public static List<BlockPos> getBlocksAroundPlayer(Entity entity, double blockRange) {
        List<BlockPos> nearbyBlocks = new ArrayList<>();
        int range = (int) MathUtil.roundDouble(blockRange, 0);

        for (int x = -range; x <= range; x++)
            for (int y = -range; y <= range - (range / 2); y++)
                for (int z = -range; z <= range; z++)
                    nearbyBlocks.add(entity.getPosition().add(x, y, z));

        return nearbyBlocks;
    }

    public static List<BlockPos> getSphere(float radius, boolean ignoreAir) {
        ArrayList<BlockPos> sphere = new ArrayList<>();
        BlockPos pos = new BlockPos(BlockUtil.mc.player.getPositionVector());
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();

        for (int x = posX - (int) radius; (float) x <= (float) posX + radius; ++x) {
            for (int z = posZ - (int) radius; (float) z <= (float) posZ + radius; ++z) {
                for (int y = posY - (int) radius; (float) y < (float) posY + radius; ++y) {
                    double dist = (posX - x) * (posX - x) + (posZ - z) * (posZ - z) + (posY - y) * (posY - y);

                    if (dist < (double) (radius * radius)) {
                        BlockPos position = new BlockPos(x, y, z);

                        if (BlockUtil.mc.world.getBlockState(position).getBlock() != Blocks.AIR || !ignoreAir) {
                            sphere.add(position);
                        }
                    }
                }
            }
        }

        return sphere;
    }

    /**
     * Gets the block at a position
     * @param pos The position
     * @return The block
     */
    public static Block getBlockAtPos(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    /**
     * Gets the bounding box of a block
     * @param blockPos The block
     * @return The bounding box of the entity
     */
    public static AxisAlignedBB getBlockBox(BlockPos blockPos) {
        double x = blockPos.getX() - mc.getRenderManager().viewerPosX;
        double y = blockPos.getY() - mc.getRenderManager().viewerPosY;
        double z = blockPos.getZ() - mc.getRenderManager().viewerPosZ;

        return new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
    }

    /**
     * Returns whether an entity is on top / in a block, and therefore cannot be placed on.
     * @author linustouchtips, Wolfsurge
     * @param pos The block to check
     * @return Whether the block is placeable
     */
    public static boolean isIntercepted(BlockPos pos) {
        BlockPos nativePosition = pos.up();

        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(nativePosition.getX(), pos.getY(), nativePosition.getZ(), nativePosition.getX() + 1, nativePosition.getY() + 2, nativePosition.getZ() + 1))) {
            if (entity instanceof EntityEnderCrystal && entity.getPosition().equals(nativePosition)) {
                continue;
            }

            if (entity.isDead) {
                continue;
            }

            return true;
        }

        return false;
    }

}
