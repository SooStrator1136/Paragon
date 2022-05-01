package com.paragon.client.systems.module.impl.combat;

import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

/**
 * @author Wolfsurge
 */
public class Offhand extends Module {

    private final ModeSetting<ItemMode> primary = new ModeSetting<>("Primary", "The item you most want to switch to", ItemMode.CRYSTAL);
    private final ModeSetting<ItemMode> secondary = new ModeSetting<>("Secondary", "The item you least want to switch to", ItemMode.TOTEM);

    private final NumberSetting delay = new NumberSetting("Delay", "The delay between switching items", 0, 0, 100, 1);

    private final Timer timer = new Timer();

    public Offhand() {
        super("Offhand", ModuleCategory.COMBAT, "Manages the item in your offhand");
        this.addSettings(primary, secondary, delay);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        if (timer.hasMSPassed((long) delay.getValue())) {
            return;
        }

        int switchPrimary = InventoryUtil.getItemSlot(primary.getCurrentMode().getItem());
        int switchSecondary = InventoryUtil.getItemSlot(secondary.getCurrentMode().getItem());

        int switchSlot = switchPrimary == -1 ? switchSecondary : switchPrimary;

        if (switchSlot == -1) {
            return;
        }

        InventoryUtil.swapOffhand(switchSlot);

        timer.reset();
    }

    public enum ItemMode {
        CRYSTAL(Items.END_CRYSTAL),
        TOTEM(Items.TOTEM_OF_UNDYING),
        GAPPLE(Items.GOLDEN_APPLE);

        private Item item;

        ItemMode(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }
    }

}
