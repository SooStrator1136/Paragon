package com.paragon.impl.event.combat

import com.paragon.bus.event.Event
import net.minecraft.entity.player.EntityPlayer

/**
 * @author Surge
 */
class TotemPopEvent(val player: EntityPlayer) : Event()