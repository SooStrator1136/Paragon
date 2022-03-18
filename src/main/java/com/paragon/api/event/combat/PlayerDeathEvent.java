package com.paragon.api.event.combat;

import me.wolfsurge.cerauno.event.Event;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author Wolfsurge
 */
public class PlayerDeathEvent extends Event {

    private EntityPlayer entityPlayer;
    private int pops = 0;

    public PlayerDeathEvent(EntityPlayer player) {
        this.entityPlayer = player;
    }

    public PlayerDeathEvent(EntityPlayer entityPlayer, int pops) {
        this.entityPlayer = entityPlayer;
        this.pops = pops;
    }

    /**
     * Gets the player that died
     * @return The player that died
     */
    public EntityPlayer getEntityPlayer() {
        return entityPlayer;
    }

    /**
     * Gets the amount of times the player popped totems before dying
     * @return The amount of times the player popped totems before dying
     */
    public int getPops() {
        return pops;
    }

}
