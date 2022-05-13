package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.entity.RenderEatingEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoRender extends Module {

    private final Setting<Boolean> fire = new Setting<>("Fire", true)
            .setDescription("Cancel rendering the fire overlay");

    private final Setting<Boolean> water = new Setting<>("Water", true)
            .setDescription("Cancel rendering the water overlay");

    private final Setting<Boolean> bossInfo = new Setting<>("Boss Info", true)
            .setDescription("Cancel rendering the boss info overlay");

    private final Setting<Boolean> potions = new Setting<>("Potion Icons", false)
            .setDescription("Cancel rendering the potion icons");

    private final Setting<Boolean> portal = new Setting<>("Portal", true)
            .setDescription("Cancel rendering the portal effect");

    private final Setting<Boolean> bats = new Setting<>("Bats", true)
            .setDescription("Cancel rendering bats");

    private final Setting<Boolean> eatingAnimation = new Setting<>("Eating animation", false)
            .setDescription("Stops rendering the eating animation");

    public NoRender() {
        super("NoRender", Category.RENDER, "Cancels rendering certain things");
        this.addSettings(fire, water, bossInfo, potions, portal, bats, eatingAnimation);
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Pre event) {
        if (nullCheck()) {
            return;
        }

        if (bossInfo.getValue() && event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO) {
            event.setCanceled(true);
        }

        if (potions.getValue() && event.getType() == RenderGameOverlayEvent.ElementType.POTION_ICONS) {
            event.setCanceled(true);
        }

        if (portal.getValue() && event.getType() == RenderGameOverlayEvent.ElementType.PORTAL) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderLivingEntity(RenderLivingEvent.Pre<?> event) {
        if (nullCheck()) {
            return;
        }

        if (bats.getValue() && event.getEntity() instanceof EntityBat) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundAtEntityEvent event) {
        if(nullCheck()) return;
        if (bats.getValue() && event.getSound().equals(SoundEvents.ENTITY_BAT_AMBIENT) || event.getSound().equals(SoundEvents.ENTITY_BAT_DEATH) || event.getSound().equals(SoundEvents.ENTITY_BAT_HURT) || event.getSound().equals(SoundEvents.ENTITY_BAT_LOOP) || event.getSound().equals(SoundEvents.ENTITY_BAT_TAKEOFF)) {
            event.setVolume(0.0f);
            event.setPitch(0.0f);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockOverlay(RenderBlockOverlayEvent event) {
        if (fire.getValue() && event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.FIRE) {
            event.setCanceled(true);
        }

        if (water.getValue() && event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.WATER) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void onRenderEating(RenderEatingEvent event) {
        if (eatingAnimation.getValue()) {
            event.cancel();
        }
    }

}
