package com.paragon.api.event.render

import me.wolfsurge.cerauno.event.CancellableEvent
import net.minecraft.entity.Entity
import java.awt.Color

/**
 * @author Surge
 */
class ShaderColourEvent(var entity: Entity) : CancellableEvent() {

    var colour: Color? = null

}