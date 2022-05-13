package com.paragon.client.systems.module.impl.combat;

import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * @author Wolfsurge
 */
public class Replenish extends Module {

    // General
    private final Setting<Float> percent = new Setting<>("Percent", 50f, 1f, 100f, 1f)
            .setDescription("The point at which to refill");

    private final Setting<Boolean> inventorySpoof = new Setting<>("Inventory Spoof", true)
            .setDescription("Spoof opening your inventory");

    public Replenish() {
        super("Replenish", Category.COMBAT, "Automatically refills the item you are holding");
        this.addSettings(percent);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        // Loop through hotbar items
        for (int i = 0; i < 9; i++) {
            // Get the stack
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            // If the stack is empty, continue
            if (stack.isEmpty()) {
                continue;
            }

            // Get percentage of item in the slot
            double stackPercent = ((double) stack.getCount() / (double) stack.getMaxStackSize()) * 100.0;

            // Check if the item is below the threshold
            if (stackPercent <= percent.getValue().intValue()) {
                mergeStack(i, stack);
            }
        }
    }

    public void mergeStack(int current, ItemStack stack) {
        int replaceSlot = -1;

        // Loop through items in inventory
        for (int i = 9; i < 36; i++) {
            // Get the stack
            ItemStack inventoryStack = mc.player.inventory.getStackInSlot(i);

            // If the stack is empty, continue
            if (inventoryStack.isEmpty()) {
                continue;
            }

            // The name needs to be the same as the current stack's name, otherwise they can't be merged
            if (!inventoryStack.getDisplayName().equals(stack.getDisplayName())) {
                continue;
            }

            // We want to merge blocks
            if (stack.getItem() instanceof ItemBlock && inventoryStack.getItem() instanceof ItemBlock) {
                // Check the blocks are the same
                if (!((ItemBlock) stack.getItem()).getBlock().equals(((ItemBlock) inventoryStack.getItem()).getBlock())) {
                    continue;
                }
            } else {
                // Check the items are the same
                if (!stack.getItem().equals(inventoryStack.getItem())) {
                    continue;
                }
            }

            // Set replace slot
            replaceSlot = i;
            break;
        }

        if (replaceSlot != -1) {
            // Merge stacks
            mc.playerController.windowClick(0, replaceSlot, 0, ClickType.PICKUP, mc.player);

            mc.playerController.windowClick(0, current < 9 ? current + 36 : current, 0, ClickType.PICKUP, mc.player);

            mc.playerController.windowClick(0, replaceSlot, 0, ClickType.PICKUP, mc.player);
        }
    }
}