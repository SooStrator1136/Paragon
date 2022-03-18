package com.paragon.client.managers;

import com.paragon.Paragon;
import com.paragon.api.event.combat.PlayerDeathEvent;
import com.paragon.api.event.combat.TotemPopEvent;
import com.paragon.api.event.world.entity.EntityRemoveFromWorldEvent;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Wolfsurge
 */
public class PopManager {

    private final Map<EntityPlayer, Integer> pops = new HashMap<>();

    public PopManager() {
        MinecraftForge.EVENT_BUS.register(this);
        Paragon.INSTANCE.getEventBus().register(this);
    }

    @Listener
    public void onTotemPop(TotemPopEvent event) {
        pops.put(event.getPlayer(), pops.containsKey(event.getPlayer()) ? pops.get(event.getPlayer()) + 1 : 1);
    }

    @Listener
    public void onEntityRemove(EntityRemoveFromWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            if (pops.containsKey((EntityPlayer) event.getEntity())) {
                PlayerDeathEvent playerDeathEvent = new PlayerDeathEvent((EntityPlayer) event.getEntity(), getPops((EntityPlayer) event.getEntity()));
                Paragon.INSTANCE.getEventBus().post(playerDeathEvent);

                pops.remove((EntityPlayer) event.getEntity());
            }
        }
    }

    public int getPops(EntityPlayer player) {
        return pops.getOrDefault(player, 0);
    }

}
