package com.paragon.api.event.render.world;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.util.math.BlockPos;

/**
 * @author Surge
 */
public class BlockSetOpaqueEvent extends CancellableEvent {

    private final BlockPos pos;

    public BlockSetOpaqueEvent(BlockPos pos) {
        this.pos = pos;
    }

    /**
     * Gets the pos
     *
     * @return The pos
     */
    public BlockPos getPos() {
        return pos;
    }

}
