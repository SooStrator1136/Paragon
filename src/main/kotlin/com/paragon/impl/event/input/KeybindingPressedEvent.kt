package com.paragon.impl.event.input

import com.paragon.bus.event.CancellableEvent

class KeybindingPressedEvent(val keyCode: Int, var pressedState: Boolean) : CancellableEvent()