package com.paragon.client.systems.module.impl.combat;

import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.server.SPacketOpenWindow;

/**
 * @author Wolfsurge
 */
public class Offhand extends Module {

    private final ModeSetting<ItemMode> priority = new ModeSetting<>("Priority", "The item we most want to use in the offhand", ItemMode.END_CRYSTAL);
    private final ModeSetting<ItemMode> secondary = new ModeSetting<>("Secondary", "The item we want to use if we cannot find the main item", ItemMode.TOTEM_OF_UNDYING);

    private final BooleanSetting safety = new BooleanSetting("Safety", "Switch to a totem in certain scenarios", true);
    private final BooleanSetting elytra = (BooleanSetting) new BooleanSetting("Elytra", "Switch to a totem when elytra flying", true).setParentSetting(safety);
    private final BooleanSetting falling = (BooleanSetting) new BooleanSetting("Falling", "Switch to a totem when you are falling more than 3 blocks (you will take damage)", true).setParentSetting(safety);
    private final BooleanSetting health = (BooleanSetting) new BooleanSetting("Health", "Switch to a totem when you are below a value", true).setParentSetting(safety);
    private final NumberSetting healthValue = (NumberSetting) new NumberSetting("Health Value", "The value we want to switch to a totem when you are below", 10, 0, 20, 1).setParentSetting(safety).setVisiblity(health::isEnabled);
    private final BooleanSetting lava = (BooleanSetting) new BooleanSetting("Lava", "Switch to a totem when you are in lava", true).setParentSetting(safety);
    private final BooleanSetting fire = (BooleanSetting) new BooleanSetting("Fire", "Switch to a totem when you are on fire", false).setParentSetting(safety);

    private final NumberSetting delay = new NumberSetting("Delay", "The delay between switching items", 0, 0, 200, 1);

    private final BooleanSetting inventorySpoof = new BooleanSetting("Inventory Spoof", "Spoof opening your inventory", true);

    private final Timer switchTimer = new Timer();

    public Offhand() {
        super("Offhand", ModuleCategory.COMBAT, "Manages the item in your offhand");
        this.addSettings(priority, secondary, safety, delay, inventorySpoof);
    }

    @Override
    public void onTick() {
        if (nullCheck() || mc.player.getHeldItemOffhand().getItem().equals(priority.getCurrentMode().getItem()) || mc.player.getHeldItemOffhand().getItem().equals(secondary.getCurrentMode().getItem())) {
            return;
        }

        if (switchTimer.hasTimePassed((long) delay.getValue() / 10, Timer.TimeFormat.SECONDS)) {
            int switchItemSlot = InventoryUtil.getItemSlot(priority.getCurrentMode().getItem());

            if (switchItemSlot == -1) {
                switchItemSlot = InventoryUtil.getItemSlot(secondary.getCurrentMode().getItem());
            }

            if (safety.isEnabled()) {
                if (elytra.isEnabled() && mc.player.isElytraFlying() || falling.isEnabled() && mc.player.fallDistance > 3 || health.isEnabled() && mc.player.getHealth() < healthValue.getValue() || lava.isEnabled() && mc.player.isInLava() || fire.isEnabled() && mc.player.isBurning()) {
                    switchItemSlot = InventoryUtil.getItemSlot(Items.TOTEM_OF_UNDYING);
                }
            }

            if (switchItemSlot != -1) {
                if (inventorySpoof.isEnabled()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY));
                }

                InventoryUtil.swapOffhand(switchItemSlot);

                if (inventorySpoof.isEnabled()) {
                    mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
                }
            }

            switchTimer.reset();
        }
    }

    @Override
    public String getModuleInfo() {
        return " " + InventoryUtil.getCountOfItem(priority.getCurrentMode().getItem(), false) + ", " + InventoryUtil.getCountOfItem(secondary.getCurrentMode().getItem(), false);
    }

    public enum ItemMode {
        TOTEM_OF_UNDYING(Items.TOTEM_OF_UNDYING),
        END_CRYSTAL(Items.END_CRYSTAL),
        GAPPLE(Items.GOLDEN_APPLE);

        // The item we want to switch to
        private final Item item;

        ItemMode(Item item) {
            this.item = item;
        }

        /**
         * Gets the item we want to switch to
         * @return The item we want to switch to
         */
        public Item getItem() {
            return item;
        }
    }

}
