package com.paragon.api.event.client

import com.paragon.api.module.Module
import me.wolfsurge.cerauno.event.Event

/**
 * @author Wolfsurge
 */
class ModuleToggleEvent(val module: Module) : Event()