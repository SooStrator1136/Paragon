package com.paragon.client.systems.module.impl.combat;

import com.paragon.Paragon;
import com.paragon.api.event.client.SettingUpdateEvent;
import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.player.RotationUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.asm.mixins.accessor.IPlayerControllerMP;
import com.paragon.client.managers.rotation.Rotate;
import com.paragon.client.managers.rotation.Rotation;
import com.paragon.client.managers.rotation.RotationPriority;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.impl.misc.AutoEZ;
import com.paragon.client.systems.module.settings.impl.*;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * somewhat bad autocrystal. I have looked at some other client's ACs whilst writing this, but it isn't really skidded (apart from damage calcs - gamesense).
 * @author Wolfsurge
 */
@SuppressWarnings("unchecked")
public class AutoCrystal extends Module {

    public static AutoCrystal INSTANCE;

    // Order of operations
    public final ModeSetting<Order> order = new ModeSetting<>("Order", "The order of operations", Order.PLACE_EXPLODE);
    public final BooleanSetting grouped = (BooleanSetting) new BooleanSetting("Grouped", "Immediately attack or place the crystal after calculating", true).setParentSetting(order);
    public final ModeSetting<Timing> timing = new ModeSetting<>("Timing", "When to perform actions", Timing.LINEAR);

    public final ModeSetting<Heuristic> heuristic = new ModeSetting<>("Heuristic", "The way to calculate damage", Heuristic.MINIMAX);

    // Targeting settings
    public final BooleanSetting targeting = new BooleanSetting("Targeting" , "Settings for targeting players", true);
    public final ModeSetting<TargetPriority> targetPriority = (ModeSetting<TargetPriority>) new ModeSetting<>("Priority", "The way to sort possible targets", TargetPriority.DISTANCE).setParentSetting(targeting);
    public final BooleanSetting targetFriends = (BooleanSetting) new BooleanSetting("Friends", "Target friends", false).setParentSetting(targeting);
    public final NumberSetting targetRange = (NumberSetting) new NumberSetting("Range", "The range to target players", 10, 1, 15, 1).setParentSetting(targeting);

    // Place settings
    public final BooleanSetting place = new BooleanSetting("Place", "Automatically place crystals", true);
    public final ModeSetting<When> placeWhen = (ModeSetting<When>) new ModeSetting<>("When", "When to place", When.HOLDING).setParentSetting(place);
    public final BooleanSetting placeWhenSwitchBack = (BooleanSetting) new BooleanSetting("Switch Back", "Switch back to your original item", true).setParentSetting(place).setVisiblity(() -> placeWhen.getCurrentMode().equals(When.SWITCH) || placeWhen.getCurrentMode().equals(When.SILENT_SWITCH));
    public final NumberSetting placeRange = (NumberSetting) new NumberSetting("Range", "The range to place", 5, 1, 7, 1).setParentSetting(place);
    public final NumberSetting placeDelay = (NumberSetting) new NumberSetting("Delay", "The delay between placing crystals", 10, 0, 500, 1).setParentSetting(place);
    public final ModeSetting<Rotate> placeRotate = (ModeSetting<Rotate>) new ModeSetting<>("Rotate", "Rotate to the position you are placing at", Rotate.PACKET).setParentSetting(place);
    public final BooleanSetting placeRotateBack = (BooleanSetting) new BooleanSetting("Rotate Back", "Rotate back to your original rotation", true).setParentSetting(place).setVisiblity(() -> !placeRotate.getCurrentMode().equals(Rotate.NONE));
    public final BooleanSetting placeRaytrace = (BooleanSetting) new BooleanSetting("Raytrace", "Checks if you can raytrace to the position", true).setParentSetting(place);
    public final BooleanSetting multiplace = (BooleanSetting) new BooleanSetting("Multiplace", "Place multiple crystals", false).setParentSetting(place);
    public final NumberSetting placeMinDamage = (NumberSetting) new NumberSetting("Min Damage", "The minimum amount of damage to do to the target", 4, 0, 36, 1).setParentSetting(place);
    public final NumberSetting placeMaxLocal = (NumberSetting) new NumberSetting("Max Local Damage", "The minimum amount of damage to inflict upon yourself", 8, 0, 36, 1).setParentSetting(place);
    public final BooleanSetting placePacket = (BooleanSetting) new BooleanSetting("Packet", "Place with only a packet", false).setParentSetting(place);
    public final ModeSetting<Swing> placeSwing = (ModeSetting<Swing>) new ModeSetting<>("Swing", "Swing when placing a crystal", Swing.MAIN_HAND).setParentSetting(place);

