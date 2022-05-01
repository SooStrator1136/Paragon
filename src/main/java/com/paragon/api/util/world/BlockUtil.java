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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class BlockUtil implements Wrapper {

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
        return new AxisAlignedBB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1).offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
    }

    public static boolean canSeePos(BlockPos pos) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), false, true, false) == null;
    }

}
