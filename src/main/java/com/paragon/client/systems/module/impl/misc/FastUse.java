package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.asm.mixins.accessor.IMinecraft;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;

/**
 * @author Wolfsurge
 */
public class FastUse extends Module {

    // Options
    private final Setting<Boolean> xp = new Setting<>("XP Bottles", true)
            .setDescription("Fast use XP bottles");

    private final Setting<Boolean> rotate = new Setting<>("Rotate", true)
            .setDescription("Rotate your player when using XP bottles")
            .setParentSetting(xp);

    private final Setting<Boolean> crystals = new Setting<>("Crystals", true)
            .setDescription("Place crystals fast");

    private final Setting<Boolean> blocks = new Setting<>("Blocks", false)
            .setDescription("Place blocks fast");

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
        if (xp.getValue() && InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) || blocks.getValue() && (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock || mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock) || crystals.getValue() && InventoryUtil.isHolding(Items.END_CRYSTAL)) {
            ((IMinecraft) mc).setRightClickDelayTimer(0);
        }
    }

    @Listener
    public void onPacketSend(PacketEvent.PreSend event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItem && InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) && xp.getValue() && rotate.getValue()) {
            // Send rotation packet. We aren't using the rotation manager as it doesn't immediately rotate the player
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, 90f, mc.player.onGround));
        }
    }
}
