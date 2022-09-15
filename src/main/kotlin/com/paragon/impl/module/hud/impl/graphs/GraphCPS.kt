package com.paragon.impl.module.hud.impl.graphs

import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.setting.Setting
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.module.client.Colours
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.render.RenderUtil
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.network.play.client.CPacketUseEntity
import java.awt.Color

/**
 * @author SooStrator1136
 */
object GraphCPS : HUDModule("CPSGraph", "Graph showing your Crystals per second") {

    private val scale = Setting(
        "Size", 1.0, 0.1, 2.0, 0.1
    ) describedBy "Size of the graph"

    private val updateDelay = Setting(
        "Delay", 250.0, 75.0, 1000.0, 25.0
    )

    private val background = Setting("Background", Graph.Background.ALL)

    private val backgroundColor = Setting(
        "BackgroundColor", Color.BLACK.integrateAlpha(100F)
    ) describedBy "Color of the background"
    private val borderColor = Setting(
        "Border", Colours.mainColour.value
    ) describedBy "Color of the surrounding"
    private val graphColor = Setting(
        "Graph color", Colours.mainColour.value.darker()
    ) describedBy "Color of the graph"

    private var graph = Graph("Cps", backgroundColor::value, borderColor::value, graphColor::value, background::value)

    override fun onEnable() {
        graph = Graph("Cps", backgroundColor::value, borderColor::value, graphColor::value, background::value)
    }

    private var attackedCrystals = 0.0

    val timer = Timer()

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        if (timer.hasMSPassed(updateDelay.value)) {
            graph.update(attackedCrystals)
            attackedCrystals = 0.0
            timer.reset()
        }
    }

    @Listener
    fun onPacket(event: PacketEvent.PostSend) {
        if (event.packet is CPacketUseEntity && event.packet.getEntityFromWorld(minecraft.world) is EntityEnderCrystal) {
            attackedCrystals++
        }
    }

    override fun render() {
        graph.bounds.setRect(x, y, 75F, 30F)

        RenderUtil.scaleTo(x, y, 1F, scale.value, scale.value, 1.0) {
            graph.render()
        }
    }

    override var width = 77F
    override var height = 32F

}