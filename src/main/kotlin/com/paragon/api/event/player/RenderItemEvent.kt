package com.paragon.api.event.player

import com.paragon.bus.event.Event
import net.minecraft.util.EnumHandSide

/**
 * @author Surge
 */
open class RenderItemEvent(val side: EnumHandSide) : Event() {

    class Pre(enumHandSide: EnumHandSide) : RenderItemEvent(enumHandSide)
    class Post(enumHandSide: EnumHandSide) : RenderItemEvent(enumHandSide)

}