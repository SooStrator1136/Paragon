package com.paragon.api.event.world

import me.wolfsurge.cerauno.event.CancellableEvent
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos

/**
 * @author SooStrator1136
 */
class PlayerCollideWithBlockEvent(val pos: BlockPos, val blockType: Block) : CancellableEvent()