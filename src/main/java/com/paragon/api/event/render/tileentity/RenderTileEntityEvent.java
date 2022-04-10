package com.paragon.api.event.render.tileentity;

import me.wolfsurge.cerauno.event.Event;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

/**
 * @author Wolfsurge
 */
public class RenderTileEntityEvent extends Event {

    // The tile entity renderer
    private final TileEntityRendererDispatcher tileEntityRendererDispatcher;

    // The tile entity being rendered
    private final TileEntity tileEntityIn;

    // Attributes
    private final float partialTicks;
    private final double staticPlayerX;
    private final double staticPlayerY;
    private final double staticPlayerZ;

    public RenderTileEntityEvent(TileEntityRendererDispatcher tileEntityRendererDispatcher, TileEntity tileEntityIn, float partialTicks, double staticPlayerX, double staticPlayerY, double staticPlayerZ) {
        this.tileEntityRendererDispatcher = tileEntityRendererDispatcher;
        this.tileEntityIn = tileEntityIn;
        this.partialTicks = partialTicks;
        this.staticPlayerX = staticPlayerX;
        this.staticPlayerY = staticPlayerY;
        this.staticPlayerZ = staticPlayerZ;
    }

    public TileEntityRendererDispatcher getTileEntityRendererDispatcher() {
        return this.tileEntityRendererDispatcher;
    }

    public TileEntity getTileEntityIn() {
        return this.tileEntityIn;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public double getStaticPlayerX() {
        return this.staticPlayerX;
    }

    public double getStaticPlayerY() {
        return this.staticPlayerY;
    }

    public double getStaticPlayerZ() {
        return this.staticPlayerZ;
    }
}