package com.paragon.impl.event.client

import com.paragon.impl.module.Module
import com.paragon.bus.event.Event

/**
 * @author Surge
 */
class ModuleToggleEvent(val module: Module) : Event()