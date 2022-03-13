package com.paragon.client.systems.module.impl.misc;

import com.paragon.client.managers.CommandManager;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Wolfsurge
 */
public class OnDeath extends Module {

    private final BooleanSetting printCoords = new BooleanSetting("Print Coords", "Prints your death coordinates in chat (client-side only)", true);
    private final BooleanSetting respawn = new BooleanSetting("Respawn", "Instantly respawn", true);

    public OnDeath() {
        super("OnDeath", ModuleCategory.MISC, "Do certain actions when you die");
        this.addSettings(printCoords, respawn);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        // Check that the entity that died has the same ID that the player does
        if (event.getEntity().getEntityId() == mc.player.getEntityId()) {
            Entity entity = event.getEntity();
            if (printCoords.isEnabled()) {
                BlockPos pos = entity.getPosition();

                // Build the death coord string
                String string = TextFormatting.RED + "You died at" +
                        TextFormatting.WHITE + " X " + TextFormatting.GRAY + pos.getX() +
                        TextFormatting.WHITE + " Y " + TextFormatting.GRAY + pos.getY() +
                        TextFormatting.WHITE + " Z " + TextFormatting.GRAY + pos.getZ();

                // Display the client message
                CommandManager.sendClientMessage(string, false);
            }

            if (respawn.isEnabled()) {
                // Respawn the player
                mc.player.respawnPlayer();
            }
        }
    }

}
