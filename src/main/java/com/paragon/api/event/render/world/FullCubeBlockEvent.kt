package com.paragon.api.event.render.world

import me.wolfsurge.cerauno.event.CancellableEvent
import net.minecraft.block.Block

/**
 * @author Surge
 */
class FullCubeBlockEvent(val block: Block) : CancellableEvent() {

    var returnValue = false

}