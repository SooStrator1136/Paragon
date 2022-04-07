package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.player.RaytraceEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
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
    private final BooleanSetting pickaxe = new BooleanSetting("Pickaxe", "Ignores entities when you are holding a pickaxe", true);
    private final BooleanSetting blocks = new BooleanSetting("Blocks", "Ignores entities when you are holding blocks", false);
    private final BooleanSetting crystals = new BooleanSetting("Crystals", "Ignores entities when you are holding crystals", true);

    public NoTrace() {
        super("NoTrace", ModuleCategory.MISC, "Ignores raytraced entities");
        this.addSettings(pickaxe, blocks, crystals);
    }

    @Listener
    public void onRaytrace(RaytraceEvent event) {
        // Cancel if we are holding a pickaxe
        if (pickaxe.isEnabled() && mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe) {
            event.cancel();
        }

        // Cancel if we are holding crystals
        if (crystals.isEnabled() && mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL)) {
            event.cancel();
        }

        // Cancel if we are holding blocks
        if (blocks.isEnabled() && (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock || mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock)) {
            event.cancel();
        }
    }
}
