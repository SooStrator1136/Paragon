package com.paragon.api.event.combat

import me.wolfsurge.cerauno.event.Event
import net.minecraft.entity.player.EntityPlayer

/**
 * @author Wolfsurge
 */
class PlayerDeathEvent(val entityPlayer: EntityPlayer, val pops: Int) : Event()