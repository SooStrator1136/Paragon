package com.paragon.api.event.render.world

import com.paragon.bus.event.CancellableEvent
import net.minecraft.util.math.BlockPos

/**
 * @author Surge
 */
class SideRenderBlockEvent(val pos: BlockPos) : CancellableEvent() {

    var returnValue = false

}