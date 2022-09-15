package com.paragon.impl.event.player

import com.paragon.bus.event.Event

/**
 * @author Surge
 */
class PlayerMoveEvent(var x: Double, var y: Double, var z: Double) : Event()