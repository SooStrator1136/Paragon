package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.player.RenderItemEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHandSide;

/**
 * @author Wolfsurge
 */
public class ViewModel extends Module {

    // Main hand settings
    private final Setting<Boolean> main = new Setting<>("Main Hand", true)
            .setDescription("Modify your main hand");

    private final Setting<Float> mainX = new Setting<>("X", 0.19f, -2f, 2f, 0.01f)
            .setDescription("The x of the item")
            .setParentSetting(main);

    private final Setting<Float> mainY = new Setting<>("Y", -0.14f, -2f, 2f, 0.01f)
            .setDescription("The y of the item")
            .setParentSetting(main);

    private final Setting<Float> mainZ = new Setting<>("Z", -0.43f, -2f, 2f, 0.01f)
            .setDescription("The z of the item")
            .setParentSetting(main);

    private final Setting<Float> mainYaw = new Setting<>("Yaw", 0f, -100f, 100f, 1f)
            .setDescription("The yaw of the item")
            .setParentSetting(main);

    private final Setting<Float> mainPitch = new Setting<>("Pitch", 0f, -100f, 100f, 1f)
            .setDescription("The pitch of the item")
            .setParentSetting(main);

    private final Setting<Float> mainRoll = new Setting<>("Roll", 0f, -100f, 100f, 1f)
            .setDescription("The roll of the item")
            .setParentSetting(main);

    // Offhand settings
    private final Setting<Boolean> offhand = new Setting<>("Offhand", true)
            .setDescription("Modify your offhand");

    private final Setting<Float> offhandX = new Setting<>("X", -0.19f, -2f, 2f, 0.01f)
            .setDescription("The x of the item")
            .setParentSetting(offhand);

    private final Setting<Float> offhandY = new Setting<>("Y", -0.14f, -2f, 2f, 0.01f)
            .setDescription("The y of the item").setParentSetting(offhand);

    private final Setting<Float> offhandZ = new Setting<>("Z", -0.43f, -2f, 2f, 0.01f)
            .setDescription("The z of the item")
            .setParentSetting(offhand);

    private final Setting<Float> offhandYaw = new Setting<>("Yaw", 0f, -100f, 100f, 1f)
            .setDescription("The yaw of the item")
            .setParentSetting(offhand);

    private final Setting<Float> offhandPitch = new Setting<>("Pitch", 0f, -10f, 100f, 1f)
            .setDescription("The pitch of the item")
            .setParentSetting(offhand);

    private final Setting<Float> offhandRoll = new Setting<>("Roll", 0f, -100f, 100f, 1f)
            .setDescription("The roll of the item")
            .setParentSetting(offhand);

    public ViewModel() {
        super("ViewModel", Category.RENDER, "Changes the way items are rendered in your hand");
        this.addSettings(main, offhand);
    }

    @Listener
    public void onRenderItemPre(RenderItemEvent.Pre event) {
        if (event.getSide() == EnumHandSide.LEFT && offhand.getValue()) {
            // Translate offhand item according to x, y, and z settings
            GlStateManager.translate(offhandX.getValue(), offhandY.getValue(), offhandZ.getValue());
        }

        if (event.getSide() == EnumHandSide.RIGHT && main.getValue()) {
            // Translate main hand item according to x, y, and z settings
            GlStateManager.translate(mainX.getValue(), mainY.getValue(), mainZ.getValue());
        }
    }

    @Listener
    public void onRenderItemPost(RenderItemEvent.Post event) {
        if (event.getSide() == EnumHandSide.LEFT && offhand.getValue()) {
            // Rotate offhand item according to yaw, pitch, and roll settings
            GlStateManager.rotate(offhandYaw.getValue(), 0, 1, 0);
            GlStateManager.rotate(offhandPitch.getValue(), 1, 0, 0);
            GlStateManager.rotate(offhandRoll.getValue(), 0, 0, 1);
        }

        if (event.getSide() == EnumHandSide.RIGHT && main.getValue()) {
            // Rotate main hand item according to yaw, pitch, and roll settings
            GlStateManager.rotate(mainYaw.getValue(), 0, 1, 0);
            GlStateManager.rotate(mainPitch.getValue(), 1, 0, 0);
            GlStateManager.rotate(mainRoll.getValue(), 0, 0, 1);
        }
    }

}