    // Explode settings
    public final BooleanSetting explode = new BooleanSetting("Explode", "Automatically explode crystals", true);
    public final NumberSetting explodeRange = (NumberSetting) new NumberSetting("Range", "The range to explode crystals", 5, 1, 7, 1).setParentSetting(explode);
    public final NumberSetting explodeDelay = (NumberSetting) new NumberSetting("Delay", "The delay between exploding crystals", 10, 0, 500, 1).setParentSetting(explode);
    public final ModeSetting<ExplodeFilter> explodeFilter = (ModeSetting<ExplodeFilter>) new ModeSetting<>("Filter", "What crystals to explode", ExplodeFilter.SMART).setParentSetting(explode);
    public final BooleanSetting inhibit = (BooleanSetting) new BooleanSetting("Inhibit", "Prevent excessive amounts of attacks on crystals", true).setParentSetting(explode);
    public final NumberSetting inhibitMax = (NumberSetting) new NumberSetting("Inhibit Max", "When to start ignoring the crystals", 5, 1, 10, 1).setParentSetting(explode).setVisiblity(inhibit::isEnabled);
    public final NumberSetting explodeTicksExisted = (NumberSetting) new NumberSetting("Ticks Existed", "Check the amount of ticks the crystal has existed before exploding", 0, 0, 5, 1).setParentSetting(explode);
    public final BooleanSetting explodeRaytrace = (BooleanSetting) new BooleanSetting("Raytrace", "Checks that you can raytrace to the crystal", false).setParentSetting(explode);
    public final ModeSetting<Rotate> explodeRotate = (ModeSetting<Rotate>) new ModeSetting<>("Rotate", "How to rotate to the crystal", Rotate.PACKET).setParentSetting(explode);
    public final BooleanSetting explodeRotateBack = (BooleanSetting) new BooleanSetting("Rotate Back", "Rotate back to your original rotation", true).setParentSetting(explode).setVisiblity(() -> !explodeRotate.getCurrentMode().equals(Rotate.NONE));
    public final ModeSetting<AntiWeakness> antiWeakness = (ModeSetting<AntiWeakness>) new ModeSetting<>("Anti Weakness", "If you have the weakness effect, you will still be able to explode crystals", AntiWeakness.SWITCH).setParentSetting(explode);
    public final BooleanSetting strictInventory = (BooleanSetting) new BooleanSetting("Strict Inventory", "Fake opening your inventory when you switch", true).setParentSetting(explode).setVisiblity(() -> !antiWeakness.getCurrentMode().equals(AntiWeakness.OFF));
    public final BooleanSetting packetExplode = (BooleanSetting) new BooleanSetting("Packet", "Explode crystals with a packet only", false).setParentSetting(explode);
    public final ModeSetting<Swing> explodeSwing = (ModeSetting<Swing>) new ModeSetting<>("Swing", "How to swing your hand", Swing.BOTH).setParentSetting(explode);
    public final NumberSetting explodeMinDamage = (NumberSetting) new NumberSetting("Min Damage", "The minimum amount of damage to do to the target", 4, 0, 36, 1).setParentSetting(explode).setVisiblity(() -> explodeFilter.getCurrentMode().equals(ExplodeFilter.SMART) || explodeFilter.getCurrentMode().equals(ExplodeFilter.SELF_SMART));
    public final NumberSetting explodeMaxLocal = (NumberSetting) new NumberSetting("Max Local Damage", "The minimum amount of damage to inflict upon yourself", 8, 0, 36, 1).setParentSetting(explode).setVisiblity(() -> explodeFilter.getCurrentMode().equals(ExplodeFilter.SMART) || explodeFilter.getCurrentMode().equals(ExplodeFilter.SELF_SMART));
    public final ModeSetting<SetDead> explodeSetDead = (ModeSetting<SetDead>) new ModeSetting<>("Set Dead", "Set the crystals alive status to dead", SetDead.ATTACK).setParentSetting(explode);

    // Override settings
    public final BooleanSetting override = new BooleanSetting("Override", "Override minimum damage when certain things happen", true);
    public final BooleanSetting overrideHealth = (BooleanSetting) new BooleanSetting("Health", "Override if the target's health is below a value", true).setParentSetting(override);
    public final NumberSetting overrideHealthValue = (NumberSetting) new NumberSetting("Override Health", "If the targets health is this value or below, ignore minimum damage", 10, 0, 36, 1).setParentSetting(override).setVisiblity(() -> override.isEnabled());
    public final BooleanSetting overrideTotalArmour = (BooleanSetting) new BooleanSetting("Armour", "Override if the target's total armour durability is below a certain value", true).setParentSetting(override);
    public final NumberSetting overrideTotalArmourValue = (NumberSetting) new NumberSetting("Armour Value", "The value which we will start to override at (in %)", 10, 0, 100, 1).setParentSetting(override);
    public final KeybindSetting forceOverride = (KeybindSetting) new KeybindSetting("Force Override", "Force override when you press a key", Keyboard.KEY_NONE).setParentSetting(override);
    public final BooleanSetting ignoreInhibit = (BooleanSetting) new BooleanSetting("Ignore Inhibit", "Do not inhibit if we are overriding", true).setParentSetting(override).setVisiblity(inhibit::isEnabled);

    // Pause settings
    public final BooleanSetting pause = new BooleanSetting("Pause", "Pause if certain things are happening", true);
    public final BooleanSetting pauseEating = (BooleanSetting) new BooleanSetting("Eating", "Pause when eating", true).setParentSetting(pause);
    public final BooleanSetting pauseDrinking = (BooleanSetting) new BooleanSetting("Drinking", "Pause when drinking", true).setParentSetting(pause);
    public final BooleanSetting pauseHealth = (BooleanSetting) new BooleanSetting("Health", "Pause when your health is below a specified value", true).setParentSetting(pause);
    public final NumberSetting pauseHealthValue = (NumberSetting) new NumberSetting("Health Value", "The health to pause at", 10, 1, 20, 1).setParentSetting(pause).setVisiblity(pauseHealth::isEnabled);

    // Render settings
    public final BooleanSetting render = new BooleanSetting("Render", "Render the placement", true);
    public final ModeSetting<Render> renderMode = (ModeSetting<Render>) new ModeSetting<>("Mode", "How to render placement", Render.BOTH).setParentSetting(render);
    public final NumberSetting renderOutlineWidth = (NumberSetting) new NumberSetting("Outline Width", "The width of the lines", 0.5f, 0.1f, 2, 0.1f).setParentSetting(render);
    public final ColourSetting renderColour = (ColourSetting) new ColourSetting("Colour", "The colour of the render", new Color(185, 19, 255)).setParentSetting(render);

