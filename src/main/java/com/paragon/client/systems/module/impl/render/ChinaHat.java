package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.setting.Setting;
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

    // Colours
    private final Setting<Color> topColour = new Setting<>("Top Colour", new Color(185, 17, 255, 180))
            .setDescription("The top colour of the hat");

    private final Setting<Color> bottomColour = new Setting<>("Bottom Colour", new Color(185, 17, 255, 180))
            .setDescription("The bottom colour of the hat");

    // Settings
    private final Setting<Boolean> firstPerson = new Setting<>("First Person", false)
            .setDescription("Render the hat in first person");

    private final Setting<Boolean> others = new Setting<>("Others", true)
            .setDescription("Render the hat on other players");

    public ChinaHat() {
        super("ChinaHat", ModuleCategory.RENDER, "-69420 social credit :((");
        this.addSettings(topColour, bottomColour, firstPerson, others);
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
        Vec3d vec = EntityUtil.getInterpolatedPosition(player).add(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY + player.getEyeHeight() + 0.5 + (player.isSneaking() ? -0.2 : 0), -mc.getRenderManager().viewerPosZ);

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
