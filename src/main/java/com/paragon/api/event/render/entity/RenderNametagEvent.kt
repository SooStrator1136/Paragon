package com.paragon.api.event.render.entity

import me.wolfsurge.cerauno.event.CancellableEvent
import net.minecraft.entity.Entity

/**
 * @author Surge
 */
class RenderNametagEvent(val entity: Entity) : CancellableEvent()