    // The current player we are targeting
    private EntityPlayer currentTarget;

    // The current crystal we are targeting
    private Crystal currentCrystal;

    // The current position we are placing at
    private CrystalPosition currentPlacement;

    // Exists purely to stop the flickering in the HUD info
    private CrystalPosition backlogPlacement;

    // Timers
    private final Timer explodeTimer = new Timer();
    private final Timer placeTimer = new Timer();

    // List of crystals we have placed
    private final List<BlockPos> selfPlacedCrystals = new ArrayList<>();

    // The current action we are performing
    private ActionState currentActionState = ActionState.PLACING;

    // Map of crystals we have attacked. Key is ID, Value is the amount of times we have attacked it
    private final Map<Integer, Integer> inhibitMap = new HashMap<>();

    public AutoCrystal() {
        super("AutoCrystal", ModuleCategory.COMBAT, "Automatically places and explodes crystals");
        this.addSettings(order, heuristic, timing, place, explode, targeting, override, pause, render);

        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        reset();
    }

    @Listener
    public void onSettingUpdate(SettingUpdateEvent event) {
        if (event.getSetting() == explodeFilter || event.getSetting() == explodeRotate || event.getSetting() == placeRotate) {
            reset();
        }
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        if (pause.isEnabled()) {
            // Pause if we are at a low health
            if (pauseHealth.isEnabled() && mc.player.getHealth() <= pauseHealthValue.getValue()) {
                return;
            }

            // Pause if we are eating
            if (pauseEating.isEnabled() && PlayerUtil.isPlayerEating()) {
                return;
            }

            // Pause if we are drinking
            if (pauseDrinking.isEnabled() && PlayerUtil.isPlayerDrinking()) {
                return;
            }
        }

        // Set our target
        currentTarget = getCurrentTarget();

        // Don't do anything if we don't have a target
        if (currentTarget == null) {
            return;
        }

        // Get overriding state
        // Called once because otherwise we do the same logic several times
        boolean overriding = isOverriding(currentTarget);

        // Add target to AutoEZ list
        AutoEZ.addTarget(currentTarget.getName());

        switch (order.getCurrentMode()) {
            case PLACE_EXPLODE:
                if (!timing.getCurrentMode().equals(Timing.SEQUENTIAL) || currentActionState.equals(ActionState.PLACING)) {
                    // Find placement
                    currentPlacement = findBestPosition(overriding);

                    if (currentPlacement != null && grouped.isEnabled()) {
                        placeSearchedPosition();
                    }
                }

                if (!timing.getCurrentMode().equals(Timing.SEQUENTIAL) || currentActionState.equals(ActionState.EXPLODING)) {
                    // Find crystal
                    currentCrystal = findBestCrystal(overriding);

                    if (currentCrystal != null && grouped.isEnabled()) {
                        explodeSearchedCrystal();
                    }
                }

                if (currentPlacement != null && !grouped.isEnabled()) {
                    placeSearchedPosition();
                }

                if (currentCrystal != null && !grouped.isEnabled()) {
                    explodeSearchedCrystal();
                }

                break;

            case EXPLODE_PLACE:
                if (!timing.getCurrentMode().equals(Timing.SEQUENTIAL) || currentActionState.equals(ActionState.EXPLODING)) {
                    // Find crystal
                    currentCrystal = findBestCrystal(overriding);

                    if (currentCrystal != null) {
                        explodeSearchedCrystal();
                    }
                }

                if (!timing.getCurrentMode().equals(Timing.SEQUENTIAL) || currentActionState.equals(ActionState.PLACING)) {
                    // Find placement
                    currentPlacement = findBestPosition(overriding);

                    if (currentPlacement != null) {
                        placeSearchedPosition();
                    }
                }

                if (currentCrystal != null && !grouped.isEnabled()) {
                    explodeSearchedCrystal();
                }

                if (currentPlacement != null && !grouped.isEnabled()) {
                    placeSearchedPosition();
                }

                break;

        }

        currentActionState = currentActionState.equals(ActionState.PLACING) ? ActionState.EXPLODING : ActionState.PLACING;
    }

    @Override
    public void onRender3D() {
        // Check we want to render
        if (render.isEnabled()) {
            // Check we have a placement and placing is enabled
            if (currentPlacement != null && place.isEnabled()) {
                // Render fill
                if (renderMode.getCurrentMode().equals(Render.FILL) || renderMode.getCurrentMode().equals(Render.BOTH)) {
                    RenderUtil.drawFilledBox(BlockUtil.getBlockBox(currentPlacement.getPosition()), ColourUtil.integrateAlpha(renderColour.getColour(), renderColour.getColour().getAlpha()));
                }

                // Render outline
                if (renderMode.getCurrentMode().equals(Render.OUTLINE) || renderMode.getCurrentMode().equals(Render.BOTH)) {
                    RenderUtil.drawBoundingBox(BlockUtil.getBlockBox(currentPlacement.getPosition()), renderOutlineWidth.getValue(), ColourUtil.integrateAlpha(renderColour.getColour().darker(), 255));
                }
            }
        }
    }

    @Listener
    public void onPacketReceive(PacketEvent.PreReceive event) {
        // If we are trying to use and item on a block
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            // Get packet
            CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) event.getPacket();

