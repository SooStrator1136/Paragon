package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.asm.mixins.accessor.IMinecraft;
import com.paragon.asm.mixins.accessor.IRenderManager;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.item.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glLineWidth;

public class Trajectories extends Module {

    private final Setting<Boolean> line = new Setting<>("Line", true)
            .setDescription("Render a line to the projectile's destination");

    private final Setting<Color> lineColour = new Setting<>("Line Colour", new Color(185, 17, 255))
            .setDescription("The colour of the line")
            .setParentSetting(line);

    private final Setting<Float> lineWidth = new Setting<>("Line Width", 1.0f, 0.1f, 3.0f, 0.1f)
            .setDescription("The width of the line")
            .setParentSetting(line);

    private final Setting<Boolean> box = new Setting<>("Box", true)
            .setDescription("Render a box at the projectile's destination");

    private final Setting<Boolean> fill = new Setting<>("Fill", true)
            .setDescription("Fill the box at the end of the line")
            .setParentSetting(box);

    private final Setting<Boolean> outline = new Setting<>("Outline", true)
            .setDescription("Outline the box at the end of the line")
            .setParentSetting(box);

    private final Setting<Float> outlineWidth = new Setting<>("Outline Width", 1.0f, 0.1f, 3.0f, 0.1f)
            .setDescription("The width of the outline")
            .setParentSetting(box)
            .setVisibility(outline::getValue);

    private final Setting<Color> boxColour = new Setting<>("Box Colour", new Color(185, 17, 255, 130))
            .setDescription("The colour of the box at the end of the line")
            .setVisibility(() -> fill.getValue() || outline.getValue())
            .setParentSetting(box);

    private final Setting<Boolean> bow = new Setting<>("Bow", true)
            .setDescription("Draw the trajectory of the bow");

    private final Setting<Boolean> snowball = new Setting<>("Snowball", true)
            .setDescription("Draw the trajectory of snowballs");

    private final Setting<Boolean> egg = new Setting<>("Egg", true)
            .setDescription("Draw the trajectory of eggs");

    private final Setting<Boolean> exp = new Setting<>("EXP", true)
            .setDescription("Draw the trajectory of EXP bottles");

    private final Setting<Boolean> potion = new Setting<>("Potion", true)
            .setDescription("Draw the trajectory of splash potions");

    public Trajectories() {
        super("Trajectories", Category.RENDER, "Shows where projectiles will land");
        this.addSettings(line, box, bow, snowball, egg, exp, potion);
    }

    @Override
    public void onRender3D() {
        if (nullCheck()) {
            return;
        }

        ItemStack stack = mc.player.getHeldItemMainhand();

        // Check the item we are holding is a projectile (or a bow) and that projectile is enabled
        if (stack.getItem() instanceof ItemBow && bow.getValue() || stack.getItem() instanceof ItemSnowball && snowball.getValue() || stack.getItem() instanceof ItemEgg && egg.getValue() || stack.getItem() instanceof ItemSplashPotion && potion.getValue() || stack.getItem() instanceof ItemExpBottle && exp.getValue()) {
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
            if (line.getValue()) {
                // GL render 3D
                glPushMatrix();
                glDisable(GL_TEXTURE_2D);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glDisable(GL_DEPTH_TEST);
                glDepthMask(false);
                glEnable(GL_LINE_SMOOTH);

                // Set line width
                glLineWidth(lineWidth.getValue());
                ColourUtil.setColour(lineColour.getValue().getRGB());

                glBegin(GL_LINE_STRIP);

                // Add vertices to the line whilst we haven't hit a target
                for (int i = 0; i < 1000; i++) {
                    // Add vertex
                    glVertex3d(position.x - ((IRenderManager) mc.getRenderManager()).getRenderX(), position.y - ((IRenderManager) mc.getRenderManager()).getRenderY(), position.z - ((IRenderManager) mc.getRenderManager()).getRenderZ());

                    // Move position
                    position = new Vec3d(position.x + (velocity.x * 0.1D), position.y + (velocity.y * 0.1D), position.z + (velocity.z * 0.1D));
                    velocity = new Vec3d(velocity.x, velocity.y - (stack.getItem() instanceof ItemBow ? 0.05 : (stack.getItem() instanceof ItemPotion ? 0.4 : stack.getItem() instanceof ItemExpBottle ? 0.1 : 0.03)) * 0.1, velocity.z);

                    // Check if we hit a target
                    RayTraceResult result = mc.world.rayTraceBlocks(EntityUtil.getInterpolatedPosition(mc.player).add(0, mc.player.getEyeHeight(), 0), new Vec3d(position.x, position.y, position.z));

                    if (result != null) {
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
            if (box.getValue()) {
                // Get highlight bb
                AxisAlignedBB bb = new AxisAlignedBB((position.x - ((IRenderManager) mc.getRenderManager()).getRenderX()) - 0.25, (position.y - ((IRenderManager) mc.getRenderManager()).getRenderY()) - 0.25, (position.z - ((IRenderManager) mc.getRenderManager()).getRenderZ()) - 0.25,
                        (position.x - ((IRenderManager) mc.getRenderManager()).getRenderX()) + 0.25, (position.y - ((IRenderManager) mc.getRenderManager()).getRenderY()) + 0.25, (position.z - ((IRenderManager) mc.getRenderManager()).getRenderZ()) + 0.25);

                // Draw filled box
                if (fill.getValue()) {
                    RenderUtil.drawFilledBox(bb, boxColour.getValue());
                }

                // Draw outline box
                if (outline.getValue()) {
                    RenderUtil.drawBoundingBox(bb, outlineWidth.getValue(), ColourUtil.integrateAlpha(boxColour.getValue(), 255));
                }
            }
        }
    }
}