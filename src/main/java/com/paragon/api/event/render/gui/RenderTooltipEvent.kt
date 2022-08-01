package com.paragon.api.event.render.gui

import me.wolfsurge.cerauno.event.CancellableEvent
import net.minecraft.item.ItemStack

/**
 * @author Surge
 */
class RenderTooltipEvent(val stack: ItemStack, val x: Float, val y: Float) : CancellableEvent()