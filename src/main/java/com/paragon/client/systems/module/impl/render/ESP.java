package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.ShaderColourEvent;
import com.paragon.api.event.render.entity.RenderEntityEvent;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.render.OutlineUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.asm.mixins.accessor.IRenderGlobal;
import com.paragon.asm.mixins.accessor.IShaderGroup;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.*;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.List;

/**
 * @author Wolfsurge
 */
public class ESP extends Module {

    /* Entity settings */
    private final BooleanSetting passive = new BooleanSetting("Passives", "Highlight passive entities", true);
    private final ColourSetting passiveColour = (ColourSetting) new ColourSetting("Colour", "The colour to highlight passive entities in", Color.GREEN).setParentSetting(passive);

    private final BooleanSetting mobs = new BooleanSetting("Mobs", "Highlight mobs", true);
    private final ColourSetting mobColour = (ColourSetting) new ColourSetting("Colour", "The colour to highlight mobs in", Color.RED).setParentSetting(mobs);

    private final BooleanSetting players = new BooleanSetting("Players", "Highlight player entities", true);
    private final ColourSetting playerColour = (ColourSetting) new ColourSetting("Colour", "The colour to highlight player entities in", new Color(255, 255, 255)).setParentSetting(players);

    private final BooleanSetting items = new BooleanSetting("Items", "Highlight items", true);
    private final ColourSetting itemColour = (ColourSetting) new ColourSetting("Colour", "The colour to highlight items in", new Color(185, 17, 255)).setParentSetting(items);

    private final BooleanSetting crystals = new BooleanSetting("Crystals", "Highlight end crystals", true);
    private final ColourSetting crystalColour = (ColourSetting) new ColourSetting("Colour", "The colour to highlight end crystals in", new Color(185, 17, 255)).setParentSetting(crystals);

    /* Mode and line width */
    private final ModeSetting<Mode> mode = new ModeSetting<>("Mode", "How to render the entities", Mode.OUTLINE);
    private final NumberSetting lineWidth = new NumberSetting("Line Width", "How thick to render the outlines", 2, 0.1f, 2, 0.1f);

    public ESP() {
        super("ESP", ModuleCategory.RENDER, "Highlights entities in the world");
        this.addSettings(passive, mobs, players, items, crystals, mode, lineWidth);
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }

        for(Entity e : mc.world.loadedEntityList) {
            e.setGlowing(false);
        }
    }

    @Listener
    public void onRenderEntity(RenderEntityEvent event) {
        if(isEntityValid(event.getEntity()) && mode.getCurrentMode() == Mode.OUTLINE) {
            OutlineUtil.renderOne(lineWidth.getValue());
            event.renderModel();
            OutlineUtil.renderTwo();
            event.renderModel();
            OutlineUtil.renderThree();
            event.renderModel();
            OutlineUtil.renderFour(getColourByEntity(event.getEntity()));
            event.renderModel();
            OutlineUtil.renderFive();
            event.renderModel();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        for(Entity e : mc.world.loadedEntityList) {
            if(isEntityValid(e)) {
                espEntity(e);
            }
        }

        // Check glow
        if (mode.getCurrentMode().equals(Mode.GLOW)) {
            // Get shaders
            List<Shader> shaders = ((IShaderGroup) ((IRenderGlobal) mc.renderGlobal).getEntityOutlineShader()).getListShaders();

            shaders.forEach(shader -> {
                // Get line width
                ShaderUniform uniform = shader.getShaderManager().getShaderUniform("Radius");

                if (uniform != null) {
                    // Set line width
                    uniform.set(lineWidth.getValue());
                }
            });
        }
    }

    @Listener
    public void onShaderColour(ShaderColourEvent event) {
        if (mode.getCurrentMode().equals(Mode.GLOW)) {
            event.setColour(getColourByEntity(event.getEntity()));
            event.cancel();
        }
    }

    /**
     * Highlights an entity
     * @param entityIn The entity to highlight
     */
    public void espEntity(Entity entityIn) {
        if (!((entityIn instanceof EntityItem) || (entityIn instanceof EntityEnderCrystal)) && mode.getCurrentMode() == Mode.OUTLINE) {
            return;
        }

        // Set it glowing if it's an item
        if (entityIn instanceof EntityItem) {
            entityIn.setGlowing(true);
            return;
        }

        if (mode.getCurrentMode() == Mode.BOX) {
            RenderUtil.drawBoundingBox(EntityUtil.getEntityBox(entityIn), lineWidth.getValue(), getColourByEntity(entityIn));
        } else if (mode.getCurrentMode() == Mode.GLOW) {
            entityIn.setGlowing(true);
        }

        if (mode.getCurrentMode() != Mode.GLOW) {
            entityIn.setGlowing(false);
        }
    }

    /**
     * Checks if an entity is valid
     * @param entityIn The entity to check
     * @return Is the entity valid
     */
    private boolean isEntityValid(Entity entityIn) {
        if(entityIn instanceof EntityLiving && !(entityIn instanceof EntityMob) && passive.isEnabled()) return true;
        else if(entityIn instanceof EntityMob && mobs.isEnabled()) return true;
        else if(entityIn instanceof EntityPlayer && entityIn != mc.player && players.isEnabled()) return true;
        else if(entityIn instanceof EntityItem && items.isEnabled()) return true;
        else return entityIn instanceof EntityEnderCrystal && crystals.isEnabled();
    }

    /**
     * Gets the entity's colour
     * @param entityIn The entity
     * @return The entity's colour
     */
    private Color getColourByEntity(Entity entityIn) {
        if(entityIn instanceof EntityLiving && !(entityIn instanceof EntityMob)) return passiveColour.getColour();
        else if(entityIn instanceof EntityMob) return mobColour.getColour();
        else if(entityIn instanceof EntityPlayer && entityIn != mc.player) return playerColour.getColour();
        else if(entityIn instanceof EntityEnderCrystal) return crystalColour.getColour();
        else if (entityIn instanceof EntityItem) return itemColour.getColour();
        else return new Color(0);
    }

    public enum Mode {
        /**
         * Outline the entity
         */
        OUTLINE,

        /**
         * Apply vanilla glow shader
         */
        GLOW,

        /**
         * Draw a box
         */
        BOX
    }
}
