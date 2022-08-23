package com.paragon.client.managers

import com.paragon.api.module.Module
import com.paragon.client.systems.module.hud.impl.*
import com.paragon.client.systems.module.hud.impl.graphs.GraphCPS
import com.paragon.client.systems.module.hud.impl.graphs.GraphFPS
import com.paragon.client.systems.module.hud.impl.graphs.GraphPing
import com.paragon.client.systems.module.hud.impl.graphs.GraphSpeed
import com.paragon.client.systems.module.impl.client.ClickGUI
import com.paragon.client.systems.module.impl.client.ClientFont
import com.paragon.client.systems.module.impl.client.Colours
import com.paragon.client.systems.module.impl.client.DiscordRPC
import com.paragon.client.systems.module.impl.combat.*
import com.paragon.client.systems.module.impl.misc.*
import com.paragon.client.systems.module.impl.movement.*
import com.paragon.client.systems.module.impl.render.*
import net.minecraftforge.common.MinecraftForge
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors

class ModuleManager {

    val modules: Array<Module>

    init {
        MinecraftForge.EVENT_BUS.register(this)

        modules = arrayOf(

            // Combat
            Aura,
            AutoCrystal,
            BowBomb,
            BowRelease,
            Criticals,
            Offhand,
            Replenish,
            Surround,
            WebAura,

            // Movement
            AntiVoidinq,
            ElytraFlight,
            EntitySpeed,
            Flight,
            InventoryWalk,
            NoFall,
            NoSlow,
            ReverseStep,
            Sprint,
            Step,
            Strafe,
            Velocity,

            // Render
            AspectRatio,
            BlockHighlight,
            Breadcrumbs,
            BreakESP,
            Chams,
            ChinaHat,
            ClearChat,
            ESP,
            Fullbright,
            HandChams,
            HitColour,
            HoleESP,
            MobOwner,
            Nametags,
            NoRender,
            PhaseESP,
            Shader,
            ShulkerViewer,
            SoundHighlight,
            SourceESP,
            StorageESP,
            Tracers,
            Trajectories,
            ViewClip,
            ViewModel,
            VoidinqESP,
            Xray,

            // Misc
            Alert,
            Announcer,
            AntiHunger,
            AutoEZ,
            AutoLog,
            AutoTranslate,
            BookBot,

            AutoWalk,
            Blink,
            ChatModifications,
            ChorusControl,
            Cryptic,
            CustomWorld,
            DonkeyAlert,
            ExtraTab,
            FakePlayer,
            FastUse,
            Interact,
            Lawnmower,
            MiddleClick,
            NoGlobalSounds,
            NoRotate,
            Notifier,
            OnDeath,
            RotationLock,
            Spammer,
            TeleTofu,
            TimerModule,
            XCarry,

            // Client
            ClientFont,
            Colours,
            ClickGUI,
            DiscordRPC,

            // HUD
            Armour,
            ArrayListHUD,
            CombatInfo,
            Coordinates,
            Crystals,
            CustomText,
            Direction,
            FPS,
            GraphCPS,
            GraphFPS,
            GraphPing,
            GraphSpeed,
            HUD,
            HUDEditor,
            Inventory,
            Keystrokes,
            Notifications,
            Ping,
            PotionHUD,
            Speed,
            TabGui,
            TargetHUD,
            Totems,
            Watermark
        )

        // I'M SORRY :SOB:
        // Kotlin objects do some funky bytecode stuff and the fields aren't initialized until after the
        // ctr is called, but if you end up making the whole client kotlin this will be temp
        // as there are clean ways to do settings using delegates like `val someNum by int("Num Setting", 0..50, 10)`
        modules.forEach(Module::reflectSettings)
    }

    fun getModulesThroughPredicate(predicate: Predicate<Module>): List<Module> = Arrays.stream(modules).filter(predicate).collect(Collectors.toList())

}