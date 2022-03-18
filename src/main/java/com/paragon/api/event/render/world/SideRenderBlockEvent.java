package com.paragon.api.event.render.world;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.util.math.BlockPos;

/**
 * @author Wolfsurge
 */
public class SideRenderBlockEvent extends CancellableEvent {

    private final BlockPos pos;

    public SideRenderBlockEvent(BlockPos pos) {
        this.pos = pos;
    }

    /**
     * Gets the pos
     * @return The pos
     */
    public BlockPos getPos() {
        return pos;
    }

}
