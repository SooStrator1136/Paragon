package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.event.player.StepEvent;
import com.paragon.api.util.string.StringUtil;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import com.paragon.asm.mixins.accessor.IMinecraft;
import com.paragon.asm.mixins.accessor.ITimer;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Surge, Doogie13, aesthetical
 * Uses Cosmos's step event, originally from auto which Aestheticall implemented into Cosmos with auto's permission
 */
public class Step extends Module {

    // if you prefer to not use an anonymous inner class, you can always .put in the constructor
    private static final Map<Double, double[]> NCP_STEP_OFFSETS = new HashMap<Double, double[]>() {{
        // note to developers: NCP strict has a "step patch" where it will lag you back on the top of the block once you successfully stepped
        // the bypass for this is to add an extra offset value of the step height. this will not interfere with normal NCP

        put(0.875, new double[] { 0.39, 0.7, 0.875 });
        put(1.0, new double[] { 0.42, 0.75, 1.0 });

        // you can add the rest of ur step positions here
    }};

    public static Step INSTANCE;

    // Step mode
    public static Setting<Mode> mode = new Setting<>("Mode", Mode.NCP)
            .setDescription("What mode to use");

    // Vanilla step height
    public static Setting<Float> stepHeight = new Setting<>("StepHeight", 1.5f, 0.5f, 2.5f, 0.5f)
            .setDescription("How high to step up")
            .setVisibility(() -> mode.getValue().equals(Mode.VANILLA));

    public static Setting<Boolean> useTimer = new Setting<>("UseTimer", true)
            .setDescription("If to use timer to prevent the MORE_PACKETS flag on NCP")
            .setVisibility(() -> mode.getValue().equals(Mode.NCP));

    public Step() {
        super("Step", Category.MOVEMENT, "Lets you instantly step up blocks");

        INSTANCE = this;
    }

    private boolean timer = false;

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }

        // Set step height to normal
        mc.player.stepHeight = 0.6f;

        // reset our tickLength to 50.0f (1 timer speed)
        ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength(50.0f);
        timer = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (nullCheck() ||
                // wtf
                !isEnabled()) {
            return;
        }

        // Set step height
        mc.player.stepHeight = stepHeight.getValue();
    }

    @SubscribeEvent
    public void onUpdate(LivingUpdateEvent event) {
        if (event.getEntityLiving().equals(mc.player)) {

            // if we have used timer before and we are on ground after stepping, reset our timer
            if (timer && mc.player.onGround) {
                timer = false;
                ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength(50.0f);
            }
        }
    }

    @Listener
    public void onStep(StepEvent event) {
        if (mode.getValue().equals(Mode.NCP) && event.getEntity().equals(mc.player)) {
            double height = event.getBB().minY - mc.player.posY;

            // don't step if there are any flagging conditions
            if (height > stepHeight.getValue() || !mc.player.onGround || mc.player.isInWater() || mc.player.isInLava()) {
                return;
            }

            // get our packet offsets from the map, and then a default value of null
            double[] offsets = NCP_STEP_OFFSETS.getOrDefault(height, null);
            if (offsets == null || offsets.length == 0) {
                return;
            }

            if (useTimer.getValue()) {

                // set our timer dynamically based off of the amount of offsets we are using
                ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength(50.0f / (1.0f / (offsets.length + 1.0f)));
                timer = true;
            }

            // Send offsets - this simulates a fake jump
            for (double offset : offsets) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + offset, mc.player.posZ, false));
            }
        }
    }

    @Override
    public String getData() {
        return StringUtil.getFormattedText(mode.getValue());
    }

    public enum Mode {
        /**
         * Vanilla step - bypasses almost no servers :P
         */
        VANILLA,

        /**
         * NCP step - bypasses the NCP anticheat
         *
         * Is notable that some step heights over 1.5 are not possible on Strict NCP
         */
        NCP
    }

}
