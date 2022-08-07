package com.paragon.api.event.render.tileentity

import me.wolfsurge.cerauno.event.Event
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.tileentity.TileEntity

/**
 * @author Surge
 */
class RenderTileEntityEvent(
    val tileEntityRendererDispatcher: TileEntityRendererDispatcher,
    val tileEntityIn: TileEntity,
    val partialTicks: Float,
    val staticPlayerX: Double,
    val staticPlayerY: Double,
    val staticPlayerZ: Double
) : Event()