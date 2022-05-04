package com.paragon.api.event.render.gui;

import me.wolfsurge.cerauno.event.CancellableEvent;
import net.minecraft.item.ItemStack;

public class RenderTooltipEvent extends CancellableEvent {

    private ItemStack stack;
    private float x;
    private float y;

    public RenderTooltipEvent(ItemStack stack, float x, float y) {
        this.stack = stack;
        this.x = x;
        this.y = y;
    }

    public ItemStack getStack() {
        return stack;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

}
