package com.paragon.api.util.player

import com.paragon.api.util.Wrapper
import com.paragon.mixins.accessor.IPlayerControllerMP
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.util.EnumHand

object InventoryUtil : Wrapper {
    
    fun isHolding(item: Item): Boolean {
        return minecraft.player.heldItemMainhand.item == item || minecraft.player.heldItemOffhand.item == item
    }

    fun isHolding(item: Item, hand: EnumHand?): Boolean {
        return minecraft.player.getHeldItem(hand).item == item
    }

    fun getHandHolding(item: Item): EnumHand? {
        if (minecraft.player.heldItemMainhand.item === item) {
            return EnumHand.MAIN_HAND
        }
        else if (minecraft.player.heldItemOffhand.item === item) {
            return EnumHand.OFF_HAND
        }
        return null
    }

    @JvmStatic
    fun getItemSlot(itemIn: Item): Int {
        for (i in 9..35) {
            val itemInInv = minecraft.player.inventory.getStackInSlot(i).item
            if (itemInInv === itemIn) {
                return i
            }
        }
        return -1
    }

    @JvmStatic
    fun getItemInHotbar(itemIn: Item): Int {
        for (i in 0..8) {
            if (minecraft.player.inventory.getStackInSlot(i).item === itemIn) {
                return i
            }
        }
        return -1
    }

    /**
     * Switches to an item in the player's hotbar
     *
     * @param itemIn The item to switch to
     * @param silent Switch silently - use packets instead
     * @return Whether the switch was successful
     */
    fun switchToItem(itemIn: Item, silent: Boolean): Boolean {
        if (getItemInHotbar(itemIn) == -1) {
            return false
        }
        if (silent) {
            minecraft.connection!!.sendPacket(CPacketHeldItemChange(getItemInHotbar(itemIn)))
        }
        else {
            minecraft.player.inventory.currentItem = getItemInHotbar(itemIn)
        }
        return true
    }

    @JvmStatic
    fun switchToSlot(slot: Int, silent: Boolean) {
        if (silent) {
            minecraft.player.connection.sendPacket(CPacketHeldItemChange(slot))
            // Sync item
            (minecraft.playerController as IPlayerControllerMP).hookSyncCurrentPlayItem()
        }
        else {
            minecraft.player.inventory.currentItem = slot
        }
    }

    fun getCountOfItem(item: Item, hotbarOnly: Boolean, ignoreHotbar: Boolean): Int {
        var count = 0
        for (i in (if (ignoreHotbar) 9 else 0) until if (hotbarOnly) 9 else 36) {
            val stack = minecraft.player.inventory.getStackInSlot(i)
            if (stack.item === item) {
                count += stack.count
            }
        }
        return count
    }

    val isHoldingSword: Boolean
        get() = minecraft.player.heldItemMainhand.item is ItemSword

    @JvmStatic
    fun getHotbarBlockSlot(block: Block): Int {
        var slot = -1
        for (i in 0..8) {
            val item = minecraft.player.inventory.getStackInSlot(i).item
            if (item is ItemBlock && item.block == block) {
                slot = i
                break
            }
        }
        return slot
    }
}