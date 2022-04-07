package com.paragon.client.systems.module.impl.combat;

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

public class Offhand extends Module {

    private final ModeSetting<Main> main = new ModeSetting<>("Main", "The item you want to prioritise the most", Main.TOTEM);
    private final ModeSetting<Fallback> fallback = new ModeSetting<>("Fallback", "The item you want to switch to if you don't have the main item", Fallback.GAPPLE);

    private final BooleanSetting strictInv = new BooleanSetting("Strict Inventory", "Fake opening your inventory", true);
    private final BooleanSetting pauseMotion = new BooleanSetting("Pause motion", "Stop moving when swapping items", true);

    private final BooleanSetting safety = new BooleanSetting("Safety", "Switch to totems when certain things happen", true);
    private final BooleanSetting elytra = (BooleanSetting) new BooleanSetting("Elytra", "Switch to a totem when flying with an elytra", false).setParentSetting(safety);
    private final BooleanSetting fall = (BooleanSetting) new BooleanSetting("Falling", "Switch to a totem when falling", true).setParentSetting(safety);
    private final BooleanSetting lowHealth = (BooleanSetting) new BooleanSetting("Low Health", "Switch to a totem when on low health", true).setParentSetting(safety);
    private final NumberSetting lowHealthValue = (NumberSetting) new NumberSetting("Health", "The health to switch to totems", 10, 1, 20, 1).setParentSetting(safety).setVisiblity(lowHealth::isEnabled);

    private boolean hasJustPausedMotion = false;

    public Offhand() {
        super("Offhand", ModuleCategory.COMBAT, "Automatically manages the items in your offhand");
        this.addSettings(main, fallback, strictInv, pauseMotion, safety);
    }

    @Override
    public void onTick() {
        if (nullCheck() || mc.currentScreen != null) {
            return;
        }

        // Get the item we want to switch to
        Item itemToSwitch = getItemToSwitch(false);

        // Do fallback
        if (itemToSwitch == null) {
            itemToSwitch = getItemToSwitch(true);
        }

        // Cancel if we are already holding the item
        if (mc.player.getHeldItemOffhand().getItem() == itemToSwitch) {
            return;
        }

        // Get item slot
        int slot = InventoryUtil.getItemSlot(itemToSwitch);

        // Return if we couldn't find an item
        if (slot == -1) {
            return;
        }

        // Open inventory
        if (strictInv.isEnabled()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY));
        }

        // Pause movement
        if (pauseMotion.isEnabled() && !hasJustPausedMotion) {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
            mc.player.setVelocity(0, mc.player.motionY, 0);
            hasJustPausedMotion = true;
            return;
        }

        // Swap item
        InventoryUtil.swapOffhand(slot);

        // Close inventory
        if (strictInv.isEnabled()) {
            mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
        }
    }

    /**
     * Gets the item to switch to
     * @return The item to switch to
     */
    private Item getItemToSwitch(boolean fallbackItem) {
        if (safety.isEnabled()) {
            // Prioritise low health
            if (lowHealth.isEnabled()) {
                if (mc.player.getHealth() <= lowHealthValue.getValue()) {
                    return Items.TOTEM_OF_UNDYING;
                }
            }

            if (fall.isEnabled()) {
                // We will take fall damage
                if (mc.player.fallDistance > 3) {
                    return Items.TOTEM_OF_UNDYING;
                }
            }

            if (elytra.isEnabled()) {
                // We are flying with an elytra
                if (mc.player.isElytraFlying()) {
                    return Items.TOTEM_OF_UNDYING;
                }
            }
        }

        if (!fallbackItem) {
            // Get main item
            return main.getCurrentMode().getItem();
        } else {
            // Get fallback item
            return fallback.getCurrentMode().getItem();
        }
    }

    public enum Main {
        /**
         * Switch to totem
         */
        TOTEM(Items.TOTEM_OF_UNDYING),

        /**
         * Switch to gapple
         */
        GAPPLE(Items.GOLDEN_APPLE),

        /**
         * Switch to crystal
         */
        CRYSTAL(Items.END_CRYSTAL);

        private Item item;

        Main(Item item) {
            this.item = item;
        }

        /**
         * Gets the item to switch to
         * @return The item to switch to
         */
        public Item getItem() {
            return item;
        }
    }

    public enum Fallback {
        /**
         * Switch to totem
         */
        TOTEM(Items.TOTEM_OF_UNDYING),

        /**
         * Switch to gapple
         */
        GAPPLE(Items.GOLDEN_APPLE),

        /**
         * Switch to crystal
         */
        CRYSTAL(Items.END_CRYSTAL);

        private Item item;

        Fallback(Item item) {
            this.item = item;
        }

        /**
         * Gets the item to switch to
         * @return The item to switch to
         */
        public Item getItem() {
            return item;
        }
    }

}
