package com.paragon.client.systems.module.impl.misc

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.calculations.Timer
import com.paragon.mixins.accessor.IPlayerControllerMP
import io.netty.buffer.Unpooled
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemWritableBook
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraft.util.EnumHand
import kotlin.random.Random


/**
 * @author SooStrator1136, ForgeHax
 */
object BookBot : Module("BookBot", Category.MISC, "Writes books for you") {

    private val charset = Setting(
        "Charset",
        "abc"
    ) describedBy ""

    private val pageAmount = Setting(
        "Pages",
        50F,
        1F,
        50F,
        1F
    ) describedBy "Amount of pages that will be filled"

    private val maxChars = Setting(
        "Character Amount",
        256F,
        1F,
        256F,
        1F
    ) describedBy "The maximum of characters to write on a page"

    private val bookName = Setting("BookName", "e") describedBy "The name the book will get"

    private val delay = Setting(
        "Delay",
        200.0,
        0.0,
        2500.0,
        50.0
    ) describedBy "The delay between writing books"

    private val timer = Timer()

    override fun onTick() {
        if (minecraft.anyNull || !timer.hasMSPassed(delay.value)) {
            return
        }

        val book = getBook()
        if (book.first == -1) {
            return
        }

        if (minecraft.player.inventory.currentItem != book.first) {
            minecraft.player.inventory.currentItem = book.first
            (minecraft.playerController as IPlayerControllerMP).hookSyncCurrentPlayItem()
        }

        minecraft.player.openBook(book.second, EnumHand.MAIN_HAND)

        sendBook(book.second)
        minecraft.setIngameFocus()

        timer.reset()
    }

    /**
     * Finishs and sends the book
     */
    private fun sendBook(stack: ItemStack) {
        val pages = NBTTagList()

        repeat(pageAmount.value.toInt()) {
            pages.appendTag(NBTTagString(getRandomPageContent()))
        }

        if (stack.hasTagCompound()) {
            (stack.tagCompound ?: return).setTag("pages", pages)
        } else {
            stack.setTagInfo("pages", pages)
        }

        stack.setTagInfo("author", NBTTagString(minecraft.player.name))
        stack.setTagInfo(
            "title",
            NBTTagString(bookName.value)
        )

        minecraft.connection?.sendPacket(
            CPacketCustomPayload(
                "MC|BSign",
                PacketBuffer(Unpooled.buffer()).also {
                    it.writeItemStack(stack)
                }
            )
        )
    }

    /**
     * @return a String by the length of [maxChars] with random characters picked from the [charset]
     */
    private fun getRandomPageContent(): String {
        val builder = StringBuilder()

        repeat(maxChars.value.toInt()) {
            builder.append(charset.value[Random.nextInt(charset.value.length)])
        }

        return builder.toString()
    }

    /**
     * @return the slot of an unwritten book in the hotbar
     */
    private fun getBook(): Pair<Int, ItemStack> {
        var book = Pair(-1, ItemStack.EMPTY)

        for (i in 0 until InventoryPlayer.getHotbarSize()) {
            val stack: ItemStack? = minecraft.player.inventory.getStackInSlot(i)
            if (stack != null && stack != ItemStack.EMPTY && stack.item is ItemWritableBook) {
                book = Pair(i, stack)
                break
            }
        }

        return book
    }

}