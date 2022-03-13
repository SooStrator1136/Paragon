package com.paragon.api.event.player;

import me.wolfsurge.cerauno.event.Event;
import net.minecraft.util.EnumHandSide;

/**
 * @author Wolfsurge
 */
public class RenderItemEvent extends Event {

    private EnumHandSide side;

    public RenderItemEvent(EnumHandSide enumHandSide) {
        this.side = enumHandSide;
    }

    public EnumHandSide getSide() {
        return side;
    }

    public static class Pre extends RenderItemEvent {
        public Pre(EnumHandSide enumHandSide) {
            super(enumHandSide);
        }
    }

    public static class Post extends RenderItemEvent {
        public Post(EnumHandSide enumHandSide) {
            super(enumHandSide);
        }
    }

}
