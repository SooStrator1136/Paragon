package com.paragon.client.systems.module.impl.misc;

import com.paragon.client.managers.CommandManager;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Wolfsurge
 */
public class OnDeath extends Module {

    public static OnDeath INSTANCE;

    public static Setting<Boolean> printCoords = new Setting<>("Print Coords", true)
            .setDescription("Prints your death coordinates in chat (client-side only)");

    public static Setting<Boolean> respawn = new Setting<>("Respawn", true)
            .setDescription("Respawns you after death");

    public OnDeath() {
        super("OnDeath", Category.MISC, "Do certain actions when you die");

        INSTANCE = this;
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (nullCheck()) {
            return;
        }

        // Check that the entity that died has the same ID that the player does
        if (event.getEntity().getEntityId() == mc.player.getEntityId()) {
            Entity entity = event.getEntity();
            if (printCoords.getValue()) {
                BlockPos pos = entity.getPosition();

                // Build the death coord string
                String string = TextFormatting.RED + "You died at" +
                        TextFormatting.WHITE + " X " + TextFormatting.GRAY + pos.getX() +
                        TextFormatting.WHITE + " Y " + TextFormatting.GRAY + pos.getY() +
                        TextFormatting.WHITE + " Z " + TextFormatting.GRAY + pos.getZ();

                // Display the client message
                CommandManager.sendClientMessage(string, false);
            }

            if (respawn.getValue()) {
                // Respawn the player
                mc.player.respawnPlayer();
            }
        }
    }

}
