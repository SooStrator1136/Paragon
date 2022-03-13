package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;

public class FastUse extends Module {

    // Options
    private final BooleanSetting xp = new BooleanSetting("XP Bottles", "Fast use XP bottles", true);
    private final BooleanSetting rotate = (BooleanSetting) new BooleanSetting("Rotate", "Rotate your player when using XP bottles", true).setParentSetting(xp);
    private final BooleanSetting crystals = new BooleanSetting("Crystals", "Place crystals fast", true);
    private final BooleanSetting blocks = new BooleanSetting("Blocks", "Place blocks fast", false);

    public FastUse() {
        super("FastUse", ModuleCategory.MISC, "Allows you to use items quicker than you would be able to in vanilla");
        this.addSettings(xp, crystals, blocks);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        // Check we want to set the delay timer to 0
        if (xp.isEnabled() && InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) || blocks.isEnabled() && (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock || mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock) || crystals.isEnabled() && InventoryUtil.isHolding(Items.END_CRYSTAL)) {
            mc.rightClickDelayTimer = 0;
        }
    }

    @Listener
    public void onPacketSend(PacketEvent.PreSend event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItem && InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) && xp.isEnabled() && rotate.isEnabled()) {
            // Send rotation packet
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, 90f, mc.player.onGround));
        }
    }
}
