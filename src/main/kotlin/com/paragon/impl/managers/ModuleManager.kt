package com.paragon.impl.managers

import com.paragon.impl.module.Module
import com.paragon.impl.module.hud.impl.*
import com.paragon.impl.module.hud.impl.graphs.GraphCPS
import com.paragon.impl.module.hud.impl.graphs.GraphFPS
import com.paragon.impl.module.hud.impl.graphs.GraphPing
import com.paragon.impl.module.hud.impl.graphs.GraphSpeed
import com.paragon.impl.module.client.*
import com.paragon.impl.module.combat.*
import com.paragon.impl.module.misc.*
import com.paragon.impl.module.movement.*
import com.paragon.impl.module.render.*
import net.minecraftforge.common.MinecraftForge
import java.util.*
import java.util.function.Predicate

class ModuleManager {

    val modules: ArrayList<Module>

    init {
        MinecraftForge.EVENT_BUS.register(this)

        modules = arrayListOf(
            // Combat
            Aura, AutoCrystal, BowBomb, BowRelease, Criticals, Offhand, Replenish, Surround, WebAura,

            // Movement
            AntiVoidinq, ElytraFlight, EntitySpeed, Flight, InventoryWalk, NoFall, NoSlow, ReverseStep, Sprint, Step, Strafe, Velocity,

            // Render
            AspectRatio, BlockHighlight, Breadcrumbs, BreakESP, Chams, ChinaHat, ClearChat, ESP, Fullbright, HandChams, HitColour, HoleESP, LogoutSpots, MobOwner, Nametags, NoRender, PhaseESP, PopChams, Shader, ShulkerViewer, SoundHighlight, SourceESP, StorageESP, Tracers, Trajectories, ViewClip, ViewModel, VoidinqESP, Xray,

            // Misc
            Alert, Announcer, AntiGhast, AntiHunger, AutoEZ, AutoLog, AutoTranslate, BookBot,

            // Movement
            AutoWalk, Blink, ChatModifications, ChorusControl, Cryptic, CustomWorld, DonkeyAlert, ExtraTab, FakePlayer, FastUse, Interact, Lawnmower, MiddleClick, NoGlobalSounds, NoRotate, Notifier, OnDeath, RotationLock, Spammer, TeleTofu, TimerModule, XCarry,

            // Client
            ClientFont, Colours, ClickGUI, DiscordRPC,

            // HUD
            Armour, ArrayListHUD, CombatInfo, Coordinates, Crystals, CustomText, Direction, FPS, GraphCPS, GraphFPS, GraphPing, GraphSpeed, HUD, HUDEditor, Inventory, Keystrokes, Notifications, Ping, PotionHUD, Speed, TabGui, TargetHUD, Totems, Watermark
        )

        modules.forEach { it.reflectSettings() }
    }

    fun getModulesThroughPredicate(predicate: Predicate<Module>): List<Module> = modules.filter { predicate.test(it) }

}