package com.paragon.client.systems.module.impl.combat;

import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.string.EnumFormatter;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.setting.Setting;
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
    private final Setting<ItemMode> priority = new Setting<>("Priority", ItemMode.CRYSTAL)
            .setDescription("The item we most want to use in the offhand");

    private final Setting<ItemMode> secondary = new Setting<>("Secondary", ItemMode.TOTEM)
            .setDescription("The item we want to use if we cannot find the main item");

    private final Setting<Boolean> gappleSword = new Setting<>("Gapple Sword", true)
            .setDescription("Swap to a gapple when wielding a sword");

    // Safety settings
    private final Setting<Boolean> safety = new Setting<>("Safety", true)
            .setDescription("Switch to a totem in certain scenarios");

    private final Setting<Boolean> elytra = new Setting<>("Elytra", true)
            .setDescription("Switch to a totem when elytra flying")
            .setParentSetting(safety);

    private final Setting<Boolean> falling = new Setting<>("Falling", true)
            .setDescription("Switch to a totem when you will take fall damage")
            .setParentSetting(safety);

    private final Setting<Boolean> health = new Setting<>("Health", true)
            .setDescription("Switch to a totem when you are below a value")
            .setParentSetting(safety);

    private final Setting<Float> healthValue = new Setting<>("Health Value", 10f, 0f, 20f, 1f)
            .setDescription("The value we want to switch to a totem when you are below")
            .setParentSetting(safety)
            .setVisibility(health::getValue);

    private final Setting<Boolean> lava = new Setting<>("Lava", true)
            .setDescription("Switch to a totem when you are in lava")
            .setParentSetting(safety);

    private final Setting<Boolean> fire = new Setting<>("Fire", false)
            .setDescription("Switch to a totem when you are on fire")
            .setParentSetting(safety);

    // Delay
    private final Setting<Double> delay = new Setting<>("Delay", 0D, 0D, 200D, 1D)
            .setDescription("The delay between switching items");

    // Bypass settings
    private final Setting<Boolean> inventorySpoof = new Setting<>("Inventory Spoof", true)
            .setDescription("Spoof opening your inventory");

    private final Setting<Boolean> cancelMotion = new Setting<>("Cancel Motion", false)
            .setDescription("Cancel the motion of the player when switching items");

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
        if (switchTimer.hasMSPassed(delay.getValue().longValue() * 10)) {
            // Get slot to switch to
            int switchItemSlot = getSwapSlot();

            // Check we have a slot to switch
            if (switchItemSlot != -1) {
                // Spoof inventory
                if (inventorySpoof.getValue() && mc.getConnection() != null) {
                    mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY));
                }

                // Cancel motion
                if (cancelMotion.getValue()) {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                    mc.player.setVelocity(0, mc.player.motionY, 0);
                }

                // Switch items
                swapOffhand(switchItemSlot);

                // Close inventory
                if (inventorySpoof.getValue() && mc.getConnection() != null && mc.player.inventoryContainer != null) {
                    mc.getConnection().sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
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
        Item swap = priority.getValue().getItem();

        // Apply gapple sword
        if (gappleSword.getValue() && InventoryUtil.isHoldingSword()) {
            swap = Items.GOLDEN_APPLE;
        }

        // Apply safety
        if (safety.getValue()) {
            if (elytra.getValue() && mc.player.isElytraFlying() || falling.getValue() && mc.player.fallDistance > 3 || health.getValue() && mc.player.getHealth() < healthValue.getValue() || lava.getValue() && mc.player.isInLava() || fire.getValue() && mc.player.isBurning()) {
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
            swapSlot = InventoryUtil.getItemSlot(secondary.getValue().getItem());
        }

        // Return slot
        return swapSlot;
    }

    @Override
    public String getArrayListInfo() {
        return " " + EnumFormatter.getFormattedText(priority.getValue()) + ", " + InventoryUtil.getCountOfItem(secondary.getValue().getItem(), false, true);
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