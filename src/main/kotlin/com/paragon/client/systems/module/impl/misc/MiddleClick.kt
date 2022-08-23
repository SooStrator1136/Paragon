package com.paragon.client.systems.module.impl.misc

import com.paragon.Paragon
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import com.paragon.api.util.player.InventoryUtil
import com.paragon.client.managers.social.Player
import com.paragon.client.managers.social.Relationship
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.util.EnumHand
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Mouse

/**
 * @author Surge
 */
object MiddleClick : Module("MiddleClick", Category.MISC, "Allows you to perform actions when you middle click") {

    private val friend = Setting(
        "Friend",
        true
    ) describedBy "Add a friend when you middle click on an player"
    private val pearl = Setting(
        "Pearl",
        true
    ) describedBy "Throw an ender pearl when you miss an entity"

    // To prevent excessive spam
    private var hasClicked = false

    @SubscribeEvent
    fun onMouseInput(event: InputEvent.MouseInputEvent?) {
        if (minecraft.anyNull) {
            return
        }

        // Check that middle click button is pressed, and we haven't just clicked
        if (Mouse.isButtonDown(2)) {
            val result = minecraft.objectMouseOver.typeOfHit
            if (!hasClicked) {
                // If the type of hit is a player
                if (result == RayTraceResult.Type.ENTITY && minecraft.objectMouseOver.entityHit is EntityPlayer && friend.value) {
                    // Create new player object
                    val player = Player(minecraft.objectMouseOver.entityHit.name, Relationship.FRIEND)

                    if (Paragon.INSTANCE.socialManager.isFriend(player.name)) {
                        // Remove player from social list
                        Paragon.INSTANCE.socialManager.removePlayer(player.name)
                        Paragon.INSTANCE.commandManager.sendClientMessage(
                            TextFormatting.RED.toString() + "Removed player " + TextFormatting.GRAY + player.name + TextFormatting.RED + " from your socials list!",
                            false
                        )
                    } else {
                        // Add player to social list
                        Paragon.INSTANCE.socialManager.addPlayer(player)
                        Paragon.INSTANCE.commandManager.sendClientMessage(
                            TextFormatting.GREEN.toString() + "Added player " + TextFormatting.GRAY + player.name + TextFormatting.GREEN + " to your friends list!",
                            false
                        )
                    }
                } else if (pearl.value) {
                    // The last slot we were on
                    val prevSlot = minecraft.player.inventory.currentItem

                    // Switch to pearl, if we can
                    if (InventoryUtil.switchToItem(Items.ENDER_PEARL, false)) {
                        // Throw pearl
                        minecraft.playerController.processRightClick(
                            minecraft.player,
                            minecraft.world,
                            EnumHand.MAIN_HAND
                        )

                        // Switch back to old slot
                        InventoryUtil.switchToSlot(prevSlot, false)
                    }
                }
            }

            // We have clicked
            hasClicked = true
        } else {
            // Reset hasClicked
            hasClicked = false
        }
    }

}