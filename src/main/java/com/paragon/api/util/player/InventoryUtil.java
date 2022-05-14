package com.paragon.api.util.player;

import com.paragon.api.util.Wrapper;
import com.paragon.asm.mixins.accessor.IPlayerControllerMP;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.*;
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
        for (int i = 9; i < 36; i++) {
            Item itemInInv = mc.player.inventory.getStackInSlot(i).getItem();
            if (itemInInv == itemIn) {
                return i;
            }
        }

        return -1;
    }

    public static int getItemInHotbar(Item itemIn) {
        for (int i = 0; i < 9; i++) {
            Item itemInInv = mc.player.inventory.getStackInSlot(i).getItem();

            if (itemInInv == itemIn) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Switches to an item in the player's hotbar
     *
     * @param itemIn The item to switch to
     * @param silent Switch silently - use packets instead
     * @return Whether the switch was successful
     */
    public static boolean switchToItem(Item itemIn, boolean silent) {
        if (getItemInHotbar(itemIn) == -1) {
            return false;
        }

        if (silent) {
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

    public static int getCountOfItem(Item item, boolean hotbarOnly, boolean ignoreHotbar) {
        int count = 0;

        for (int i = (ignoreHotbar ? 9 : 0); i < (hotbarOnly ? 9 : 36); i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }

        return count;
    }

    public static boolean isHoldingSword() {
        return mc.player.getHeldItemMainhand().getItem() instanceof ItemSword;
    }

    public static int getHotbarBlockSlot(Block block) {
        int slot = -1;

        for (int i = 0; i < 9; i++) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();

            if (item instanceof ItemBlock && ((ItemBlock) item).getBlock().equals(block)) {
                slot = i;

                break;
            }
        }

        return slot;
    }
}
