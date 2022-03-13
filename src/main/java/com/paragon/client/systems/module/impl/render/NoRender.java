package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.entity.RenderEatingEvent;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoRender extends Module {

    private final BooleanSetting fire = new BooleanSetting("Fire", "Cancel rendering the fire overlay", true);
    private final BooleanSetting water = new BooleanSetting("Water", "Cancel rendering the water overlay", true);
    private final BooleanSetting bossInfo = new BooleanSetting("Boss Info", "Cancel rendering the boss info overlay", true);
    private final BooleanSetting potions = new BooleanSetting("Potion Icons", "Cancel rendering the potion icons", false);
    private final BooleanSetting portal = new BooleanSetting("Portal", "Cancel rendering the portal effect", true);
    private final BooleanSetting bats = new BooleanSetting("Bats", "Cancel rendering bats", true);
    private final BooleanSetting eatingAnimation = new BooleanSetting("Eating animation", "Stops rendering the eating animation", false);

    public NoRender() {
        super("NoRender", ModuleCategory.RENDER, "Cancels rendering certain things");
        this.addSettings(fire, water, bossInfo, potions, portal, bats, eatingAnimation);
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent event) {
        if (nullCheck()) {
            return;
        }

        if (bossInfo.isEnabled() && event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO) {
            event.setCanceled(true);
        }

        if (potions.isEnabled() && event.getType() == RenderGameOverlayEvent.ElementType.POTION_ICONS) {
            event.setCanceled(true);
        }

        if (portal.isEnabled() && event.getType() == RenderGameOverlayEvent.ElementType.PORTAL) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderLivingEntity(RenderLivingEvent.Pre<?> event) {
        if (nullCheck()) {
            return;
        }

        if (bats.isEnabled() && event.getEntity() instanceof EntityBat) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundAtEntityEvent event) {
        if(nullCheck()) return;
        if (bats.isEnabled() && event.getSound().equals(SoundEvents.ENTITY_BAT_AMBIENT) || event.getSound().equals(SoundEvents.ENTITY_BAT_DEATH) || event.getSound().equals(SoundEvents.ENTITY_BAT_HURT) || event.getSound().equals(SoundEvents.ENTITY_BAT_LOOP) || event.getSound().equals(SoundEvents.ENTITY_BAT_TAKEOFF)) {
            event.setVolume(0.0f);
            event.setPitch(0.0f);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockOverlay(RenderBlockOverlayEvent event) {
        if (fire.isEnabled() && event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.FIRE) {
            event.setCanceled(true);
        }

        if (water.isEnabled() && event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.WATER) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void onRenderEating(RenderEatingEvent event) {
        if (eatingAnimation.isEnabled()) {
            event.cancel();
        }
    }

}
