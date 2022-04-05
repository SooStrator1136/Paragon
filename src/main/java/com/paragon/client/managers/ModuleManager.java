package com.paragon.client.managers;

import com.paragon.Paragon;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.hud.impl.*;
import com.paragon.client.systems.module.impl.client.*;
import com.paragon.client.systems.module.impl.combat.*;
import com.paragon.client.systems.module.impl.misc.*;
import com.paragon.client.systems.module.impl.movement.*;
import com.paragon.client.systems.module.impl.render.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Wolfsurge
 */
public class ModuleManager {

    private final List<Module> modules;

    public ModuleManager() {
        MinecraftForge.EVENT_BUS.register(this);

        Paragon.INSTANCE.getLogger().info("Initialising Module Manager");

        modules = Arrays.asList(
                new Aura(),
                new AutoCrystal(),
                new AutoCrystalRewrite(),
                new Blink(),
                new Offhand(),

                new ElytraFlight(),
                new NoSlow(),
                new ReverseStep(),
                new Sprint(),
                new Step(),
                new Velocity(),

                new BlockHighlight(),
                new Breadcrumbs(),
                new ESP(),
                new Fullbright(),
                new HoleESP(),
                new Nametags(),
                new NoRender(),
                new Tracers(),
                new ViewModel(),

                new AutoEZ(),
                new AutoLog(),
                new ChatModifications(),
                new CustomWorld(),
                new FakePlayer(),
                new FastUse(),
                new MiddleClick(),
                new NoRotate(),
                new Notifier(),
                new OnDeath(),
                new XCarry(),

                new ClientFont(),
                new Colours(),
                new GUI(),
                new HUD(),

                new Armour(),
                new HArrayList(),
                new Coordinates(),
                new FPS(),
                new Totems(),
                new Watermark()
        );

        modules.forEach(module -> {
            // Load config
            Paragon.INSTANCE.getStorageManager().loadModuleConfiguration(module);
        });
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) {
            if (!(Keyboard.getEventKey() > 1)) {
                return;
            }

            getModules().forEach(module -> {
                if (module.getKeyCode().getKeyCode() == Keyboard.getEventKey()) {
                    module.toggle();
                }
            });
        }
    }

    /**
     * Gets a list of modules
     * @return The modules
     */
    public List<Module> getModules() {
        return modules;
    }

    /**
     * Gets the modules in a category
     * @param moduleCategory The module category to get modules in
     * @return The modules in the given category
     */
    public List<Module> getModulesInCategory(ModuleCategory moduleCategory) {
        List<Module> modulesInCategory = new ArrayList<>();

        getModules().forEach(module -> {
            if (module.getCategory() == moduleCategory) {
                modulesInCategory.add(module);
            }
        });

        return modulesInCategory;
    }

    /**
     * Gets all the HUD modules
     * @return The HUD modules
     */
    public List<HUDModule> getHUDModules() {
        List<HUDModule> hudModules = new ArrayList<>();

        getModules().forEach(module -> {
            if (module instanceof HUDModule) {
                hudModules.add((HUDModule) module);
            }
        });

        return hudModules;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        modules.forEach(module -> {
            if (module.isEnabled()) {
                module.onTick();
            }
        });
    }

    @SubscribeEvent
    public void onRender2D(RenderGameOverlayEvent event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            modules.forEach(module -> {
                if (module.isEnabled()) {
                    module.onRender2D();
                }
            });
        }
    }

    @SubscribeEvent
    public void onRender3D(RenderWorldLastEvent event) {
        modules.forEach(module -> {
            if (module.isEnabled()) {
                module.onRender3D();
            }
        });
    }

}
