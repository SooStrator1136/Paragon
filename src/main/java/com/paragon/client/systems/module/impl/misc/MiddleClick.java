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
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

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

        if (Mouse.isButtonDown(2)) {
            if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY && friend.isEnabled()) {
                Player player = new Player(mc.objectMouseOver.entityHit.getName(), Relationship.FRIEND);

                if (Paragon.INSTANCE.getSocialManager().isFriend(player.getName())) {
                    Paragon.INSTANCE.getSocialManager().removePlayer(player.getName());
                    CommandManager.sendClientMessage(TextFormatting.RED + "Removed player " + TextFormatting.GRAY + player.getName() + TextFormatting.RED + " from your socials list!", false);
                } else {
                    Paragon.INSTANCE.getSocialManager().addPlayer(player);
                    CommandManager.sendClientMessage(TextFormatting.GREEN + "Added player " + TextFormatting.GRAY + player.getName() + TextFormatting.GREEN + " to your friends list!", false);
                }
            } else if (pearl.isEnabled())  {
                int prevSlot = mc.player.inventory.currentItem;
                if (InventoryUtil.switchToItem(Items.ENDER_PEARL, false)) {
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                    InventoryUtil.switchToSlot(prevSlot, false);
                }
            }
        }
    }
}
