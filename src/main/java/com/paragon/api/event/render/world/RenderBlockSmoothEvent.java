package com.paragon.api.event.render.world;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.util.math.BlockPos;

/**
 * @author Wolfsurge
 */
public class RenderBlockSmoothEvent extends CancellableEvent {

    private final BlockPos pos;
    private boolean returnValue;

    public RenderBlockSmoothEvent(BlockPos pos) {
        this.pos = pos;
    }

    /**
     * Gets the pos
     * @return The pos
     */
    public BlockPos getPos() {
        return pos;
    }

    /**
     * Gets the return value
     * @return The return value
     */
    public boolean getReturnValue() {
        return returnValue;
    }

    /**
     * Sets the return value
     * @param value The return value
     */
    public void setReturnValue(boolean value) {
        returnValue = value;
    }

}
