package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Wolfsurge
 * @since 01/05/2022
 */
public class ChinaHat extends Module {

    public static ChinaHat INSTANCE;

    // Colours
    public static Setting<Color> topColour = new Setting<>("TopColour", new Color(185, 17, 255, 180))
            .setDescription("The top colour of the hat");

    public static Setting<Color> bottomColour = new Setting<>("BottomColour", new Color(185, 17, 255, 180))
            .setDescription("The bottom colour of the hat");

    // Settings
    public static Setting<Boolean> firstPerson = new Setting<>("FirstPerson", false)
            .setDescription("Render the hat in first person");

    public static Setting<Boolean> others = new Setting<>("Others", true)
            .setDescription("Render the hat on other players");

    public ChinaHat() {
        super("ChinaHat", Category.RENDER, "-69420 social credit :((");

        INSTANCE = this;
    }

    @Override
    public void onRender3D() {
        // Iterate through all players
        mc.world.playerEntities.forEach(player -> {
            // We don't want to render the hat
            if (player == mc.player && !firstPerson.getValue() && mc.gameSettings.thirdPersonView == 0 || !others.getValue() && player != mc.player) {
                return;
            }

            // Render the hat
            renderHat(player);
        });
    }

    public void renderHat(EntityPlayer player) {
        glPushMatrix();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_POINT_SMOOTH);
        glEnable(GL_BLEND);
        glShadeModel(GL_SMOOTH);
        GlStateManager.disableCull();
        glBegin(GL_TRIANGLE_STRIP);

        // Get the vector to start drawing the hat
        Vec3d vec = EntityUtil.getInterpolatedPosition(player).add(new Vec3d(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY + player.getEyeHeight() + 0.5 + (player.isSneaking() ? -0.2 : 0), -mc.getRenderManager().viewerPosZ));

        // Change vec if elytra flying
        if (player.isElytraFlying()) {
            vec.add(new Vec3d(PlayerUtil.forward(2)[0], -0.8, PlayerUtil.forward(2)[2]));
        }

        // Add vertices for each point
        for (float i = 0; i < Math.PI * 2; i += Math.PI * 4 / 128) {
            // X coord
            double hatX = vec.x + 0.65 * Math.cos(i);

            // Z coord
            double hatZ = vec.z + 0.65 * Math.sin(i);

            // Set bottom colour
            ColourUtil.setColour(bottomColour.getValue().getRGB());

            // Add bottom point
            glVertex3d(hatX, vec.y - 0.25, hatZ);

            // Set top colour
            ColourUtil.setColour(topColour.getValue().getRGB());

            // Add top point
            glVertex3d(vec.x, vec.y, vec.z);
        }

        glEnd();
        glShadeModel(GL_FLAT);
        glDepthMask(true);
        glEnable(GL_LINE_SMOOTH);
        GlStateManager.enableCull();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_POINT_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }
}
