package com.paragon.api.event.render.entity

import com.paragon.bus.event.CancellableEvent

/**
 * @author Surge
 */
class CameraClipEvent(var distance: Double) : CancellableEvent()