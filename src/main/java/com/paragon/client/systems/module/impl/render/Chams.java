package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.entity.RenderCrystalEvent;
import com.paragon.api.event.render.entity.RenderEntityEvent;
import com.paragon.api.util.render.RubiksCrystalUtil;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;
import com.paragon.client.systems.ui.animation.Easing;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Quaternion;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static org.lwjgl.opengl.GL11.*;

/**
 * Rubik's crystals based off of rebane2001's and olliem5's Rubiks crystals
 *
 * otherwise
 * @author Wolfsurge
 */
public class Chams extends Module {

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.WIRE_MODEL)
            .setDescription("The render mode to use");

    public static Setting<Float> width = new Setting<>("Width", 1f, 0.1f, 3f, 0.1f)
            .setDescription("The width of the lines")
            .setVisibility(() -> !mode.getValue().equals(Mode.MODEL));

    // Entity filters
    public static Setting<Boolean> players = new Setting<>("Players", true)
            .setDescription("Highlight players");

    public static Setting<Boolean> mobs = new Setting<>("Mobs", true)
            .setDescription("Highlight mobs");

    public static Setting<Boolean> passives = new Setting<>("Passives", true)
            .setDescription("Highlight passives");

    // Crystals

    public static Setting<Boolean> crystals = new Setting<>("Crystals", true)
            .setDescription("Highlight crystals");

    public static Setting<Boolean> bounce = new Setting<>("Bounce", false)
            .setDescription("Make the crystals bounce like they do in vanilla")
            .setParentSetting(crystals);

    public static Setting<Boolean> rubiks = new Setting<>("Rubik's Cube", false)
            .setDescription("Make end crystals look like Rubik's cubes")
            .setParentSetting(crystals);

    public static Setting<Float> scaleSetting = new Setting<>("Scale", 0.6f, 0.0f, 1f, 0.01f)
            .setDescription("The scale of the crystal")
            .setParentSetting(crystals)
            .setVisibility(() -> !rubiks.getValue());

    public static Setting<Float> time = new Setting<>("Time", 400f, 200f, 1000f, 1f)
            .setDescription("The time it takes for a side to rotate")
            .setParentSetting(crystals)
            .setVisibility(() -> rubiks.getValue());

    public static Setting<Boolean> cube = new Setting<>("Cube", true)
            .setDescription("Render the crystal cube")
            .setParentSetting(crystals);

    public static Setting<Boolean> glass = new Setting<>("Glass", true)
            .setDescription("Render the glass")
            .setParentSetting(crystals);

    // Render settings

    public static Setting<Boolean> texture = new Setting<>("Texture", false)
            .setDescription("Render the entity's texture");

    public static Setting<Boolean> lighting = new Setting<>("Lighting", true)
            .setDescription("Disables lighting");

    public static Setting<Boolean> blend = new Setting<>("Blend", true)
            .setDescription("Enables blending");

    public static Setting<Boolean> transparent = new Setting<>("Transparent", true)
            .setDescription("Enables transparency on models");

    public static Setting<Boolean> depth = new Setting<>("Depth", true)
            .setDescription("Enables depth");

    public static Setting<Boolean> walls = new Setting<>("Walls", true)
            .setDescription("Render entities through walls");

    public static Setting<Color> colour = new Setting<>("Colour", new Color(185, 17, 255, 85))
            .setDescription("The colour of the crystal");

    private int rotating;
    private long lastTime;
    private ModelRenderer cubeModel;

    public Chams() {
        super("Chams", Category.RENDER, "Shows entities through walls");
    }

    @Listener
    public void onRenderEntity(RenderEntityEvent event) {
        // Check entity is valid
        if (isEntityValid(event.getEntity())) {
            // Cancel model render
            if (!texture.getValue()) {
                event.cancel();
            }

            // Enable transparency
            if (transparent.getValue()) {
                GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }

            GL11.glPushMatrix();
            GL11.glPushAttrib(GL_ALL_ATTRIB_BITS);

            // Disable texture
            if (!texture.getValue()) {
                GL11.glDisable(GL_TEXTURE_2D);
            }

            // Enable blend
            if (blend.getValue()) {
                GL11.glEnable(GL_BLEND);
            }

            // Disable lighting
            if (lighting.getValue()) {
                GL11.glDisable(GL_LIGHTING);
            }

            // Remove depth
            if (depth.getValue()) {
                GL11.glDepthMask(false);
            }

            // Remove depth
            if (walls.getValue()) {
                GL11.glDisable(GL_DEPTH_TEST);
            }

            // Change polygon rendering mode
            switch (mode.getValue()) {
                case WIRE:
                    GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                    break;

                case WIRE_MODEL:
                case MODEL:
                    GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                    break;
            }

            // Anti aliasing for smooth lines
            GL11.glEnable(GL_LINE_SMOOTH);
            GL11.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

            // Set line width
            GL11.glLineWidth(width.getValue());

            // Set colour
            GL11.glColor4f(colour.getValue().getRed() / 255f, colour.getValue().getGreen() / 255f, colour.getValue().getBlue() / 255f, colour.getAlpha() / 255f);

            // Render model
            event.renderModel();

            // Re enable depth
            if (walls.getValue() && !mode.getValue().equals(Mode.WIRE_MODEL)) {
                GL11.glEnable(GL_DEPTH_TEST);
            }

            // Change polygon rendering mode
            if (mode.getValue().equals(Mode.WIRE_MODEL)) {
                GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            }

            GL11.glColor4f(colour.getValue().getRed() / 255f, colour.getValue().getGreen() / 255f, colour.getValue().getBlue() / 255f, mode.getValue().equals(Mode.MODEL) ? colour.getAlpha() / 255f : 1);

            // Render model
            event.renderModel();

            // Re enable depth
            if (walls.getValue() && mode.getValue().equals(Mode.WIRE_MODEL)) {
                GL11.glEnable(GL_DEPTH_TEST);
            }

            // Enable lighting
            if (lighting.getValue()) {
                GL11.glEnable(GL_LIGHTING);
            }

            // Enable depth
            if (depth.getValue()) {
                GL11.glDepthMask(true);
            }

            // Enable blending
            if (blend.getValue()) {
                GL11.glDisable(GL_BLEND);
            }

            // Enable texture
            if (!texture.getValue()) {
                GL11.glEnable(GL_TEXTURE_2D);
            }

            // Reset colour
            GlStateManager.color(1, 1, 1, 1);

            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    @Listener
    public void onRenderCrystal(RenderCrystalEvent event) {
        if (crystals.getValue()) {
            // Cancel vanilla crystal render
            event.cancel();

            // Enable transparency
            if (transparent.getValue()) {
                GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }

            GL11.glPushMatrix();
            GL11.glPushAttrib(GL_ALL_ATTRIB_BITS);

            // Disable texture
            if (!texture.getValue()) {
                GL11.glDisable(GL_TEXTURE_2D);
            }

            // Enable blend
            if (blend.getValue()) {
                GL11.glEnable(GL_BLEND);
            }

            // Disable lighting
            if (lighting.getValue()) {
                GL11.glDisable(GL_LIGHTING);
            }

            // Remove depth
            if (depth.getValue()) {
                GL11.glDepthMask(false);
            }

            // Remove depth
            if (walls.getValue()) {
                GL11.glDisable(GL_DEPTH_TEST);
            }

            // Change polygon rendering mode
            switch (mode.getValue()) {
                case WIRE:
                    GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                    break;

                case WIRE_MODEL:
                case MODEL:
                    GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                    break;
            }

            // Anti aliasing for smooth lines
            GL11.glEnable(GL_LINE_SMOOTH);
            GL11.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

            // Set line width
            GL11.glLineWidth(width.getValue());

            // Set colour
            GL11.glColor4f(colour.getValue().getRed() / 255f, colour.getValue().getGreen() / 255f, colour.getValue().getBlue() / 255f, colour.getAlpha() / 255f);

            // Render crystal
            renderCrystal(event);

            // Re enable depth
            if (walls.getValue() && !mode.getValue().equals(Mode.WIRE_MODEL)) {
                GL11.glEnable(GL_DEPTH_TEST);
            }

            // Change polygon rendering mode
            if (mode.getValue().equals(Mode.WIRE_MODEL)) {
                GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            }

            GL11.glColor4f(colour.getValue().getRed() / 255f, colour.getValue().getGreen() / 255f, colour.getValue().getBlue() / 255f, mode.getValue().equals(Mode.MODEL) ? colour.getAlpha() / 255f : 1);

            renderCrystal(event);

            // Re enable depth
            if (walls.getValue() && mode.getValue().equals(Mode.WIRE_MODEL)) {
                GL11.glEnable(GL_DEPTH_TEST);
            }

            // Enable lighting
            if (lighting.getValue()) {
                GL11.glEnable(GL_LIGHTING);
            }

            // Enable depth
            if (depth.getValue()) {
                GL11.glDepthMask(true);
            }

            // Enable blending
            if (blend.getValue()) {
                GL11.glDisable(GL_BLEND);
            }

            // Enable texture
            if (!texture.getValue()) {
                GL11.glEnable(GL_TEXTURE_2D);
            }

            // Reset colour
            GlStateManager.color(1, 1, 1, 1);

            // Reset scale
            GlStateManager.scale(1 / scaleSetting.getValue(), 1 / scaleSetting.getValue(), 1 / scaleSetting.getValue());

            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    public void renderCrystal(RenderCrystalEvent event) {
        if (rubiks.getValue()) {
            cubeModel = event.getCube();

            GL11.glPushMatrix();

            float oldScale = scaleSetting.getValue();
            scaleSetting.setValue(0.4f);

            GlStateManager.scale(2, 2, 2);
            GlStateManager.translate(0, -0.5, 0);

            if (event.getBase() != null) {
                event.getBase().render(event.getScale());
            }

            GlStateManager.rotate(event.getLimbSwingAmount(), 0, 1, 0);

            GlStateManager.translate(0, 1f + (bounce.getValue() ? event.getAgeInTicks() : 0), 0);

            GlStateManager.rotate(60, 0.7071f, 0, 0.7071f);
            GlStateManager.scale(0.875f, 0.875f, 0.875f);
            GlStateManager.rotate(60, 0.7071f, 0, 0.7071f);
            GlStateManager.rotate(event.getLimbSwingAmount(), 0, 1, 0);

            if (glass.getValue()) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(1f, 1f, 1f);

                event.getGlass().render(event.getScale());

                GlStateManager.popMatrix();
            }

            GlStateManager.scale(0.875f, 0.875f, 0.875f);
            GlStateManager.rotate(60, 0.7071f, 0, 0.7071f);
            GlStateManager.rotate(event.getLimbSwingAmount(), 0, 1, 0);

            if (glass.getValue()) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(1f, 1f, 1f);

                event.getGlass().render(event.getScale());

                GlStateManager.popMatrix();
            }

            GlStateManager.scale(0.875f, 0.875f, 0.875f);
            GlStateManager.rotate(60, 0.7071f, 0, 0.7071f);
            GlStateManager.rotate(event.getLimbSwingAmount(), 0, 1, 0);

            GlStateManager.scale(scaleSetting.getValue(), scaleSetting.getValue(), scaleSetting.getValue());
            event.setScale(event.getScale() * scaleSetting.getValue());

            long currentTime = Minecraft.getSystemTime();
            if (currentTime - time.getValue() > lastTime) {
                int[] currentSide = RubiksCrystalUtil.cubeSides[rotating];
                Quaternion[] cubelets = {
                        RubiksCrystalUtil.cubeletStatus[currentSide[0]],
                        RubiksCrystalUtil.cubeletStatus[currentSide[1]],
                        RubiksCrystalUtil.cubeletStatus[currentSide[2]],
                        RubiksCrystalUtil.cubeletStatus[currentSide[3]],
                        RubiksCrystalUtil.cubeletStatus[currentSide[4]],
                        RubiksCrystalUtil.cubeletStatus[currentSide[5]],
                        RubiksCrystalUtil.cubeletStatus[currentSide[6]],
                        RubiksCrystalUtil.cubeletStatus[currentSide[7]],
                        RubiksCrystalUtil.cubeletStatus[currentSide[8]]
                };

                RubiksCrystalUtil.cubeletStatus[currentSide[0]] = cubelets[6];
                RubiksCrystalUtil.cubeletStatus[currentSide[1]] = cubelets[3];
                RubiksCrystalUtil.cubeletStatus[currentSide[2]] = cubelets[0];
                RubiksCrystalUtil.cubeletStatus[currentSide[3]] = cubelets[7];
                RubiksCrystalUtil.cubeletStatus[currentSide[4]] = cubelets[4];
                RubiksCrystalUtil.cubeletStatus[currentSide[5]] = cubelets[1];
                RubiksCrystalUtil.cubeletStatus[currentSide[6]] = cubelets[8];
                RubiksCrystalUtil.cubeletStatus[currentSide[7]] = cubelets[5];
                RubiksCrystalUtil.cubeletStatus[currentSide[8]] = cubelets[2];

                int[] transform = RubiksCrystalUtil.cubeSideTransforms[rotating];
                for (int x = -1; x < 2; x++) {
                    for (int y = -1; y < 2; y++) {
                        for (int z = -1; z < 2; z++) {
                            applyRotation(x, y, z, transform[0], transform[1], transform[2]);
                        }
                    }
                }

                rotating = ThreadLocalRandom.current().nextInt(0, 6);
                lastTime = currentTime;
            }

            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            drawCubeletStatic(event.getScale(), x, y, z);
                        }
                    }
                }
            }

            int[] transform = RubiksCrystalUtil.cubeSideTransforms[rotating];

            GlStateManager.pushMatrix();
            GlStateManager.translate(transform[0] * scaleSetting.getValue(), transform[1] * scaleSetting.getValue(), transform[2] * scaleSetting.getValue());

            float angle = (float) Math.toRadians(Easing.EXPO_IN_OUT.ease((currentTime - lastTime) / time.getValue()) * 90);
            float xx = (float) (transform[0] * Math.sin(angle / 2));
            float yy = (float) (transform[1] * Math.sin(angle / 2));
            float zz = (float) (transform[2] * Math.sin(angle / 2));
            float ww = (float) Math.cos(angle / 2);

            Quaternion quaternion = new Quaternion(xx, yy, zz, ww);

            GlStateManager.rotate(quaternion);

            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            drawCubeletRotating(event.getScale(), x, y, z);
                        }
                    }
                }
            }

            scaleSetting.setValue(oldScale);

            GL11.glPopMatrix();
        }

        else {
            GlStateManager.pushMatrix();
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            GlStateManager.translate(0.0F, -0.5F, 0.0F);

            if (event.getBase() != null) {
                event.getBase().render(event.getScale());
            }

            GlStateManager.rotate(event.getLimbSwingAmount(), 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, 1f + (bounce.getValue() ? event.getAgeInTicks() : 0), 0.0F);
            GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);

            GlStateManager.pushMatrix();
            GlStateManager.scale(scaleSetting.getValue(), scaleSetting.getValue(), scaleSetting.getValue());

            if (glass.getValue()) {
                event.getGlass().render(event.getScale());
            }

            GlStateManager.popMatrix();

            GlStateManager.scale(0.875F, 0.875F, 0.875F);
            GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);
            GlStateManager.rotate(event.getLimbSwingAmount(), 0.0F, 1.0F, 0.0F);

            GlStateManager.pushMatrix();
            GlStateManager.scale(scaleSetting.getValue(), scaleSetting.getValue(), scaleSetting.getValue());

            if (glass.getValue()) {
                event.getGlass().render(event.getScale());
            }

            GlStateManager.popMatrix();

            GlStateManager.scale(0.875F, 0.875F, 0.875F);
            GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);
            GlStateManager.rotate(event.getLimbSwingAmount(), 0.0F, 1.0F, 0.0F);

            GlStateManager.pushMatrix();
            GlStateManager.scale(scaleSetting.getValue(), scaleSetting.getValue(), scaleSetting.getValue());

            if (cube.getValue()) {
                event.getCube().render(event.getScale());
            }

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    public void drawCubeletStatic(float scale, int x, int y, int z) {
        int id = RubiksCrystalUtil.cubeletLookup[x + 1][y + 1][z + 1];

        if (Arrays.stream(RubiksCrystalUtil.cubeSides[rotating]).anyMatch(i -> i == id)) {
            return;
        }

        drawCubelet(scale, x, y, z, id);
    }

    public void drawCubeletRotating(float scale, int x, int y, int z) {
        int id = RubiksCrystalUtil.cubeletLookup[x + 1][y + 1][z + 1];

        if (!Arrays.stream(RubiksCrystalUtil.cubeSides[rotating]).anyMatch(i -> i == id)) {
            return;
        }

        int[] transform = RubiksCrystalUtil.cubeSideTransforms[rotating];

        drawCubelet(scale, x - transform[0], y - transform[1], z - transform[2], id);
    }

    public void applyRotation(int x, int y, int z, int rX, int rY, int rZ) {
        int id = RubiksCrystalUtil.cubeletLookup[x + 1][y + 1][z + 1];

        if (!Arrays.stream(RubiksCrystalUtil.cubeSides[rotating]).anyMatch(i -> i == id)) {
            return;
        }

        float angle = (float) Math.toRadians(90);
        float xx = (float) (rX * Math.sin(angle / 2));
        float yy = (float) (rY * Math.sin(angle / 2));
        float zz = (float) (rZ * Math.sin(angle / 2));
        float ww = (float) Math.cos(angle / 2);

        RubiksCrystalUtil.cubeletStatus[id] = Quaternion.mul(new Quaternion(xx, yy, zz, ww), RubiksCrystalUtil.cubeletStatus[id], null);
    }

    public void drawCubelet(float scale, int x, int y, int z, int id) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x * scaleSetting.getValue(), y * scaleSetting.getValue(), z * scaleSetting.getValue());
        GlStateManager.pushMatrix();

        GlStateManager.rotate(RubiksCrystalUtil.cubeletStatus[id]);

        if (cube.getValue()) {
            cubeModel.render(scale);
        }

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    /**
     * Checks if an entity is valid
     *
     * @param entityIn The entity to check
     * @return Is the entity valid
     */
    private boolean isEntityValid(Entity entityIn) {
        return entityIn instanceof EntityPlayer && entityIn != mc.player && players.getValue() || entityIn instanceof EntityLiving && !(entityIn instanceof EntityMob) && passives.getValue() || entityIn instanceof EntityMob && mobs.getValue();
    }

    public enum Mode {
        /**
         * Renders the model
         */
        MODEL,

        /**
         * Outlines the model
         */
        WIRE,

        /**
         * Renders and outlines the model
         */
        WIRE_MODEL
    }

}
