package com.paragon.api.event.client

import com.paragon.api.module.Module
import com.paragon.bus.event.Event

/**
 * @author Surge
 */
class ModuleToggleEvent(val module: Module) : Event()