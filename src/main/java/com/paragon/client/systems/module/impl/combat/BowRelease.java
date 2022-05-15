package com.paragon.client.systems.module.impl.combat;

import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * @author Wolfsurge
 */
public class BowRelease extends Module {

    private final Setting<Float> releasePower = new Setting<>("Release Power", 3.1f, 0.1f, 4.0f, 0.1f);

    public BowRelease() {
        super("BowRelease", Category.COMBAT, "Automatically releases your bow when at max charge");
        this.addSettings(releasePower);
    }

    @Override
    public void onTick() {
        if (nullCheck() || mc.player.getHeldItemMainhand().getItem() != Items.BOW) {
            return;
        }

        if (mc.player.getItemInUseCount() <= 4) {
            return;
        }

        // Get the charge power (awesome logic from trajectories!)
        float power = ((((72000 - mc.player.getItemInUseCount()) / 20.0F) * ((72000 - mc.player.getItemInUseCount()) / 20.0F) + ((72000 - mc.player.getItemInUseCount()) / 20.0F) * 2.0F) / 3.0F) * 3;

        // 3.1 seems to be the power for a critical shot
        if (power >= releasePower.getValue()) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
            mc.player.stopActiveHand();
        }
    }
}
