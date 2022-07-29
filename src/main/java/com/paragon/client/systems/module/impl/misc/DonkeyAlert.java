package com.paragon.client.systems.module.impl.misc;

import com.paragon.Paragon;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Setting;
import com.paragon.client.managers.notifications.Notification;
import com.paragon.client.managers.notifications.NotificationType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityMule;

public class DonkeyAlert extends Module {
    public static Setting<Boolean> donkeyAlert = new Setting<>("Donkeys", true)
            .setDescription("Alert for donkeys");

    public static Setting<Boolean> llamaAlert = new Setting<>("Donkeys", true)
            .setDescription("Alert for llamas");

    public static Setting<Boolean> horseAlert = new Setting<>("Donkeys", true)
            .setDescription("Alert for horses");

    public static Setting<Boolean> muleAlert = new Setting<>("Donkeys", true)
            .setDescription("Alert for mules");

    public static Setting<AlertType> alertMode = new Setting<>("Alert", AlertType.Message);


    public DonkeyAlert() {
        super("Donkey Alert", Category.MISC, "Allows you to find donkeys adn other rideable entities easier");
    }


    private int antiSpam;

    @Override
    public void onTick() {
        antiSpam++;

        for (Entity e : Minecraft.getMinecraft().world.loadedEntityList) {
            if (e instanceof EntityDonkey && donkeyAlert.getValue()) {
                if (antiSpam >= 100) {
                    if(alertMode.getValue() == AlertType.Notification) {
                        Paragon.INSTANCE.getNotificationManager().addNotification(new Notification(" Found Donkey!" + " X:" + (int) e.posX + " Z:" + (int) e.posZ, NotificationType.INFO));
                    }
                    else{
                        Paragon.INSTANCE.getCommandManager().sendClientMessage(" Found Donkey!" + " X:" + (int) e.posX + " Z:" + (int) e.posZ, false);
                    }
                    antiSpam = -600;
                }
            }
            if (e instanceof EntityMule && muleAlert.getValue()) {
                if (antiSpam >= 100) {
                    if(alertMode.getValue() == AlertType.Notification) {
                        Paragon.INSTANCE.getNotificationManager().addNotification(new Notification(" Found Mule!" + " X:" + (int) e.posX + " Z:" + (int) e.posZ, NotificationType.INFO));
                    }
                    else{
                        Paragon.INSTANCE.getCommandManager().sendClientMessage(" Found Mule!" + " X:" + (int) e.posX + " Z:" + (int) e.posZ, false);
                    }
                    antiSpam = -600;
                }

            }
            if (e instanceof EntityLlama && llamaAlert.getValue()) {
                if (antiSpam >= 100) {
                    if(alertMode.getValue() == AlertType.Notification) {
                        Paragon.INSTANCE.getNotificationManager().addNotification(new Notification(" Found Llama!" + " X:" + (int) e.posX + " Z:" + (int) e.posZ, NotificationType.INFO));
                    }
                    else{
                        Paragon.INSTANCE.getCommandManager().sendClientMessage(" Found Llama!" + " X:" + (int) e.posX + " Z:" + (int) e.posZ, false);
                    }
                    antiSpam = -600;
                }

            }
            if (e instanceof EntityHorse && horseAlert.getValue()) {
                if (antiSpam >= 100) {
                    if(alertMode.getValue() == AlertType.Notification) {
                        Paragon.INSTANCE.getNotificationManager().addNotification(new Notification(" Found Horse!" + " X:" + (int) e.posX + " Z:" + (int) e.posZ, NotificationType.INFO));
                    }
                    else{
                        Paragon.INSTANCE.getCommandManager().sendClientMessage(" Found Horse!" + " X:" + (int) e.posX + " Z:" + (int) e.posZ, false);
                    }
                    antiSpam = -600;
                }

            }

        }
    }

    enum AlertType{
        Notification,
        Message
    }

}
