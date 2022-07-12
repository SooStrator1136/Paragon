package com.paragon.client.managers;

import com.paragon.Paragon;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.client.systems.module.hud.HUDModule;
import com.paragon.client.systems.module.hud.impl.*;
import com.paragon.client.systems.module.impl.client.*;
import com.paragon.client.systems.module.impl.combat.*;
import com.paragon.client.systems.module.impl.misc.*;
import com.paragon.client.systems.module.impl.movement.*;
import com.paragon.client.systems.module.impl.render.*;
import net.minecraftforge.common.MinecraftForge;

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
                // Combat
                new Aura(),
                new AutoCrystal(),
                new AutoCrystalRewrite(),
                new BowRelease(),
                new Criticals(),
                new HoleFill(),
                new Offhand(),
                new Replenish(),
                new Surround(),

                // Movement
                new ElytraFlight(),
                new Flight(),
                new NoFall(),
                new NoSlow(),
                ReverseStep.INSTANCE,
                Sprint.INSTANCE,
                new Step(),
                new Velocity(),

                // Render
                new AspectRatio(),
                new BlockHighlight(),
                new Breadcrumbs(),
                new BreakESP(),
                new Chams(),
                ChinaHat.INSTANCE,
                new ClearChat(),
                new ESP(),
                Fullbright.INSTANCE,
                new HitColour(),
                new HoleESP(),
                new MobOwner(),
                new Nametags(),
                new NoRender(),
                new NoSwing(),
                new Shader(),
                new ShulkerViewer(),
                new SoundHighlight(),
                new StorageESP(),
                new Tracers(),
                new Trajectories(),
                new ViewClip(),
                new ViewModel(),
                new Xray(),

                // Misc
                new Announcer(),
                new AutoEZ(),
                new AutoLog(),
                AutoTranslate.INSTANCE,
                new AutoWalk(),
                new Blink(),
                new BuildHeight(),
                new ChatModifications(),
                Cryptic.INSTANCE,
                new CustomWorld(),
                new ExtraTab(),
                new FakePlayer(),
                new FastUse(),
                new LiquidInteract(),
                new MiddleClick(),
                new NoGlobalSounds(),
                new NoRotate(),
                new Notifier(),
                new NoTrace(),
                new OnDeath(),
                new RotationLock(),
                Spammer.INSTANCE,
                new TeleTofu(),
                new TimerModule(),
                new XCarry(),

                // Client
                new ClientFont(),
                new Colours(),
                new ClickGUI(),
                DiscordRPC.INSTANCE,

                // HUD
                new Armour(),
                new ArrayListHUD(),
                new CombatInfo(),
                new Coordinates(),
                new Crystals(),
                new Direction(),
                new FPS(),
                new HUD(),
                new HUDEditor(),
                new Inventory(),
                new Notifications(),
                new Ping(),
                new Speed(),
                new Totems(),
                new Watermark()
        );

        // IM SORRY :SOB:
        // Kotlin objects do some funky bytecode stuff and the fields aren't initialized until after the
        // ctr is called, but if you end up making the whole client kotlin this will be temp
        // as there are clean ways to do settings using delegates like `val someNum by int("Num Setting", 0..50, 10)`
        modules.forEach(Module::reflectSettings);
    }

    /**
     * Gets a list of modules
     *
     * @return The modules
     */
    public List<Module> getModules() {
        return modules;
    }

    /**
     * Gets the modules in a category
     *
     * @param moduleCategory The module category to get modules in
     * @return The modules in the given category
     */
    public List<Module> getModulesInCategory(Category moduleCategory) {
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
     *
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

}
