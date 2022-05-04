package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.asm.mixins.accessor.IMinecraft;
import com.paragon.asm.mixins.accessor.IRenderManager;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ColourSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import net.minecraft.item.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glLineWidth;

public class Trajectories extends Module {

    private final BooleanSetting line = new BooleanSetting("Line", "Render a line to the projectile's destination", true);
    private final ColourSetting lineColour = (ColourSetting) new ColourSetting("Line Colour", "The colour of the line", new Color(185, 17, 255)).setParentSetting(line);
    private final NumberSetting lineWidth = (NumberSetting) new NumberSetting("Line Width", "The width of the line", 1.0f, 0.1f, 3.0f, 0.1f).setParentSetting(line);

    private final BooleanSetting box = new BooleanSetting("Box", "Render a box at the projectile's destination", true);
    private final BooleanSetting fill = (BooleanSetting) new BooleanSetting("Fill", "Fill the box at the end of the line", true).setParentSetting(box);
    private final BooleanSetting outline = (BooleanSetting) new BooleanSetting("Outline", "Outline the box at the end of the line", true).setParentSetting(box);
    private final NumberSetting outlineWidth = (NumberSetting) new NumberSetting("Outline Width", "The width of the outline", 1.0f, 0.1f, 3.0f, 0.1f).setParentSetting(box)
            .setVisiblity(outline::isEnabled);
    private final ColourSetting boxColour = (ColourSetting) new ColourSetting("Box Colour", "The colour of the box at the end of the line", new Color(185, 17, 255, 130))
            .setVisiblity(() -> fill.isEnabled() || outline.isEnabled()).setParentSetting(box);

    private final BooleanSetting bow = new BooleanSetting("Bow", "Draw the trajectory of the bow", true);
    private final BooleanSetting snowball = new BooleanSetting("Snowball", "Draw the trajectory of snowballs", true);
    private final BooleanSetting egg = new BooleanSetting("Egg", "Draw the trajectory of eggs", true);
    private final BooleanSetting exp = new BooleanSetting("EXP", "Draw the trajectory of EXP bottles", true);
    private final BooleanSetting potion = new BooleanSetting("Potion", "Draw the trajectory of splash potions", true);

    public Trajectories() {
        super("Trajectories", ModuleCategory.RENDER, "Shows where projectiles will land");
        this.addSettings(line, box, bow, snowball, egg, exp, potion);
    }

