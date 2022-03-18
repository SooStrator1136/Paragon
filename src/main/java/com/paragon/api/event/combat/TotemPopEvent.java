package com.paragon.api.event.combat;

import me.wolfsurge.cerauno.event.Event;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author Wolfsurge
 */
public class TotemPopEvent extends Event {

    private EntityPlayer player;

    public TotemPopEvent(EntityPlayer player) {
        this.player = player;
    }

    /**
     * Gets the player that popped the totem
     * @return The player that popped the totem
     */
    public EntityPlayer getPlayer() {
        return player;
    }

}