            // Check we are holding end crystals
            if (mc.player.getHeldItem(packet.getHand()).getItem() == Items.END_CRYSTAL) {
                // If we can place a crystal on that block, add it to our self placed crystals list
                selfPlacedCrystals.add(packet.getPos());
            }
        }

        // Check it's a sound packet
        if (event.getPacket() instanceof SPacketSoundEffect && explodeSetDead.getCurrentMode().equals(SetDead.SOUND)) {
            // Get packet
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();

            // Check it's an explosion sound
            if (packet.getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE) && packet.getCategory().equals(SoundCategory.BLOCKS)) {
                // Iterate through loaded entities
                for (Entity entity : mc.world.loadedEntityList) {
                    // If the entity isn't an ender crystal, ignore
                    if (!(entity instanceof EntityEnderCrystal)) {
                        continue;
                    }

                    // If the entity is dead, ignore
                    if (entity.isDead) {
                        continue;
                    }

                    // If the crystal is close to the explosion sound origin, set the crystals state to dead
                    if (entity.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6) {
                        entity.setDead();
                    }
                }
            }
        }
    }

    /**
     * Gets the best player to target
     * @return The best player to target
     */
    public EntityPlayer getCurrentTarget() {
        // All valid targets
        List<EntityPlayer> validTargets = new ArrayList<>();

        // Iterate through loaded entities
        for (Entity entity : mc.world.loadedEntityList) {
            // Check it's a player that isn't us
            if (entity instanceof EntityOtherPlayerMP) {
                // If the player is dead, ignore
                if (entity.isDead || ((EntityOtherPlayerMP) entity).getHealth() <= 0) {
                    continue;
                }

                // Get player
                EntityPlayer entityPlayer = (EntityPlayer) entity;

                // If it's too far away, ignore
                if (EntityUtil.isTooFarAwayFromSelf(entityPlayer, targetRange.getValue())) {
                    continue;
                }

                // If it's a friend, and we don't want to target friends, ignore
                if (!targetFriends.isEnabled()) {
                    if (Paragon.INSTANCE.getSocialManager().isFriend(entityPlayer.getName())) {
                        continue;
                    }
                }

                // Add to valid targets list
                validTargets.add(entityPlayer);
            }
        }

        // Return null if there are no valid targets
        if (validTargets.isEmpty()) {
            return null;
        }

        // Sort by priority
        switch (targetPriority.getCurrentMode()) {
            // Sort by distance
            case DISTANCE:
                validTargets.sort(Comparator.comparingDouble(target -> mc.player.getDistance(target)));
                break;

            // Sort by health
            case HEALTH:
                validTargets.sort(Comparator.comparingDouble(EntityLivingBase::getHealth));
                break;

            // Sort by total armour value
            case ARMOUR:
                validTargets.sort(Comparator.comparingDouble(target -> {
                    float totalArmour = 0;

                    // Iterate through target's armour slots
                    for (ItemStack armour : target.getArmorInventoryList()) {
                        // Don't do anything if they don't have an item in the slot
                        if (armour.isEmpty()) {
                            continue;
                        }

                        // Add item damage to total
                        totalArmour += armour.getItemDamage();
                    }

                    return totalArmour;
                }));

                break;
        }

        return validTargets.get(0);
    }

    /**
     * Explodes the searched crystal
     */
    public void explodeSearchedCrystal() {
        // Check we want to explode
        if (explode.isEnabled()) {
            // Check the timer has passed the required value
            if (!explodeTimer.hasMSPassed((long) explodeDelay.getValue())) {
                return;
            }

            // Get our current slot so we can switch back
            int antiWeaknessSlot = mc.player.inventory.currentItem;

            // Check we want to apply anti weakness
            if (!antiWeakness.getCurrentMode().equals(AntiWeakness.OFF)) {
                // Check we have the weakness effect
                if (mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                    // If we want to fake opening our inventory, send the opening inventory packet
                    if (strictInventory.isEnabled()) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY));
                    }

                    // Get the best sword
                    int hotbarSwordSlot = InventoryUtil.getItemInHotbar(Items.DIAMOND_SWORD);

                    // If we have found a sword, switch to it
                    if (hotbarSwordSlot != -1) {
                        InventoryUtil.switchToSlot(hotbarSwordSlot, antiWeakness.getCurrentMode().equals(AntiWeakness.SILENT));
                    }
                }
            }

            // Get our original rotation before rotating to the crystal
            Vec2f originalPlayerRotation = new Vec2f(mc.player.rotationYaw, mc.player.rotationPitch);

            // Check we want to rotate
            if (!explodeRotate.getCurrentMode().equals(Rotate.NONE)) {
                // Get rotation
                Vec2f rotationVec = RotationUtil.getRotationToVec3d(new Vec3d(currentCrystal.getCrystal().posX, currentCrystal.getCrystal().posY + 1, currentCrystal.getCrystal().posZ));

                Rotation rotation = new Rotation(rotationVec.x, rotationVec.y, explodeRotate.getCurrentMode(), RotationPriority.HIGHEST);

                // Send rotation
                Paragon.INSTANCE.getRotationManager().addRotation(rotation);
            }

            if (packetExplode.isEnabled()) {
                // Explode with a packet
                mc.player.connection.sendPacket(new CPacketUseEntity(currentCrystal.getCrystal()));
            } else {
                // Attack crystal
                mc.playerController.attackEntity(mc.player, currentCrystal.getCrystal());
            }

            // If we want to set the crystal to dead as soon as we attack, do that
            if (explodeSetDead.getCurrentMode().equals(SetDead.ATTACK)) {
                currentCrystal.getCrystal().setDead();
            }

            // Remove it from our self placed crystals
            selfPlacedCrystals.remove(currentCrystal.getCrystal().getPosition());

            // Swing our arm
            swing(explodeSwing.getCurrentMode());

            // Rotate back to our original rotation
            if (!explodeRotate.getCurrentMode().equals(Rotate.NONE) && explodeRotateBack.isEnabled()) {
                Rotation rotation = new Rotation(originalPlayerRotation.x, originalPlayerRotation.y, explodeRotate.getCurrentMode(), RotationPriority.HIGH);

                // Send rotation
                Paragon.INSTANCE.getRotationManager().addRotation(rotation);
            }

            // Check we want to switch
            if (!antiWeakness.getCurrentMode().equals(AntiWeakness.OFF)) {
                // Check we have weakness
                if (mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                    // Fake opening inventory
                    if (strictInventory.isEnabled()) {
                        mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
                    }

                    // Switch to slot
                    if (antiWeaknessSlot != -1) {
                        InventoryUtil.switchToSlot(antiWeaknessSlot, antiWeakness.getCurrentMode().equals(AntiWeakness.SILENT));
                    }
                }
            }

            explodeTimer.reset();
        }
    }

    /**
     * Places a crystal at the searched position
     */
    public void placeSearchedPosition() {
        // Check the place timer has passed the required time
        if (!placeTimer.hasMSPassed((long) placeDelay.getValue())) {
            return;
        }

        boolean hasSwitched = false;
        int oldSlot = mc.player.inventory.currentItem;

        switch (placeWhen.getCurrentMode()) {
            case HOLDING:
                hasSwitched = InventoryUtil.isHolding(Items.END_CRYSTAL);
                break;
            case SWITCH:
                int crystalSlot = InventoryUtil.getItemInHotbar(Items.END_CRYSTAL);

                if (crystalSlot == -1) {
                    break;
                } else {
                    InventoryUtil.switchToSlot(crystalSlot, false);
                    hasSwitched = true;
                }

                break;
            case SILENT_SWITCH:
                int silentCrystalSlot = InventoryUtil.getItemInHotbar(Items.END_CRYSTAL);

                if (silentCrystalSlot == -1) {
                    break;
                } else {
                    InventoryUtil.switchToSlot(silentCrystalSlot, true);
                    hasSwitched = true;
                }

                break;
        }

        if (!hasSwitched) {
            return;
        }

        // Get our current rotation
        Vec2f originalRotation = new Vec2f(mc.player.rotationYaw, mc.player.rotationPitch);

        // Check we want to rotate
        if (!placeRotate.getCurrentMode().equals(Rotate.NONE)) {
            // Get rotation
            Vec2f placeRotation = RotationUtil.getRotationToBlockPos(currentPlacement.getPosition());

            Rotation rotation = new Rotation(placeRotation.x, placeRotation.y, placeRotate.getCurrentMode(), RotationPriority.HIGHEST);
            Paragon.INSTANCE.getRotationManager().addRotation(rotation);
        }

        // Let's call this, it fixes the packet place bug, and it shouldn't do anything bad afaik.
        ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();

        if (placePacket.isEnabled()) {
            // Send place packet
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(currentPlacement.getPosition(), currentPlacement.getFacing(), placeWhen.getCurrentMode().equals(When.HOLDING) ? InventoryUtil.getHandHolding(Items.END_CRYSTAL) : EnumHand.MAIN_HAND, (float) currentPlacement.facingVec.x, (float) currentPlacement.facingVec.y, (float) currentPlacement.facingVec.z));
        } else {
            // Place crystal
            mc.playerController.processRightClickBlock(mc.player, mc.world, currentPlacement.getPosition(), currentPlacement.getFacing(), new Vec3d(currentPlacement.getFacing().getDirectionVec()), placeWhen.getCurrentMode().equals(When.HOLDING) ? InventoryUtil.getHandHolding(Items.END_CRYSTAL) : EnumHand.MAIN_HAND);
        }

        // Swing arm
        swing(placeSwing.getCurrentMode());

        // Add position to our self placed crystals
        selfPlacedCrystals.add(currentPlacement.getPosition().up());

        // Check we want to rotate back
        if (!placeRotate.getCurrentMode().equals(Rotate.NONE) && placeRotateBack.isEnabled()) {
            // Rotate back
            Rotation rotation = new Rotation(originalRotation.x, originalRotation.y, placeRotate.getCurrentMode(), RotationPriority.HIGH);
            Paragon.INSTANCE.getRotationManager().addRotation(rotation);
        }

        if (placeWhenSwitchBack.isEnabled()) {
            switch (placeWhen.getCurrentMode()) {
                case SWITCH:
                    InventoryUtil.switchToSlot(oldSlot, false);
                    break;
                case SILENT_SWITCH:
                    InventoryUtil.switchToSlot(oldSlot, true);
                    break;
            }
        }

        placeTimer.reset();
    }

    /**
     * Finds the best crystal to attack
     * @return The best crystal to attack
     */
    public Crystal findBestCrystal(boolean overriding) {
        // The best crystal (we will return this)
        Crystal crystal = null;

        // Check we want to explode
        if (explode.isEnabled()) {
            // Iterate through loaded entities
            for (Entity entity : mc.world.loadedEntityList) {
                // Check the entity is a crystal
                if (entity instanceof EntityEnderCrystal && !entity.isDead) {
                    // Check the crystal is old enough
                    if (entity.ticksExisted < explodeTicksExisted.getValue()) {
                        continue;
                    }

                    // We have already tried to explode this crystal
                    if (inhibitMap.containsKey(entity.getEntityId()) && inhibitMap.get(entity.getEntityId()) > inhibitMax.getValue()) {
                        if (!overriding && !ignoreInhibit.isEnabled()) {
                            continue;
                        }
                    } else {
                        inhibitMap.put(entity.getEntityId(), inhibitMap.getOrDefault(entity.getEntityId(), 0) + 1);
                    }

                    // If it's too far away, ignore
                    if (EntityUtil.isTooFarAwayFromSelf(entity, explodeRange.getValue())) {
                        continue;
                    }

                    // Check we can see the crystal
                    if (explodeRaytrace.isEnabled()) {
                        if (!mc.player.canEntityBeSeen(entity)) {
                            continue;
                        }
                    }

                    // Get the crystals position as a vector
                    Vec3d vec = new Vec3d(entity.posX, entity.posY, entity.posZ);

                    // Crystal
                    Crystal calculatedCrystal = new Crystal((EntityEnderCrystal) entity, calculateDamage(vec, currentTarget), calculateDamage(vec, mc.player));

                    // Position of crystal
                    CrystalPosition crystalPos = new CrystalPosition(calculatedCrystal.getCrystal().getPosition(), null, new Vec3d(0, 0, 0), calculatedCrystal.getTargetDamage(), calculatedCrystal.getSelfDamage());

                    // Check it meets our filter
                    switch (explodeFilter.getCurrentMode()) {
                        case SELF:
                            // Check it's in our self placed crystals
                            if (!selfPlacedCrystals.contains(crystalPos.getPosition())) {
                                continue;
                            }

                            break;

                        case SELF_SMART:
                            // Check it's in our self placed crystals
                            if (!selfPlacedCrystals.contains(crystalPos.getPosition())) {
                                continue;
                            }

                            // Check it meets our max local requirement
                            if (calculatedCrystal.getSelfDamage() > explodeMaxLocal.getValue()) {
                                continue;
                            }

                            // Check it meets our minimum damage requirement
                            if (calculatedCrystal.getTargetDamage() < explodeMinDamage.getValue() && !overriding) {
                                continue;
                            }

                            break;

                        case SMART:
                            // Check it meets our max local requirement
                            if (calculatedCrystal.getSelfDamage() > explodeMaxLocal.getValue()) {
                                continue;
                            }

                            // Check it meets our minimum damage requirement
                            if (calculatedCrystal.getTargetDamage() < explodeMinDamage.getValue() && !overriding) {
                                continue;
                            }

                            break;
                    }

                    // Set the crystal to this if: the current best crystal is null, or this crystal's target damage is higher than the last crystal checked
                    if (crystal == null || calculateHeuristic(calculatedCrystal.getSelfDamage(), calculatedCrystal.getTargetDamage(), mc.player.getDistance(entity), heuristic.getCurrentMode()) > calculateHeuristic(crystal.getSelfDamage(), crystal.getTargetDamage(), mc.player.getDistance(entity), heuristic.getCurrentMode())) {
                        crystal = calculatedCrystal;
                    }
                }
            }
        }

        return crystal;
    }

    /**
     * Finds the best position to place at
     * @return The best position to place at
     */
    public CrystalPosition findBestPosition(boolean overriding) {
        List<CrystalPosition> crystalPositions = new ArrayList<>();

        // Check we want to place
        if (place.isEnabled()) {

            // Iterate through blocks around us
            for (BlockPos pos : BlockUtil.getSphere(placeRange.getValue(), true)) {
                // Check we can place crystals
                if (!canPlaceCrystal(pos)) {
                    continue;
                }

                // Position we are placing at
                Vec3d placeVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

                // Position we will calculate damage at
                Vec3d damageVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);

                // Get the direction we want to face
                EnumFacing facing = EnumFacing.getDirectionFromEntityLiving(pos, mc.player);
                Vec3d facingVec = null;
                RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), mc.player.getPositionEyes(1).add(placeVec.x * placeRange.getValue(), placeVec.y * placeRange.getValue(), placeVec.z * placeRange.getValue()), false, false, true);;
                RayTraceResult laxResult = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1), new Vec3d(pos).add(0.5, 0.5, 0.5));

                // Check we hit a block
                if (laxResult != null && laxResult.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                    facing = laxResult.sideHit;

                    // We can place if we are at max height by doing this
                    if (pos.getY() >= (mc.world.getActualHeight() - 1)) {
                        facing = EnumFacing.DOWN;
                    }
                }

                // Get angles
                if (rayTraceResult != null && rayTraceResult.hitVec != null) {
                    facingVec = new Vec3d(rayTraceResult.hitVec.x - pos.getX(), rayTraceResult.hitVec.y - pos.getY(), rayTraceResult.hitVec.z - pos.getZ());
                }

                // Check we can see the position
                if (placeRaytrace.isEnabled()) {
                    if (!BlockUtil.canSeePos(pos)) {
                        continue;
                    }
                }

                // Create new crystal position
                CrystalPosition crystalPosition = new CrystalPosition(pos, facing, facingVec, calculateDamage(damageVec, currentTarget), calculateDamage(damageVec, mc.player));

                // Check it's below or equal to our maximum local damage requirement
                if (crystalPosition.getSelfDamage() > placeMaxLocal.getValue()) {
                    continue;
                }

                // Check we aren't overriding
                if (!overriding) {
                    // Check it's above or equal to our minimum damage requirement
                    if (crystalPosition.getTargetDamage() < placeMinDamage.getValue()) {
                        continue;
                    }
                }

                crystalPositions.add(crystalPosition);
            }
        }

        crystalPositions.sort(Comparator.comparingDouble(position -> calculateHeuristic(position, heuristic.getCurrentMode())));
        Collections.reverse(crystalPositions);

        if (!crystalPositions.isEmpty()) {
            backlogPlacement = crystalPositions.get(0);
            return crystalPositions.get(0);
        }

        return null;
    }

    public boolean isOverriding(EntityPlayer player) {
        if (override.isEnabled()) {
            if (overrideHealth.isEnabled()) {
                // Get total health
                if (EntityUtil.getEntityHealth(player) <= overrideHealthValue.getValue()) {
                    return true;
                }
            }

            if (forceOverride.getKeyCode() != 0) {
                return Keyboard.isKeyDown(forceOverride.getKeyCode());
            }

            if (overrideTotalArmour.isEnabled()) {
                // *Looked* at Cosmos for this, so thanks, just wanted to make sure I was doing this right :')

                float lowest = 100;

                // Iterate through target's armour
                for (ItemStack armourPiece : player.getArmorInventoryList()) {
                    // If it is an actual piece of armour
                    if (armourPiece != null && armourPiece.getItem() != Items.AIR) {
                        // Get durability
                        float durability = (armourPiece.getMaxDamage() - armourPiece.getItemDamage()) / (float) armourPiece.getMaxDamage() * 100;

                        // If it is less than the last lowest, set the lowest to this durability
                        if (durability < lowest) {
                            lowest = durability;
                        }
                    }
                }

                // We are overriding if the lowest durability is less or equal to the total armour value setting
                return lowest <= overrideTotalArmourValue.getValue();
            }
        }

        return false;
    }

    /**
     * Swings our hands
     * @param swing The hand to swing
     */
    public void swing(Swing swing) {
        switch (swing) {
            case MAIN_HAND:
                // Swing main hand
                mc.player.swingArm(EnumHand.MAIN_HAND);
                break;
            case OFFHAND:
                // Swing offhand
                mc.player.swingArm(EnumHand.OFF_HAND);
                break;
            case BOTH:
                // Swing both hands
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.swingArm(EnumHand.OFF_HAND);
                break;
            case PACKET:
                // Send a swing hand packet
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                break;
        }
    }

    /**
     * Calculates the heuristic
     * @param self The damage done to us
     * @param target The damage done to the target
     * @param distance The distance from the crystal
     * @param heuristic The heuristic type
     * @return The damage heuristic
     */
    public float calculateHeuristic(float self, float target, float distance, Heuristic heuristic) {
        switch (heuristic) {
            case DAMAGE:
                // Just target damage
                return target;
            case MINIMAX:
                // Target damage minus self damage
                return target - self;
            case UNIFORM:
                // Target damage minus self damage minus distance
                return target - self - distance;
        }

        return target;
    }

    /**
     * Calculates the heuristic based on a crystal position
     * @param crystal The crystal to calculate heuristic for
     * @param heuristic The heuristic type
     * @return The damage heuristic
     */
    public float calculateHeuristic(CrystalPosition crystal, Heuristic heuristic) {
        // Prevent NPE
        if (crystal == null) {
            return 0;
        }

        // Calculate and return heuristic
        return calculateHeuristic(crystal.getSelfDamage(), crystal.getTargetDamage(), (float) mc.player.getDistanceSq(crystal.getPosition()), heuristic);
    }

    /**
     * Checks if we can place a crystal on a block
     * @param pos The pos to check
     * @return Whether we can place a crystal on that block or not
     */
    public boolean canPlaceCrystal(BlockPos pos) {
        // Get block
        Block block = BlockUtil.getBlockAtPos(pos);

        // Check the block is obsidian or bedrock
        if (!(block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.BEDROCK))) {
            return false;
        }

        // Check the position above is an air block
        if (!mc.world.isAirBlock(pos.up()) || !mc.world.getBlockState(pos.up(2)).getMaterial().isReplaceable()) {
            return false;
        }

        // Check we aren't standing in the position
        if (mc.player.getPosition().equals(pos)) {
            return false;
        }

        // Iterate through entities in the block above
        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.up()))) {
            // If the entity is dead, or we aren't multiplacing, continue
            if (entity.isDead || !multiplace.isEnabled() && entity instanceof EntityEnderCrystal) {
                continue;
            }

            return false;
        }

        return true;
    }

    /**
     * Calculates the explosion damage based on a Vec3D
     * @param vec The vector to calculate damage from
     * @param entity The target
     * @return The damage done to the target
     */
    public float calculateDamage(Vec3d vec, EntityLivingBase entity) {
        float finalDamage = 1.0f;
        try {
            float doubleExplosionSize = 12.0F;
            double distancedSize = entity.getDistance(vec.x, vec.y, vec.z) / (double) doubleExplosionSize;
            double blockDensity = entity.world.getBlockDensity(new Vec3d(vec.x, vec.y, vec.z), entity.getEntityBoundingBox());
            double v = (1.0D - distancedSize) * blockDensity;
            float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));

            int diff = mc.world.getDifficulty().getId();
            finalDamage = getBlastReduction(entity, damage * (diff == 0 ? 0 : (diff == 2 ? 1 : (diff == 1 ? 0.5f : 1.5f))), new Explosion(mc.world, null, vec.x, vec.y, vec.z, 6F, false, true));
        } catch (NullPointerException ignored){
        }

        return finalDamage;
    }

    /**
     * Gets the blast reduction
     * @param entity The entity to calculate damage for
     * @param damage The original damage
     * @param explosion The explosion
     * @return The blast reduction
     */
    public float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

            int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            float f = MathHelper.clamp(k, 0.0F, 20.0F);
            damage *= 1.0F - f / 25.0F;

            if (entity.isPotionActive(Potion.getPotionById(11))) {
                damage = damage - (damage / 4);
            }

            damage = Math.max(damage, 0.0F);
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    /**
     * Resets the process
     */
    public void reset() {
        this.currentTarget = null;
        this.currentCrystal = null;
        this.currentPlacement = null;
        this.selfPlacedCrystals.clear();
        this.inhibitMap.clear();
    }

    @Override
    public String getArrayListInfo() {
        return (currentTarget == null ? " No Target" : " " + currentTarget.getName() +
                " DMG " + (!isOverriding(currentTarget) ? "" : "[OVERRIDING] ") +
                (backlogPlacement == null ? "No Placement" : Math.round(calculateHeuristic(backlogPlacement, heuristic.getCurrentMode()))));
    }

    public enum Order {
        /**
         * Place then explode
         */
        PLACE_EXPLODE,

        /**
         * Explode then place
         */
        EXPLODE_PLACE
    }

    public enum Timing {
        /**
         * Run actions one after another
         */
        LINEAR,

        /**
         * Run actions on different ticks
         */
        SEQUENTIAL
    }

    public enum ActionState {
        /**
         * About to explode crystals
         */
        EXPLODING,

        /**
         * About to place crystals
         */
        PLACING
    }

    public enum Heuristic {
        /**
         * Just target damage
         */
        DAMAGE,

        /**
         * Target damage minus self damage
         */
        MINIMAX,

        /**
         * Target damage minus self damage minus distance
         */
        UNIFORM
    }

    public enum TargetPriority {
        /**
         * Target closest to us
         */
        DISTANCE,

        /**
         * Target with the lowest health
         */
        HEALTH,

        /**
         * Target with the lowest total armour value
         */
        ARMOUR
    }

    public enum When {
        /**
         * Only place when holding crystals
         */
        HOLDING,

        /**
         * Switch to crystals
         */
        SWITCH,

        /**
         * Silent switch to crystals (with a packet)
         */
        SILENT_SWITCH
    }

    public enum ExplodeFilter {
        /**
         * Explode all crystals regardless of parameters
         */
        ALL,

        /**
         * Explode crystals if they fit our minimum damage and maximum local damage requirements
         */
        SMART,

        /**
         * Explode crystals only if we have placed them
         */
        SELF,

        /**
         * Explode crystals if they fit our minimum damage and maximum local damage requirements, and only we have placed them
         */
        SELF_SMART
    }

    public enum AntiWeakness {
        /**
         * Switch to sword
         */
        SWITCH,

        /**
         * Silent switch to sword
         */
        SILENT,

        /**
         * Don't switch at all
         */
        OFF
    }

    public enum SetDead {
        /**
         * Set crystal's alive status to dead when we attack it
         */
        ATTACK,

        /**
         * Set crystal's alive status to dead when the explosion sound plays
         */
        SOUND,

        /**
         * Don't modify when we set it's alive status
         */
        OFF
    }

    public enum Swing {
        /**
         * Swing main hand
         */
        MAIN_HAND,

        /**
         * Swing offhand
         */
        OFFHAND,

        /**
         * Swing both hands
         */
        BOTH,

        /**
         * Send swing animation packet
         */
        PACKET,

        /**
         * Don't swing
         */
        OFF
    }

    public enum Render {
        /**
         * Render outline
         */
        OUTLINE,

        /**
         * Render fill
         */
        FILL,

        /**
         * Render both
         */
        BOTH
    }

    static class Crystal {
        // The entity
        private final EntityEnderCrystal crystal;

        // The damage we do to the target
        private final float targetDamage;

        // The damage we do to us
        private final float selfDamage;

        public Crystal(EntityEnderCrystal crystal, float targetDamage, float selfDamage) {
            this.crystal = crystal;
            this.targetDamage = targetDamage;
            this.selfDamage = selfDamage;
        }

        /**
         * Gets the crystal
         * @return The crystal
         */
        public EntityEnderCrystal getCrystal() {
            return crystal;
        }

        /**
         * Gets the target damage
         * @return The target damage
         */
        public float getTargetDamage() {
            return targetDamage;
        }

        /**
         * Gets the self damage
         * @return The self damage
         */
        public float getSelfDamage() {
            return selfDamage;
        }
    }

    static class CrystalPosition {
        // The position we will place at
        private final BlockPos position;

        // The direction we want to face
        private final EnumFacing facing;

        // Rotation angles
        private final Vec3d facingVec;

        // The damage we do to the target
        private final float targetDamage;

        // The damage we do to us
        private final float selfDamage;

        public CrystalPosition(BlockPos position, EnumFacing facing, Vec3d facingVec, float targetDamage, float selfDamage) {
            this.position = position;
            this.facing = facing;
            this.facingVec = facingVec;
            this.targetDamage = targetDamage;
            this.selfDamage = selfDamage;
        }

        /**
         * Gets the position
         * @return The position
         */
        public BlockPos getPosition() {
            return position;
        }

        /**
         * Gets the direction we want to face in
         * @return The direction we want to face in
         */
        public EnumFacing getFacing() {
            return facing;
        }

        /**
         * Gets the target damage
         * @return The target damage
         */
        public float getTargetDamage() {
            return targetDamage;
        }

        /**
         * Gets the self damage
         * @return The self damage
         */
        public float getSelfDamage() {
            return selfDamage;
        }
    }
}