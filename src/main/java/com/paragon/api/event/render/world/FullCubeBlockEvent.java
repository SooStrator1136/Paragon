package com.paragon.api.event.render.world;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.block.Block;

/**
 * @author Wolfsurge
 */
public class FullCubeBlockEvent extends CancellableEvent {

    private final Block block;

    public FullCubeBlockEvent(Block block) {
        this.block = block;
    }

    /**
     * Gets the block
     * @return The block
     */
    public Block getBlock() {
        return block;
    }

}
