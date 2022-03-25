package com.paragon.api.util.player;

import com.paragon.api.util.Wrapper;
import com.paragon.asm.mixins.accessor.IPlayerControllerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;

public class InventoryUtil implements Wrapper {

    public static boolean isHolding(Item item) {
        return mc.player.getHeldItemMainhand().getItem().equals(item) || mc.player.getHeldItemOffhand().getItem().equals(item);
    }

    public static EnumHand getHandHolding(Item item) {
        if (mc.player.getHeldItemMainhand().getItem() == item) {
            return EnumHand.MAIN_HAND;
        } else if (mc.player.getHeldItemOffhand().getItem() == item) {
            return EnumHand.OFF_HAND;
        }

        return null;
    }

    public static int getItemSlot(Item itemIn) {
        for (int i = 0; i < 36; i++) {
            Item itemInInv = mc.player.inventory.getStackInSlot(i).getItem();
            if (itemInInv == itemIn) {
                return i;
            }
        }

        return -1;
    }

    public static int getItemInHotbar(Item itemIn) {
        for(int i = 0; i < 9; i++) {
            Item itemInInv = mc.player.inventory.getStackInSlot(i).getItem();

            if(itemInInv == itemIn)
                return i;
        }

        return -1;
    }

    /**
     * Switches to an item in the player's hotbar
     * @param itemIn The item to switch to
     * @param silent Switch silently - use packets instead
     * @return Whether the switch was successful
     */
    public static boolean switchToItem(Item itemIn, boolean silent) {
        if(getItemInHotbar(itemIn) == -1) {
            return false;
        }

        if(silent) {
            mc.getConnection().sendPacket(new CPacketHeldItemChange(getItemInHotbar(itemIn)));
        } else {
            mc.player.inventory.currentItem = getItemInHotbar(itemIn);
        }

        return true;
    }

    public static void switchToSlot(int slot, boolean silent) {
        if (silent) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            // Sync item
            ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();
        } else {
            mc.player.inventory.currentItem = slot;
        }
    }

    public static int getCountOfItem(Item item, boolean hotbarOnly) {
        int count = 0;

        for (int i = 0; i < (hotbarOnly ? 9 : 36); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }

        return count;
    }

    public static void swapOffhand(int slot) {
        mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);

        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);

        int returnSlot = -1;
        for (int i = 9; i <= 44; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                returnSlot = i;
                break;
            }
        }

        if (returnSlot != -1) {
            mc.playerController.windowClick(0, returnSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.updateController();
        }
    }

    public static boolean isHoldingSword() {
        Item heldItem = mc.player.getHeldItemMainhand().getItem();

        return heldItem == Items.WOODEN_SWORD || heldItem == Items.STONE_SWORD || heldItem == Items.IRON_SWORD || heldItem == Items.GOLDEN_SWORD || heldItem == Items.DIAMOND_SWORD;
    }
}
