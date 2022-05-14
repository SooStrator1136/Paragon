package com.paragon.api.event.render.world;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.block.Block;

/**
 * @author Wolfsurge
 */
public class FullCubeBlockEvent extends CancellableEvent {

    private final Block block;
    private boolean returnValue = false;

    public FullCubeBlockEvent(Block block) {
        this.block = block;
    }

    /**
     * Gets the block
     *
     * @return The block
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Gets the return value
     *
     * @return The return value
     */
    public boolean getReturnValue() {
        return returnValue;
    }

    /**
     * Sets the return value
     *
     * @param returnValue The return value
     */
    public void setReturnValue(boolean returnValue) {
        this.returnValue = returnValue;
    }

}
