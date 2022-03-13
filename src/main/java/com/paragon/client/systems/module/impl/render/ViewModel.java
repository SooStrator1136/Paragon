package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.player.RenderItemEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHandSide;

public class ViewModel extends Module {

    // Main hand settings
    private final BooleanSetting main = new BooleanSetting("Main Hand", "Modify your main hand", true);
    private final NumberSetting mainX = (NumberSetting) new NumberSetting("X", "The x of the item", 0.19f, -2, 2, 0.01f).setParentSetting(main);
    private final NumberSetting mainY = (NumberSetting) new NumberSetting("Y", "The y of the item", -0.14f, -2, 2, 0.01f).setParentSetting(main);
    private final NumberSetting mainZ = (NumberSetting) new NumberSetting("Z", "The z of the item", -0.43f, -2, 2, 0.01f).setParentSetting(main);
    private final NumberSetting mainYaw = (NumberSetting) new NumberSetting("Yaw", "The yaw of the item", 0, -100, 100, 1).setParentSetting(main);
    private final NumberSetting mainPitch = (NumberSetting) new NumberSetting("Pitch", "The pitch of the item", 0, -100, 100, 1).setParentSetting(main);
    private final NumberSetting mainRoll = (NumberSetting) new NumberSetting("Roll", "The roll of the item", 0, -100, 100, 1).setParentSetting(main);

    // Offhand settings
    private final BooleanSetting offhand = new BooleanSetting("Offhand", "Modify your offhand", true);
    private final NumberSetting offhandX = (NumberSetting) new NumberSetting("X", "The x of the item", -0.19f, -2, 2, 0.01f).setParentSetting(offhand);
    private final NumberSetting offhandY = (NumberSetting) new NumberSetting("Y", "The y of the item", -0.14f, -2, 2, 0.01f).setParentSetting(offhand);
    private final NumberSetting offhandZ = (NumberSetting) new NumberSetting("Z", "The z of the item", -0.43f, -2, 2, 0.01f).setParentSetting(offhand);
    private final NumberSetting offhandYaw = (NumberSetting) new NumberSetting("Yaw", "The yaw of the item", 0, -100, 100, 1).setParentSetting(offhand);
    private final NumberSetting offhandPitch = (NumberSetting) new NumberSetting("Pitch", "The pitch of the item", 0, -100, 100, 1).setParentSetting(offhand);
    private final NumberSetting offhandRoll = (NumberSetting) new NumberSetting("Roll", "The roll of the item", 0, -100, 100, 1).setParentSetting(offhand);

    public ViewModel() {
        super("ViewModel", ModuleCategory.RENDER, "Changes the way items are rendered in your hand");
        this.addSettings(main, offhand);
    }

    @Listener
    public void onRenderItemPre(RenderItemEvent.Pre event) {
        if (event.getSide() == EnumHandSide.LEFT && offhand.isEnabled()) {
            // Translate offhand item according to x, y, and z settings
            GlStateManager.translate(offhandX.getValue(), offhandY.getValue(), offhandZ.getValue());
        }

        if (event.getSide() == EnumHandSide.RIGHT && main.isEnabled()) {
            // Translate main hand item according to x, y, and z settings
            GlStateManager.translate(mainX.getValue(), mainY.getValue(), mainZ.getValue());
        }
    }

    @Listener
    public void onRenderItemPost(RenderItemEvent.Post event) {
        if (event.getSide() == EnumHandSide.LEFT && offhand.isEnabled()) {
            // Rotate offhand item according to yaw, pitch, and roll settings
            GlStateManager.rotate(offhandYaw.getValue(), 0, 1, 0);
            GlStateManager.rotate(offhandPitch.getValue(), 1, 0, 0);
            GlStateManager.rotate(offhandRoll.getValue(), 0, 0, 1);
        }

        if (event.getSide() == EnumHandSide.LEFT && main.isEnabled()) {
            // Rotate main hand item according to yaw, pitch, and roll settings
            GlStateManager.rotate(mainYaw.getValue(), 0, 1, 0);
            GlStateManager.rotate(mainPitch.getValue(), 1, 0, 0);
            GlStateManager.rotate(mainRoll.getValue(), 0, 0, 1);
        }
    }

}
