package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.player.RaytraceEntityEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPickaxe;

/**
 * @author Wolfsurge
 * @since 07/04/22
 */
public class NoTrace extends Module {

    // Settings
    private final Setting<Boolean> pickaxe = new Setting<>("Pickaxe", true)
            .setDescription("Ignores entities when you are holding a pickaxe");

    private final Setting<Boolean> blocks = new Setting<>("Blocks", false)
            .setDescription("Ignores entities when you are holding blocks");

    private final Setting<Boolean> crystals = new Setting<>("Crystals", true)
            .setDescription("Ignores entities when you are holding crystals");

    public NoTrace() {
        super("NoTrace", ModuleCategory.MISC, "Ignores raytraced entities");
        this.addSettings(pickaxe, blocks, crystals);
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
