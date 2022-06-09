package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

/**
 * @author Wolfsurge
 */
public class Tracers extends Module {

    public static Tracers INSTANCE;

    public static Setting<Boolean> passive = new Setting<>("Passives", true)
            .setDescription("Draws lines to passive entities");

    public static Setting<Color> passiveColour = new Setting<>("Colour", new Color(0, 255, 0, 180))
            .setDescription("The colour to render the passive tracers in")
            .setParentSetting(passive);

    public static Setting<Boolean> mobs = new Setting<>("Mobs", true)
            .setDescription("Draws lines to monsters");

    public static Setting<Color> mobColour = new Setting<>("Colour", new Color(255, 0, 0, 180))
            .setDescription("The colour to render the mob tracers in")
            .setParentSetting(mobs);

    public static Setting<Boolean> players = new Setting<>("Players", true)
            .setDescription("Draws lines to players");

    public static Setting<Color> playerColour = new Setting<>("Colour", new Color(255, 255, 255, 180))
            .setDescription("The colour to render the player tracers in")
            .setParentSetting(players);

    public static Setting<Boolean> crystals = new Setting<>("Crystals", true)
            .setDescription("Draws lines to ender crystals");

    public static Setting<Color> crystalColour = new Setting<>("Colour", new Color(200, 0, 200, 180))
            .setDescription("The colour to render the ender crystal tracers in")
            .setParentSetting(crystals);

    public static Setting<Float> lineWidth = new Setting<>("LineWidth", 0.5f, 0.1f, 2f, 0.1f)
            .setDescription("How thick to render the lines");

    public Tracers() {
        super("Tracers", Category.RENDER, "Draws lines to entities in the world");

        INSTANCE = this;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        mc.world.loadedEntityList.forEach(entity -> {
            if (EntityUtil.isEntityAllowed(entity, players.getValue(), mobs.getValue(), passive.getValue()) && entity != mc.player || entity instanceof EntityEnderCrystal && crystals.getValue()) {
                RenderUtil.drawTracer(entity, lineWidth.getValue(), getColourByEntity(entity));
            }
        });
    }

    /**
     * Gets the entity's colour
     *
     * @param entityIn The entity
     * @return The entity's colour
     */
    private Color getColourByEntity(Entity entityIn) {
        if (EntityUtil.isPassive(entityIn)) {
            return passiveColour.getValue();
        }

        if (EntityUtil.isMonster(entityIn)) {
            return mobColour.getValue();
        }

        if (entityIn instanceof EntityPlayer) {
            return playerColour.getValue();
        }

        if (entityIn instanceof EntityEnderCrystal) {
            return crystalColour.getValue();
        }

        return passiveColour.getValue();
    }

}
