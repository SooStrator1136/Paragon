package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.awt.*;

public class Tracers extends Module {

    private final Setting<Boolean> passive = new Setting<>("Passives", true)
            .setDescription("Draws lines to passive entities");

    private final Setting<Color> passiveColour = new Setting<>("Passive Colour", new Color(0, 255, 0, 180))
            .setDescription("The colour to render the passive tracers in")
            .setParentSetting(passive);

    private final Setting<Boolean> mobs = new Setting<>("Mobs", true)
            .setDescription("Draws lines to monsters");

    private final Setting<Color> mobColour = new Setting<>("Mob Colour", new Color(255, 0, 0, 180))
            .setDescription("The colour to render the mob tracers in")
            .setParentSetting(mobs);

    private final Setting<Boolean> players = new Setting<>("Players", true)
            .setDescription("Draws lines to players");

    private final Setting<Color> playerColour = new Setting<>("Player Colour", new Color(255, 255, 255, 180))
            .setDescription("The colour to render the player tracers in")
            .setParentSetting(players);

    private final Setting<Boolean> crystals = new Setting<>("Crystals", true)
            .setDescription("Draws lines to ender crystals");

    private final Setting<Color> crystalColour = new Setting<>("Crystal Colour", new Color(200, 0, 200, 180))
            .setDescription("The colour to render the ender crystal tracers in")
            .setParentSetting(crystals);

    private final Setting<Float> lineWidth = new Setting<>("Line Width", 0.5f, 0.1f, 2f, 0.1f)
            .setDescription("How thick to render the lines");

    public Tracers() {
        super("Tracers", ModuleCategory.RENDER, "Draws lines to entities in the world");
        this.addSettings(passive, mobs, players, crystals, lineWidth);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        for(Entity entity : mc.world.loadedEntityList) {
            if(isEntityValid(entity))
                RenderUtil.drawTracer(entity, lineWidth.getValue(), getColourByEntity(entity));
        }
    }

    /**
     * Checks if an entity is valid
     * @param entityIn The entity to check
     * @return Is the entity valid
     */
    private boolean isEntityValid(Entity entityIn) {
        if(entityIn instanceof EntityLiving && !(entityIn instanceof EntityMob) && passive.getValue()) return true;
        else if(entityIn instanceof EntityMob && mobs.getValue()) return true;
        else if(entityIn instanceof EntityPlayer && entityIn != mc.player && players.getValue()) return true;
        else return entityIn instanceof EntityEnderCrystal && crystals.getValue();
    }

    /**
     * Gets the entity's colour
     * @param entityIn The entity
     * @return The entity's colour
     */
    private Color getColourByEntity(Entity entityIn) {
        if(entityIn instanceof EntityLiving && !(entityIn instanceof EntityMob)) return passiveColour.getValue();
        else if(entityIn instanceof EntityMob) return mobColour.getValue();
        else if(entityIn instanceof EntityPlayer && entityIn != mc.player) return playerColour.getValue();
        else if(entityIn instanceof EntityEnderCrystal) return crystalColour.getValue();
        else return new Color(0);
    }

}
