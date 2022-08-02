package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.world.*;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.Arrays;
import java.util.List;

/**
 * @author Surge
 */
public class Xray extends Module {

    private final List<Block> visibleBlocks = Arrays.asList(
            Blocks.DIAMOND_ORE,
            Blocks.IRON_ORE,
            Blocks.GOLD_ORE,
            Blocks.COAL_ORE,
            Blocks.LAPIS_ORE,
            Blocks.QUARTZ_ORE,
            Blocks.EMERALD_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.LIT_REDSTONE_ORE,
            Blocks.WATER,
            Blocks.FLOWING_WATER,
            Blocks.LAVA,
            Blocks.FLOWING_LAVA,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST
    );

    public Xray() {
        super("Xray", Category.RENDER, "Lets you see ores and liquids through blocks");
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            return;
        }

        mc.renderGlobal.loadRenderers();
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }

        mc.renderGlobal.loadRenderers();
    }

    @Listener
    public void onBlockSetOpaque(BlockSetOpaqueEvent event) {
        event.cancel();
    }

    @Listener
    public void onFullCubeBlock(FullCubeBlockEvent event) {
        event.setReturnValue(visibleBlocks.contains(event.getBlock()));
        event.cancel();
    }

    @Listener
    public void onRenderBlockModel(RenderBlockModelEvent event) {
        event.setReturnValue(!visibleBlocks.contains(BlockUtil.getBlockAtPos(event.getPos())));
        event.cancel();
    }

    @Listener
    public void onRenderSmooth(RenderBlockSmoothEvent event) {
        event.setReturnValue(!visibleBlocks.contains(BlockUtil.getBlockAtPos(event.getPos())));
        event.cancel();
    }

    @Listener
    public void onSideRenderBlock(SideRenderBlockEvent event) {
        event.setReturnValue(visibleBlocks.contains(BlockUtil.getBlockAtPos(event.getPos())));
        event.cancel();
    }

}
