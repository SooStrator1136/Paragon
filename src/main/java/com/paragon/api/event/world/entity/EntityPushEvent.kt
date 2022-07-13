package com.paragon.api.event.world.entity

import me.wolfsurge.cerauno.event.CancellableEvent
import net.minecraft.entity.Entity

/**
 * @author Wolfsurge, SooStrator1136
 */
class EntityPushEvent(val entity: Entity) : CancellableEvent()