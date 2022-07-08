package com.paragon.client.systems.module.hud.impl;

import com.paragon.asm.mixins.accessor.IMinecraft;
import com.paragon.asm.mixins.accessor.ITimer;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.impl.client.Colours;
import com.paragon.api.setting.Setting;
import net.minecraft.util.text.TextFormatting;

import java.util.function.Function;

/**
 * @author Wolfsurge
 */
public class Speed extends HUDModule {

    public static Speed INSTANCE;

    public static Setting<Unit> unit = new Setting<>("Unit", Unit.BPS)
            .setDescription("The unit to display the speed in");

    public Speed() {
        super("Speed", "Displays your current speed");

        INSTANCE = this;
    }

    @Override
    public void render() {
        double distX = mc.player.posX - mc.player.lastTickPosX;
        double distZ = mc.player.posZ - mc.player.lastTickPosZ;

        renderText("Speed " + TextFormatting.WHITE + String.format("%.2f", unit.getValue().apply(getPlayerSpeed(distX, distZ))) + unit.getValue().name().toLowerCase(), getX(), getY(), Colours.mainColour.getValue().getRGB());
    }

    public double getPlayerSpeed(double distX, double distZ) {
        double tps = 1000 / ((ITimer) ((IMinecraft) mc).getTimer()).getTickLength();

        return Math.hypot(distX, distZ) * tps;
    }

    @Override
    public float getWidth() {
        double distX = mc.player.posX - mc.player.lastTickPosX;
        double distZ = mc.player.posZ - mc.player.lastTickPosZ;

        return getStringWidth("Speed " +  String.format("%.2f", unit.getValue().apply(getPlayerSpeed(distX, distZ))) + unit.getValue().name().toLowerCase());
    }

    @Override
    public float getHeight() {
        return getFontHeight();
    }

    public enum Unit {
        /**
         * Speed in blocks per second
         */
        BPS((value) -> value),

        /**
         * Speed in kilometers (1000 blocks) per hour
         */
        KMH((value) -> value * 3.6),

        /**
         * Speed in miles (1.60934 km) per hour
         */
        MPH((value) -> value * 2.237);

        private final Function<Double, Double> algorithm;

        Unit(Function<Double, Double> algorithm) {
            this.algorithm = algorithm;
        }

        public double apply(double value) {
            return algorithm.apply(value);
        }
    }
}
