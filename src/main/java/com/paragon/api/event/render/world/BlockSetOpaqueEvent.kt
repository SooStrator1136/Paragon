package com.paragon.api.event.render.world

import me.wolfsurge.cerauno.event.CancellableEvent
import net.minecraft.util.math.BlockPos

/**
 * @author Surge
 */
class BlockSetOpaqueEvent(val pos: BlockPos) : CancellableEvent()