package com.paragon.client.systems.module.impl.misc;

import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Setting;
import com.paragon.api.util.render.RenderUtil;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.item.ItemChorusFruit;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author GentlemanMC, SooStrator1136
 */
public final class ChorusControl extends Module {

    private final Setting<Boolean> cPacketPlayer = new Setting<>("CPacketPlayer", true, null, null, null);
    private final Setting<Boolean> packetPlayerPosLook = new Setting<>("SPacketPlayerPosLook", true, null, null, null);

    private final Collection<CPacketPlayer> packets = new ArrayList<>(10);
    private final Collection<CPacketConfirmTeleport> teleportPackets = new ArrayList<>(2);

    private Vec3d renderPos = null; //Idk where to set this, your turn gentle

    private boolean ate = false;

    public ChorusControl() {
        super("ChorusControl", Category.MISC, "Cancels packets to let you not teleport");
    }

    @Override
    public void onDisable() {
        if (mc.getConnection() != null) {
            this.packets.forEach(mc.getConnection()::sendPacket);
            this.teleportPackets.forEach(mc.getConnection()::sendPacket);
        }

        this.packets.clear();
        this.teleportPackets.clear();
        this.ate = false;
    }

    @Override
    public void onRender3D() {
        if (this.renderPos == null) {
            return;
        }

        RenderUtil.drawNametagText("Player Chorus", this.renderPos, -1);
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItem) {
            if ((((CPacketPlayerTryUseItem) event.getPacket()).getHand() == EnumHand.OFF_HAND ? mc.player.getHeldItemOffhand() : mc.player.getHeldItemMainhand()).getItem() instanceof ItemChorusFruit) {
                this.ate = true;
            }
        }
        if (this.ate) {
            if (event.getPacket() instanceof SPacketPlayerPosLook && this.packetPlayerPosLook.getValue()) {
                event.cancel();
            }

            if (event.getPacket() instanceof CPacketPlayer && this.cPacketPlayer.getValue()) {
                this.packets.add(((CPacketPlayer) event.getPacket()));
                event.cancel();
            }

            if (event.getPacket() instanceof CPacketConfirmTeleport) {
                this.teleportPackets.add(((CPacketConfirmTeleport) event.getPacket()));
                event.cancel();
            }
        }
    }

}
