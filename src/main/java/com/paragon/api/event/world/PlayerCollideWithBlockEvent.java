package com.paragon.api.event.world;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class PlayerCollideWithBlockEvent extends CancellableEvent {

    private BlockPos pos;
    private Block blockType;

    public PlayerCollideWithBlockEvent(BlockPos pos, Block blockType) {
        this.pos = pos;
        this.blockType = blockType;
    }

    /**
     * Gets the block position
     *
     * @return The block position
     */
    public BlockPos getPos() {
        return pos;
    }

    /**
     * Gets the block type
     *
     * @return The block type
     */
    public Block getBlockType() {
        return blockType;
    }
}
