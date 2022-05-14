package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.util.world.BlockUtil;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Wolfsurge
 */
public class NoGlitchBlocks extends Module {

    public NoGlitchBlocks() {
        super("NoGlitchBlocks", Category.MISC, "Removes glitched blocks in the world");
    }

    @SubscribeEvent
    public void onBreakBlock(BlockEvent.BreakEvent event) {
        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) {
            return;
        }

        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos player = mc.player.getPosition();
                    BlockPos pos = new BlockPos(player.getX() + x, player.getY() + y, player.getZ() + z);

                    if (!BlockUtil.getBlockAtPos(pos).equals(Blocks.AIR)) {
                        mc.playerController.processRightClickBlock(mc.player, mc.world, pos, EnumFacing.DOWN, new Vec3d(0.5, 0.5, 0.5), EnumHand.MAIN_HAND);
                    }
                }
            }
        }
    }

}
