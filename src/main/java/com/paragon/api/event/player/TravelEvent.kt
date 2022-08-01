package com.paragon.api.event.player

import me.wolfsurge.cerauno.event.CancellableEvent

/**
 * @author Surge
 */
class TravelEvent(var strafe: Float, var vertical: Float, var forward: Float) : CancellableEvent()