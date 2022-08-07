package com.paragon.api.event.render.entity

import me.wolfsurge.cerauno.event.CancellableEvent
import java.awt.Color

/**
 * @author Surge
 */
class EntityHighlightOnHitEvent : CancellableEvent() {

    var colour: Color? = null

}