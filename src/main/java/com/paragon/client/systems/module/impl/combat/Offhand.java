package com.paragon.client.systems.module.impl.combat;

import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.string.EnumFormatter;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;

/**
 * @author Wolfsurge
 * @since 01/05/2022
 */
public class Offhand extends Module {

    // Switch
    private final ModeSetting<ItemMode> primary = new ModeSetting<>("Primary", "The item you most want to switch to", ItemMode.CRYSTAL);
    private final ModeSetting<ItemMode> secondary = new ModeSetting<>("Secondary", "The item you least want to switch to", ItemMode.TOTEM);
    private final BooleanSetting gappleSword = new BooleanSetting("Gapple Sword", "Switches to a gapple in your offhand when you are wielding a sword", true);

    // Safety
    private final BooleanSetting safety = new BooleanSetting("Safety", "Switch to a totem in certain scenarios", true);
    private final BooleanSetting elytra = (BooleanSetting) new BooleanSetting("Elytra", "Switch to a totem when elytra flying", true).setParentSetting(safety);
    private final BooleanSetting falling = (BooleanSetting) new BooleanSetting("Falling", "Switch to a totem when you are falling more than 3 blocks (you will take damage)", true).setParentSetting(safety);
    private final BooleanSetting health = (BooleanSetting) new BooleanSetting("Health", "Switch to a totem when you are below a value", true).setParentSetting(safety);
    private final NumberSetting healthValue = (NumberSetting) new NumberSetting("Health Value", "The value we want to switch to a totem when you are below", 10, 0, 20, 1).setParentSetting(safety).setVisiblity(health::isEnabled);
    private final BooleanSetting lava = (BooleanSetting) new BooleanSetting("Lava", "Switch to a totem when you are in lava", true).setParentSetting(safety);
    private final BooleanSetting fire = (BooleanSetting) new BooleanSetting("Fire", "Switch to a totem when you are on fire", false).setParentSetting(safety);

    // Other
    private final NumberSetting delay = new NumberSetting("Delay", "The delay between switching items", 0, 0, 100, 1);
    private final BooleanSetting inventorySpoof = new BooleanSetting("Inventory Spoof", "Fake opening your inventory", true);
    private final BooleanSetting cancelMotion = new BooleanSetting("Cancel Motion", "Cancel the motion of the player when switching items", false);

    // Switch timer
    private final Timer timer = new Timer();

    public Offhand() {
        super("Offhand", ModuleCategory.COMBAT, "Manages the item in your offhand");
        this.addSettings(primary, secondary, gappleSword, safety, delay, inventorySpoof, cancelMotion);
    }

    @Override
    public void onTick() {
        // In game & not in a gui screen check
        if (nullCheck() || mc.currentScreen != null) {
            return;
        }

        // Delay timer has passed
        if (timer.hasMSPassed((long) delay.getValue())) {
            // Get switch slot
            int switchItemSlot = InventoryUtil.getItemSlot(getSwitchItem());

            if (switchItemSlot != -1) {
                // Fake opening inventory
                if (inventorySpoof.isEnabled()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY));
                }

                // Cancel motion
                if (cancelMotion.isEnabled()) {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                    mc.player.setVelocity(0, mc.player.motionY, 0);
                }

                // Switch to the item
                InventoryUtil.swapOffhand(switchItemSlot);

                // Close inventory
                if (inventorySpoof.isEnabled()) {
                    mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.openContainer.windowId));
                }

                // Reset timer
                timer.reset();
            }
        }
    }

    /**
     * Gets the item we want to switch to
     * @return The item we want to switch to
     */
    public Item getSwitchItem() {
        // Check safety is enabled
        if (safety.isEnabled()) {
            // Check safety conditions
            if (elytra.isEnabled() && mc.player.isElytraFlying() || falling.isEnabled() && mc.player.fallDistance > 3 || health.isEnabled() && EntityUtil.getEntityHealth(mc.player) <= healthValue.getValue() || lava.isEnabled() && mc.player.isInLava() || fire.isEnabled() && mc.player.isBurning()) {
                return Items.TOTEM_OF_UNDYING;
            }
        }

        // Apply gapple sword
        if (gappleSword.isEnabled() && mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD) {
            return Items.GOLDEN_APPLE;
        }

        // Return primary item
        if (InventoryUtil.getItemSlot(primary.getCurrentMode().getItem()) != -1) {
            return primary.getCurrentMode().getItem();
        }
        // Return secondary item
        else {
            return secondary.getCurrentMode().getItem();
        }
    }

    @Override
    public String getArrayListInfo() {
        return " " + EnumFormatter.getFormattedText(primary.getCurrentMode()) + ", " + InventoryUtil.getCountOfItem(primary.getCurrentMode().getItem(), false, true);
    }

    public enum ItemMode {
        /**
         * Switch to end crystals
         */
        CRYSTAL(Items.END_CRYSTAL),

        /**
         * Switch to totems
         */
        TOTEM(Items.TOTEM_OF_UNDYING),

        /**
         * Switch to golden apples
         */
        GAPPLE(Items.GOLDEN_APPLE);

        // The item
        private final Item item;

        ItemMode(Item item) {
            this.item = item;
        }

        /**
         * Gets the item
         * @return The item
         */
        public Item getItem() {
            return item;
        }
    }

}
