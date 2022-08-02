package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.player.RenderItemEvent;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHandSide;

/**
 * @author Surge
 */
public class ViewModel extends Module {

    public static ViewModel INSTANCE;

    // Main hand settings
    public static Setting<Boolean> main = new Setting<>("MainHand", true)
            .setDescription("Modify your main hand");

    public static Setting<Float> mainX = new Setting<>("X", 0.19f, -2f, 2f, 0.01f)
            .setDescription("The x of the item")
            .setParentSetting(main);

    public static Setting<Float> mainY = new Setting<>("Y", -0.14f, -2f, 2f, 0.01f)
            .setDescription("The y of the item")
            .setParentSetting(main);

    public static Setting<Float> mainZ = new Setting<>("Z", -0.43f, -2f, 2f, 0.01f)
            .setDescription("The z of the item")
            .setParentSetting(main);

    public static Setting<Float> mainYaw = new Setting<>("Yaw", 0f, -100f, 100f, 1f)
            .setDescription("The yaw of the item")
            .setParentSetting(main);

    public static Setting<Float> mainPitch = new Setting<>("Pitch", 0f, -100f, 100f, 1f)
            .setDescription("The pitch of the item")
            .setParentSetting(main);

    public static Setting<Float> mainRoll = new Setting<>("Roll", 0f, -100f, 100f, 1f)
            .setDescription("The roll of the item")
            .setParentSetting(main);

    public static Setting<Float> mainScaleX = new Setting<>("ScaleX", 1f, 0f, 1f, 0.01f)
            .setDescription("The X scale of the item")
            .setParentSetting(main);

    public static Setting<Float> mainScaleY = new Setting<>("ScaleY", 1f, 0f, 1f, 0.01f)
            .setDescription("The Y scale of the item")
            .setParentSetting(main);

    public static Setting<Float> mainScaleZ = new Setting<>("ScaleZ", 1f, 0f, 1f, 0.01f)
            .setDescription("The Z scale of the item")
            .setParentSetting(main);

    // Offhand settings
    public static Setting<Boolean> offhand = new Setting<>("Offhand", true)
            .setDescription("Modify your offhand");

    public static Setting<Float> offhandX = new Setting<>("X", -0.19f, -2f, 2f, 0.01f)
            .setDescription("The x of the item")
            .setParentSetting(offhand);

    public static Setting<Float> offhandY = new Setting<>("Y", -0.14f, -2f, 2f, 0.01f)
            .setDescription("The y of the item").setParentSetting(offhand);

    public static Setting<Float> offhandZ = new Setting<>("Z", -0.43f, -2f, 2f, 0.01f)
            .setDescription("The z of the item")
            .setParentSetting(offhand);

    public static Setting<Float> offhandYaw = new Setting<>("Yaw", 0f, -100f, 100f, 1f)
            .setDescription("The yaw of the item")
            .setParentSetting(offhand);

    public static Setting<Float> offhandPitch = new Setting<>("Pitch", 0f, -100f, 100f, 1f)
            .setDescription("The pitch of the item")
            .setParentSetting(offhand);

    public static Setting<Float> offhandRoll = new Setting<>("Roll", 0f, -100f, 100f, 1f)
            .setDescription("The roll of the item")
            .setParentSetting(offhand);

    public static Setting<Float> offhandScaleX = new Setting<>("ScaleX", 1f, 0f, 1f, 0.01f)
            .setDescription("The X scale of the item")
            .setParentSetting(offhand);

    public static Setting<Float> offhandScaleY = new Setting<>("ScaleY", 1f, 0f, 1f, 0.01f)
            .setDescription("The Y scale of the item")
            .setParentSetting(offhand);

    public static Setting<Float> offhandScaleZ = new Setting<>("ScaleZ", 1f, 0f, 1f, 0.01f)
            .setDescription("The Z scale of the item")
            .setParentSetting(offhand);

    public ViewModel() {
        super("ViewModel", Category.RENDER, "Changes the way items are rendered in your hand");

        INSTANCE = this;
    }

    @Listener
    public void onRenderItemPre(RenderItemEvent.Pre event) {
        if (event.getSide() == EnumHandSide.LEFT && offhand.getValue()) {
            // Translate offhand item according to x, y, and z settings
            GlStateManager.translate(offhandX.getValue(), offhandY.getValue(), offhandZ.getValue());

            // Scale offhand
            GlStateManager.scale(offhandScaleX.getValue(), offhandScaleY.getValue(), offhandScaleZ.getValue());
        }

        if (event.getSide() == EnumHandSide.RIGHT && main.getValue()) {
            // Translate main hand item according to x, y, and z settings
            GlStateManager.translate(mainX.getValue(), mainY.getValue(), mainZ.getValue());

            // Scale main hand
            GlStateManager.scale(mainScaleX.getValue(), mainScaleY.getValue(), mainScaleZ.getValue());
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
