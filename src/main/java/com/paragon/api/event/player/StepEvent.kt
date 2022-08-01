package com.paragon.api.event.player

import me.wolfsurge.cerauno.event.CancellableEvent
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB

/**
 * @author Surge
 */
class StepEvent(val bB: AxisAlignedBB, val entity: Entity, var height: Float) : CancellableEvent()