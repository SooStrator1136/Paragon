package com.paragon.api.event.input

import me.wolfsurge.cerauno.event.CancellableEvent

class KeybindingPressedEvent(val keyCode: Int, var pressedState: Boolean) : CancellableEvent()