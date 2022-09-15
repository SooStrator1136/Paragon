package com.paragon.impl.event.combat

import com.paragon.bus.event.Event
import net.minecraft.entity.player.EntityPlayer

/**
 * @author Surge
 */
class PlayerDeathEvent(val entityPlayer: EntityPlayer, val pops: Int) : Event()