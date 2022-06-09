package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.render.ColourUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Wolfsurge
 */
public class Breadcrumbs extends Module {

    public static Breadcrumbs INSTANCE;

    // Settings
    public static Setting<Boolean> infinite = new Setting<>("Infinite", false)
            .setDescription("Breadcrumbs last forever");

    public static Setting<Float> lifespanValue = new Setting<>("Lifespan", 100f, 1f, 1000f, 1f)
            .setDescription("The lifespan of the positions in ticks")
            .setVisibility(() -> !infinite.getValue());

    public static Setting<Float> lineWidth = new Setting<>("LineWidth", 1f, 0.1f, 5f, 0.1f)
            .setDescription("The width of the lines");

    public static Setting<Color> colour = new Setting<>("Colour", new Color(185, 17, 255))
            .setDescription("The colour of the breadcrumbs");

    public static Setting<Boolean> rainbow = new Setting<>("Rainbow", true)
            .setDescription("Makes the trail a rainbow");

    private final LinkedList<Position> positions = new LinkedList<>();
    private int colourHue = 0;

    public Breadcrumbs() {
        super("Breadcrumbs", Category.RENDER, "Draws a trail behind you");

        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        // Clear positions when we disable
        positions.clear();
    }

    @Override
    public void onTick() {
        if (nullCheck() || mc.player.ticksExisted <= 20) {
            // We may have just loaded into a world, so we need to clear the positions
            positions.clear();
            return;
        }

        // Create position
        Position pos = new Position(new Vec3d(mc.player.lastTickPosX, mc.player.lastTickPosY, mc.player.lastTickPosZ), new Color(ColourUtil.getRainbow(4, 1, colourHue)));
        colourHue++;

        // Add position
        positions.add(pos);

        // Update positions
        positions.forEach(Position::update);

        // Remove old positions
        positions.removeIf(p -> !p.isAlive() && !infinite.getValue());
    }

    @Override
    public void onRender3D() {
        glPushMatrix();

        // GL stuff
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        glLineWidth(lineWidth.getValue());

        // Disable lighting
        mc.entityRenderer.disableLightmap();

        glBegin(GL_LINE_STRIP);

        for (Position pos : positions) {
            double renderPosX = mc.getRenderManager().viewerPosX;
            double renderPosY = mc.getRenderManager().viewerPosY;
            double renderPosZ = mc.getRenderManager().viewerPosZ;

            ColourUtil.setColour(rainbow.getValue() ? pos.getColour().getRGB() : colour.getValue().getRGB());
            glVertex3d(pos.getPosition().x - renderPosX, pos.getPosition().y - renderPosY, pos.getPosition().z - renderPosZ);
        }

        // Reset colour
        glColor4d(1, 1, 1, 1);

        // End GL
        glEnd();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    class Position {
        // Vec3d of position
        private final Vec3d position;
        // Position's colour
        private final Color colour;
        // Position's lifespan
        private long lifespan = lifespanValue.getValue().longValue();

        public Position(Vec3d position, Color colour) {
            this.position = position;
            this.colour = colour;
        }

        /**
         * Decreases the lifespan of the position
         */
        public void update() {
            lifespan--;
        }

        /**
         * Checks if the position is alive
         *
         * @return If the position is alive
         */
        public boolean isAlive() {
            return lifespan > 0;
        }

        /**
         * Gets the position
         *
         * @return The position
         */
        public Vec3d getPosition() {
            return position;
        }

        /**
         * Gets the position's colour
         *
         * @return The position's colour
         */
        public Color getColour() {
            return colour;
        }
    }
}
