package com.paragon.api.event.render.gui

import com.paragon.bus.event.CancellableEvent

/**
 * @author Surge
 */
class RenderChatEvent(var colour: Int) : CancellableEvent()