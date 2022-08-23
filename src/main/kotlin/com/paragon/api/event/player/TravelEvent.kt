package com.paragon.api.event.player

import com.paragon.bus.event.CancellableEvent

/**
 * @author Surge
 */
class TravelEvent(var strafe: Float, var vertical: Float, var forward: Float) : CancellableEvent()