    @Override
    public void onRender3D() {
        if (nullCheck()) {
            return;
        }

        ItemStack stack = mc.player.getHeldItemMainhand();

        // Check the item we are holding is a projectile (or a bow) and that projectile is enabled
        if (stack.getItem() instanceof ItemBow && bow.isEnabled() || stack.getItem() instanceof ItemSnowball && snowball.isEnabled() || stack.getItem() instanceof ItemEgg && egg.isEnabled() || stack.getItem() instanceof ItemSplashPotion && potion.isEnabled() || stack.getItem() instanceof ItemExpBottle && exp.isEnabled()) {
            // If we are holding a bow, make sure we are charging
            if (stack.getItem() instanceof ItemBow && !mc.player.isHandActive()) {
                return;
            }

            // Original arrow position
            Vec3d position = new Vec3d(
                    // X position
                    mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * ((IMinecraft) mc).getTimer().renderPartialTicks - (Math.cos((float) Math.toRadians(mc.player.rotationYaw)) * 0.16F),
                    // Y position
                    mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * ((IMinecraft) mc).getTimer().renderPartialTicks + mc.player.getEyeHeight() - 0.15,
                    // Z position
                    mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * ((IMinecraft) mc).getTimer().renderPartialTicks - (Math.sin((float) Math.toRadians(mc.player.rotationYaw)) * 0.16F));

            // Original arrow velocity
            Vec3d velocity = new Vec3d(
                    // X velocity
                    -Math.sin(Math.toRadians(mc.player.rotationYaw)) * Math.cos(Math.toRadians(mc.player.rotationPitch)) * (stack.getItem() instanceof ItemBow ? 1.0F : 0.4F),
                    // Y velocity
                    -Math.sin(Math.toRadians(mc.player.rotationPitch)) * (stack.getItem() instanceof ItemBow ? 1.0F : 0.4F),
                    // Z velocity
                    Math.cos(Math.toRadians(mc.player.rotationYaw)) * Math.cos(Math.toRadians(mc.player.rotationPitch)) * (stack.getItem() instanceof ItemBow ? 1.0F : 0.4F));

            // Motion factor
            double motion = Math.sqrt((velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z));

            // New velocity
            velocity = new Vec3d(velocity.x / motion, velocity.y / motion, velocity.z / motion);

            // If we are holding a bow
            if (stack.getItem() instanceof ItemBow) {
                // Get the charge power
                float power = MathHelper.clamp((((72000 - mc.player.getItemInUseCount()) / 20.0F) * ((72000 - mc.player.getItemInUseCount()) / 20.0F) + ((72000 - mc.player.getItemInUseCount()) / 20.0F) * 2.0F) / 3.0F, 0, 1) * 3;

                // Set velocity
                velocity = new Vec3d(velocity.x * power, velocity.y * power, velocity.z * power);
            }

            // If we are holding a different projectile (e.g. snowball), times velocity by 1.5
            else {
                velocity = new Vec3d(velocity.x * 1.5, velocity.y * 1.5, velocity.z * 1.5);
            }

            // Check we want to draw the line
            if (line.isEnabled()) {
                // GL render 3D
                glPushMatrix();
                glDisable(GL_TEXTURE_2D);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glDisable(GL_DEPTH_TEST);
                glDepthMask(false);
                glEnable(GL_LINE_SMOOTH);

                // Set line width
                glLineWidth(1);
                ColourUtil.setColour(lineColour.getColour().getRGB());

                glBegin(GL_LINE_STRIP);

                // Add vertices to the line whilst we haven't hit a target
                for (int i = 0; i < 100000; i++) {
                    // Add vertex
                    glVertex3d(position.x - ((IRenderManager) mc.getRenderManager()).getRenderX(), position.y - ((IRenderManager) mc.getRenderManager()).getRenderY(), position.z - ((IRenderManager) mc.getRenderManager()).getRenderZ());

                    // Move position
                    position = new Vec3d(position.x + (velocity.x * 0.1D), position.y + (velocity.y * 0.1D), position.z + (velocity.z * 0.1D));
                    velocity = new Vec3d(velocity.x, velocity.y - (stack.getItem() instanceof ItemBow ? 0.05 : 0.03) * 0.1, velocity.z);

                    // Check if we hit a target
                    RayTraceResult result = mc.world.rayTraceBlocks(EntityUtil.getInterpolatedPosition(mc.player).add(0, mc.player.getEyeHeight(), 0), new Vec3d(position.x, position.y, position.z));
                    if (result != null && result.typeOfHit != RayTraceResult.Type.MISS) {
                        break;
                    }
                }

                // Stop adding vertices
                glEnd();

                // Disable GL render 3D
                glDisable(GL_BLEND);
                glEnable(GL_TEXTURE_2D);
                glEnable(GL_DEPTH_TEST);
                glDepthMask(true);
                glDisable(GL_LINE_SMOOTH);
                glPopMatrix();
            }

            // Check we want to draw the box
            if (box.isEnabled()) {
                // Get highlight bb
                AxisAlignedBB bb = new AxisAlignedBB((position.x - ((IRenderManager) mc.getRenderManager()).getRenderX()) - 0.25, (position.y - ((IRenderManager) mc.getRenderManager()).getRenderY()) - 0.25, (position.z - ((IRenderManager) mc.getRenderManager()).getRenderZ()) - 0.25,
                        (position.x - ((IRenderManager) mc.getRenderManager()).getRenderX()) + 0.25, (position.y - ((IRenderManager) mc.getRenderManager()).getRenderY()) + 0.25, (position.z - ((IRenderManager) mc.getRenderManager()).getRenderZ()) + 0.25);

                // Draw filled box
                if (fill.isEnabled()) {
                    RenderUtil.drawFilledBox(bb, boxColour.getColour());
                }

                // Draw outline box
                if (outline.isEnabled()) {
                    RenderUtil.drawBoundingBox(bb, outlineWidth.getValue(), ColourUtil.integrateAlpha(boxColour.getColour(), 255));
                }
            }
        }
    }
}