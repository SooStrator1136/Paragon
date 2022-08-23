package com.paragon.client.systems.module.hud.impl.graphs

import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil.scaleTo
import com.paragon.client.systems.module.hud.HUDModule
import com.paragon.client.systems.module.hud.impl.Speed
import com.paragon.client.systems.module.impl.client.Colours
import java.awt.Color

/**
 * @author SooStrator1136
 */
object GraphSpeed : HUDModule("SpeedGraph", "Graph showing your speed") {

    private val scale = Setting(
        "Size",
        1.0,
        0.1,
        2.0,
        0.1
    ) describedBy "Size of the graph"

    private val background = Setting("Background", Graph.Background.ALL)

    private val backgroundColor = Setting(
        "BackgroundColor",
        Color.BLACK.integrateAlpha(100F)
    ) describedBy "Color of the background"
    private val borderColor = Setting(
        "Border",
        Colours.mainColour.value
    ) describedBy "Color of the surrounding"
    private val graphColor = Setting(
        "Graph color",
        Colours.mainColour.value.darker()
    ) describedBy "Color of the graph"

    private var graph = Graph("Speed", backgroundColor::value, borderColor::value, graphColor::value, background::value)

    override fun onEnable() {
        graph = Graph("Speed", backgroundColor::value, borderColor::value, graphColor::value, background::value)
    }

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        graph.update(
            Speed.getPlayerSpeed(
                minecraft.player.posX - minecraft.player.lastTickPosX,
                minecraft.player.posZ - minecraft.player.lastTickPosZ
            )
        )
    }

    override fun render() {
        graph.bounds.setRect(x, y, 75F, 30F)

        scaleTo(x, y, 1F, scale.value, scale.value, 1.0) {
            graph.render()
        }
    }

    override var width = 77F
    override var height = 32F

}