package com.paragon.api.event.render.entity

import com.paragon.bus.event.CancellableEvent
import java.awt.Color

/**
 * @author Surge
 */
class EntityHighlightOnHitEvent : CancellableEvent() {

    var colour: Color? = null

}