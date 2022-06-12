package com.paragon.client.systems.module.impl.combat;

import com.paragon.Paragon;
import com.paragon.client.managers.notifications.Notification;
import com.paragon.client.managers.notifications.NotificationType;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CevBreaker extends Module {

    public static Setting<Double> range = new Setting<>("Range", 5.0D, 1.0D, 7.0D, 0.1D)
            .setDescription("Targeting range");

    public CevBreaker() {
        super("CevBreaker", Category.COMBAT, "Places and breaks crystals above opponents to deal massive damage");
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        List<Entity> validPlayers = mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityPlayer && entity != mc.player && mc.player.getDistance(entity) <= range.getValue()).sorted(Comparator.comparingDouble(entity -> mc.player.getDistance(entity))).collect(Collectors.toList());

        if (validPlayers.isEmpty()) {
            Paragon.INSTANCE.getNotificationManager().addNotification(new Notification("No targets!", NotificationType.INFO));
            toggle();
            return;
        }
    }
}
