package com.paragon.impl.command.impl

import com.paragon.Paragon
import com.paragon.impl.managers.notifications.Notification
import com.paragon.impl.managers.notifications.NotificationType
import com.paragon.impl.module.hud.impl.Notifications
import io.netty.buffer.Unpooled
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer

/**
 * @author EBS
 */
object SizeCommand : com.paragon.impl.command.Command("Size", "size") {

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        val stack = Minecraft.getMinecraft().player.heldItemMainhand

        if (Notifications.isEnabled) {
            Paragon.INSTANCE.notificationManager.addNotification(
                Notification(
                    if (stack.isEmpty) "You are not holding any item"
                    else "Item weights " + getItemSize(stack).toString() + " bytes", NotificationType.INFO
                )
            )
        }
        else {
            Paragon.INSTANCE.commandManager.sendClientMessage(
                if (stack.isEmpty) "You are not holding any item"
                else "Item weights " + getItemSize(stack).toString() + " bytes", false
            )
        }
    }

    private fun getItemSize(stack: ItemStack): Int {
        val buff = PacketBuffer(Unpooled.buffer())
        buff.writeItemStack(stack)
        return buff.writerIndex().also { buff.release() }
    }

}