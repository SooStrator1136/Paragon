package com.paragon.client.systems.module.impl.misc;

import com.paragon.Paragon;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.client.managers.CommandManager;
import com.paragon.client.managers.social.Player;
import com.paragon.client.managers.social.Relationship;
import com.paragon.client.managers.social.SocialManager;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

/**
 * @author Wolfsurge
 */
public class MiddleClick extends Module {

    private final BooleanSetting friend = new BooleanSetting("Friend", "Add a friend when you middle click on an entity", true);
    private final BooleanSetting pearl = new BooleanSetting("Pearl", "Throw an ender pearl when you do not middle click on an entity", true);

    public MiddleClick() {
        super("MiddleClick", ModuleCategory.MISC, "Allows you to perform actions when you middle click");
        this.addSettings(friend, pearl);
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (nullCheck()) {
            return;
        }

        // Check that middle click button is pressed
        if (Mouse.isButtonDown(2)) {
            // If the type of hit is a player
            if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY && mc.objectMouseOver.entityHit instanceof EntityPlayer && friend.isEnabled()) {
                // Create new player object
                Player player = new Player(mc.objectMouseOver.entityHit.getName(), Relationship.FRIEND);

                if (Paragon.INSTANCE.getSocialManager().isFriend(player.getName())) {
                    // Remove player from social list
                    Paragon.INSTANCE.getSocialManager().removePlayer(player.getName());
                    CommandManager.sendClientMessage(TextFormatting.RED + "Removed player " + TextFormatting.GRAY + player.getName() + TextFormatting.RED + " from your socials list!", false);
                } else {
                    // Add player to social list
                    Paragon.INSTANCE.getSocialManager().addPlayer(player);
                    CommandManager.sendClientMessage(TextFormatting.GREEN + "Added player " + TextFormatting.GRAY + player.getName() + TextFormatting.GREEN + " to your friends list!", false);
                }
            } else if (pearl.isEnabled())  {
                // The last slot we were on
                int prevSlot = mc.player.inventory.currentItem;

                // Switch to pearl, if we can
                if (InventoryUtil.switchToItem(Items.ENDER_PEARL, false)) {
                    // Throw pearl
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                    // Switch back to old slot
                    InventoryUtil.switchToSlot(prevSlot, false);
                }
            }
        }
    }
}
