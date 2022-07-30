package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.player.RaytraceEntityEvent;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPickaxe;

/**
 * @author Surge
 * @since 07/04/22
 */
public class NoTrace extends Module {

    public static NoTrace INSTANCE;

    // Settings
    public static Setting<Boolean> pickaxe = new Setting<>("Pickaxe", true)
            .setDescription("Ignores entities when you are holding a pickaxe");

    public static Setting<Boolean> blocks = new Setting<>("Blocks", false)
            .setDescription("Ignores entities when you are holding blocks");

    public static Setting<Boolean> crystals = new Setting<>("Crystals", true)
            .setDescription("Ignores entities when you are holding crystals");

    public NoTrace() {
        super("NoTrace", Category.MISC, "Ignores raytraced entities");

        INSTANCE = this;
    }

    @Listener
    public void onRaytrace(RaytraceEntityEvent event) {
        // Cancel if we are holding a pickaxe
        if (pickaxe.getValue() && mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe) {
            event.cancel();
        }

        // Cancel if we are holding crystals
        if (crystals.getValue() && mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL)) {
            event.cancel();
        }

        // Cancel if we are holding blocks
        if (blocks.getValue() && (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock || mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock)) {
            event.cancel();
        }
    }
}
