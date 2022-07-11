package com.paragon.client.systems.module.impl.combat;

import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.string.StringUtil;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.Vec3d;

import java.util.stream.Collectors;

/**
 * @author Wolfsurge
 */
public class Offhand extends Module {

    public static Offhand INSTANCE;

    public static Setting<Timing> timing = new Setting<>("Timing", Timing.LINEAR)
            .setDescription("The timing to use before switching");

    // Swap settings
    public static Setting<ItemMode> priority = new Setting<>("Priority", ItemMode.CRYSTAL)
            .setDescription("The item we most want to use in the offhand");

    public static Setting<ItemMode> secondary = new Setting<>("Secondary", ItemMode.TOTEM)
            .setDescription("The item we want to use if we cannot find the main item");

    public static Setting<Boolean> gappleSword = new Setting<>("GappleSword", true)
            .setDescription("Swap to a gapple when wielding a sword");

    // Safety settings
    public static Setting<Boolean> safety = new Setting<>("Safety", true)
            .setDescription("Switch to a totem in certain scenarios");

    public static Setting<Boolean> elytra = new Setting<>("Elytra", true)
            .setDescription("Switch to a totem when elytra flying")
            .setParentSetting(safety);

    public static Setting<Boolean> falling = new Setting<>("Falling", true)
            .setDescription("Switch to a totem when you will take fall damage")
            .setParentSetting(safety);

    public static Setting<Boolean> crystal = new Setting<>("Crystal", true)
            .setDescription("Switch to a totem when you can die from a crystal")
            .setParentSetting(safety);

    public static Setting<Boolean> health = new Setting<>("Health", true)
            .setDescription("Switch to a totem when you are below a value")
            .setParentSetting(safety);

    public static Setting<Float> healthValue = new Setting<>("HealthValue", 10f, 0f, 20f, 1f)
            .setDescription("The value we want to switch to a totem when you are below")
            .setParentSetting(safety)
            .setVisibility(health::getValue);

    public static Setting<Boolean> lava = new Setting<>("Lava", true)
            .setDescription("Switch to a totem when you are in lava")
            .setParentSetting(safety);

    public static Setting<Boolean> fire = new Setting<>("Fire", false)
            .setDescription("Switch to a totem when you are on fire")
            .setParentSetting(safety);

    // Delay
    public static Setting<Double> delay = new Setting<>("Delay", 0D, 0D, 200D, 1D)
            .setDescription("The delay between switching items");

    // Bypass settings
    public static Setting<Boolean> inventorySpoof = new Setting<>("InventorySpoof", true)
            .setDescription("Spoof opening your inventory");

    public static Setting<Boolean> cancelMotion = new Setting<>("CancelMotion", false)
            .setDescription("Cancel the motion of the player when switching items");

    // The timer to determine when to switch
    private final Timer switchTimer = new Timer();

    // Sequential state
    private State state = State.IDLE;

    public Offhand() {
        super("Offhand", Category.COMBAT, "Manages the item in your offhand");

        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nullCheck() || mc.currentScreen instanceof GuiContainer || mc.player.isHandActive()) {
            return;
        }

        // Get slot to switch to
        int switchItemSlot = getSwapSlot();

        // Check we have a slot to switch
        if (switchItemSlot != -1) {
            // Spoof inventory
            if (state.equals(State.IDLE)) {
                state = State.SWAP;

                if (inventorySpoof.getValue() && mc.getConnection() != null) {
                    mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY));

                    if (timing.getValue().equals(Timing.SEQUENTIAL)) {
                        return;
                    }
                }
            }

            // Cancel motion
            if (cancelMotion.getValue()) {
                mc.player.motionX = 0;
                mc.player.motionZ = 0;
                mc.player.setVelocity(0, mc.player.motionY, 0);
            }

            // Switch items
            if (state.equals(State.SWAP)) {
                // Check time has passed
                if (switchTimer.hasMSPassed(delay.getValue() * 10) || timing.getValue().equals(Timing.SEQUENTIAL)) {
                    swapOffhand(switchItemSlot);
                    switchTimer.reset();
                }

                state = State.CLOSE_INVENTORY;

                if (timing.getValue().equals(Timing.SEQUENTIAL)) {
                    return;
                }
            }
        }

        // Close inventory
        if (state.equals(State.CLOSE_INVENTORY)) {
            if (inventorySpoof.getValue() && mc.getConnection() != null && mc.player.inventoryContainer != null) {
                mc.getConnection().sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
            }

            state = State.FINISHED;


            return;
        }

        if (state.equals(State.FINISHED)) {
            state = State.IDLE;
        }
    }

    /**
     * Swaps the offhand item
     *
     * @param slot The slot to switch to
     */
    public void swapOffhand(int slot) {
        // Click the slots
        mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

        // Get return slot
        int returnSlot = -1;
        if (mc.player.inventory.getStackInSlot(slot).isEmpty()) {
            returnSlot = slot;
        } else {
            for (int i = 9; i <= 44; i++) {
                if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                    returnSlot = i;
                    break;
                }
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
     *
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
            if (elytra.getValue() && mc.player.isElytraFlying() || falling.getValue() && mc.player.fallDistance > 3 || health.getValue() && mc.player.getHealth() + mc.player.getAbsorptionAmount() <= healthValue.getValue() || lava.getValue() && mc.player.isInLava() || fire.getValue() && mc.player.isBurning()) {
                swap = Items.TOTEM_OF_UNDYING;
            }

            if (crystal.getValue()) {
                // Check for crystal
                for (Entity crystal : mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityEnderCrystal && entity.getDistance(mc.player) <= 6).collect(Collectors.toList())) {
                    // Crystal does too much damage for us to take
                    if (AutoCrystal.INSTANCE.calculateDamage(new Vec3d(crystal.posX, crystal.posY, crystal.posZ), mc.player) >= mc.player.getHealth()) {
                        swap = Items.TOTEM_OF_UNDYING;
                    }
                }
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
            if (mc.player.getHeldItemOffhand().getItem() == secondary.getValue().getItem()) {
                return -1;
            }

            return InventoryUtil.getItemSlot(secondary.getValue().getItem());
        }

        // Return slot
        return swapSlot;
    }

    @Override
    public String getData() {
        return " " + StringUtil.getFormattedText(priority.getValue()) + ", " + InventoryUtil.getCountOfItem(secondary.getValue().getItem(), false, true) + (timing.getValue().equals(Timing.SEQUENTIAL) ? ", " + StringUtil.getFormattedText(state) : "");
    }

    public enum Timing {
        /**
         * Perform all actions on one tick
         */
        LINEAR,

        /**
         * Spread actions through multiple ticks
         */
        SEQUENTIAL
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
         *
         * @return The item we want to switch to
         */
        public Item getItem() {
            return item;
        }
    }

    public enum State {
        /**
         * Not performing an action
         */
        IDLE,

        /**
         * Swap item
         */
        SWAP,

        /**
         * Close inventory
         */
        CLOSE_INVENTORY,

        /**
         * Finished swapping
         */
        FINISHED
    }
}