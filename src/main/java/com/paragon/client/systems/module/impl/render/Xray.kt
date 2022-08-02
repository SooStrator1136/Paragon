package com.paragon.client.systems.module.impl.render

import com.paragon.api.event.render.world.*
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.util.Wrapper
import com.paragon.api.util.world.BlockUtil.getBlockAtPos
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.init.Blocks
import java.util.*

/**
 * @author Surge
 */
class Xray : Module("Xray", Category.RENDER, "Lets you see ores and liquids through blocks") {
    
    private val visibleBlocks = listOf(
        Blocks.DIAMOND_ORE,
        Blocks.IRON_ORE,
        Blocks.GOLD_ORE,
        Blocks.COAL_ORE,
        Blocks.LAPIS_ORE,
        Blocks.QUARTZ_ORE,
        Blocks.EMERALD_ORE,
        Blocks.REDSTONE_ORE,
        Blocks.LIT_REDSTONE_ORE,
        Blocks.WATER,
        Blocks.FLOWING_WATER,
        Blocks.LAVA,
        Blocks.FLOWING_LAVA,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.ENDER_CHEST
    )

    override fun onEnable() {
        if (nullCheck()) {
            return
        }
        
        minecraft.renderGlobal.loadRenderers()
    }

    override fun onDisable() {
        if (nullCheck()) {
            return
        }
        
        minecraft.renderGlobal.loadRenderers()
    }

    @Listener
    fun onBlockSetOpaque(event: BlockSetOpaqueEvent) {
        event.cancel()
    }

    @Listener
    fun onFullCubeBlock(event: FullCubeBlockEvent) {
        event.returnValue = visibleBlocks.contains(event.block)
        event.cancel()
    }

    @Listener
    fun onRenderBlockModel(event: RenderBlockModelEvent) {
        event.returnValue = !visibleBlocks.contains(getBlockAtPos(event.pos))
        event.cancel()
    }

    @Listener
    fun onRenderSmooth(event: RenderBlockSmoothEvent) {
        event.returnValue = !visibleBlocks.contains(getBlockAtPos(event.pos))
        event.cancel()
    }

    @Listener
    fun onSideRenderBlock(event: SideRenderBlockEvent) {
        event.returnValue = visibleBlocks.contains(getBlockAtPos(event.pos))
        event.cancel()
    }
}