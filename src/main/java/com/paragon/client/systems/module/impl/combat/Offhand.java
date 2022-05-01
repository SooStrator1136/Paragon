package com.paragon.client.systems.module.impl.combat;

import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.string.EnumFormatter;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;

/**
 * @author Wolfsurge
 */
public class Offhand extends Module {

    // Swap settings
    private final ModeSetting<ItemMode> priority = new ModeSetting<>("Priority", "The item we most want to use in the offhand", ItemMode.CRYSTAL);
    private final ModeSetting<ItemMode> secondary = new ModeSetting<>("Secondary", "The item we want to use if we cannot find the main item", ItemMode.TOTEM);
    private final BooleanSetting gappleSword = new BooleanSetting("Gapple Sword", "Swap to a gapple when wielding a sword", true);

    // Safety settings
    private final BooleanSetting safety = new BooleanSetting("Safety", "Switch to a totem in certain scenarios", true);
    private final BooleanSetting elytra = (BooleanSetting) new BooleanSetting("Elytra", "Switch to a totem when elytra flying", true).setParentSetting(safety);
    private final BooleanSetting falling = (BooleanSetting) new BooleanSetting("Falling", "Switch to a totem when you are falling more than 3 blocks (you will take damage)", true).setParentSetting(safety);
    private final BooleanSetting health = (BooleanSetting) new BooleanSetting("Health", "Switch to a totem when you are below a value", true).setParentSetting(safety);
    private final NumberSetting healthValue = (NumberSetting) new NumberSetting("Health Value", "The value we want to switch to a totem when you are below", 10, 0, 20, 1).setParentSetting(safety).setVisiblity(health::isEnabled);
    private final BooleanSetting lava = (BooleanSetting) new BooleanSetting("Lava", "Switch to a totem when you are in lava", true).setParentSetting(safety);
    private final BooleanSetting fire = (BooleanSetting) new BooleanSetting("Fire", "Switch to a totem when you are on fire", false).setParentSetting(safety);

    // Delay
    private final NumberSetting delay = new NumberSetting("Delay", "The delay between switching items", 0, 0, 200, 1);

    // Bypass settings
    private final BooleanSetting inventorySpoof = new BooleanSetting("Inventory Spoof", "Spoof opening your inventory", true);
    private final BooleanSetting cancelMotion = new BooleanSetting("Cancel Motion", "Cancel the motion of the player when switching items", false);

    // The timer to determine when to switch
    private final Timer switchTimer = new Timer();

    public Offhand() {
        super("Offhand", ModuleCategory.COMBAT, "Manages the item in your offhand");
        this.addSettings(priority, secondary, gappleSword, safety, delay, inventorySpoof, cancelMotion);
    }

    @Override
    public void onTick() {
        if (nullCheck() || mc.currentScreen instanceof GuiContainer || mc.player.isHandActive()) {
            return;
        }

        // Check time has passed
        if (switchTimer.hasMSPassed((long) delay.getValue() * 10)) {
            // Get slot to switch to
            int switchItemSlot = getSwapSlot();

            // Check we have a slot to switch
            if (switchItemSlot != -1) {
                // Spoof inventory
                if (inventorySpoof.isEnabled()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY));
                }

                // Cancel motion
                if (cancelMotion.isEnabled()) {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                    mc.player.setVelocity(0, mc.player.motionY, 0);
                }

                // Switch items
                swapOffhand(switchItemSlot);

                // Close inventory
                if (inventorySpoof.isEnabled()) {
                    mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
                }
            }

            switchTimer.reset();
        }
    }

    /**
     * Swaps the offhand item
     * @param slot The slot to switch to
     */
    public void swapOffhand(int slot) {
        // Click the slots
        mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

        // Get return slot
        int returnSlot = -1;
        for (int i = 9; i <= 44; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                returnSlot = i;
                break;
            }
        }

        // Click back to return slot
        if (returnSlot != -1) {
            mc.playerController.windowClick(0, returnSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.updateController();
        }
    }

    /**
     * Gets the slot to switch to
     * @return The slot to switch to
     */
    public int getSwapSlot() {
        // Get priority slot
        Item swap = priority.getCurrentMode().getItem();

        // Apply gapple sword
        if (gappleSword.isEnabled() && InventoryUtil.isHoldingSword()) {
            swap = Items.GOLDEN_APPLE;
        }

        // Apply safety
        if (safety.isEnabled()) {
            if (elytra.isEnabled() && mc.player.isElytraFlying() || falling.isEnabled() && mc.player.fallDistance > 3 || health.isEnabled() && mc.player.getHealth() < healthValue.getValue() || lava.isEnabled() && mc.player.isInLava() || fire.isEnabled() && mc.player.isBurning()) {
                swap = Items.TOTEM_OF_UNDYING;
            }
        }

        // Return -1 (no item) if we are already holding it
        if (mc.player.getHeldItemOffhand().getItem() == swap) {
            return -1;
        }

        // Get slot to switch to
        int swapSlot = InventoryUtil.getItemSlot(swap);

        // Get secondary slot if we couldn't find the priority slot
        if (swapSlot == -1) {
            swapSlot = InventoryUtil.getItemSlot(secondary.getCurrentMode().getItem());
        }

        // Return slot
        return swapSlot;
    }

    @Override
    public String getArrayListInfo() {
        return " " + EnumFormatter.getFormattedText(priority.getCurrentMode()) + ", " + InventoryUtil.getCountOfItem(secondary.getCurrentMode().getItem(), false, true);
    }

    public enum ItemMode {
        /**
         * Switch to a totem of undying
         */
        TOTEM(Items.TOTEM_OF_UNDYING),

        /**
         * Switch to an end crystal
         */
        CRYSTAL(Items.END_CRYSTAL),

        /**
         * Switch to a golden apple
         */
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