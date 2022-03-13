package com.paragon.client.systems.module.impl.combat;

import com.paragon.Paragon;
import com.paragon.api.event.network.PacketEvent;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.player.EntityFakePlayer;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.player.RotationUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.*;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.awt.*;
import java.util.*;

/**
 * @author Wolfsurge, with damage calculation utils from GameSense
 */
@SuppressWarnings("unchecked")
public class AutoCrystal extends Module {

    private final ModeSetting<ActionPriority> actionPriority = new ModeSetting<>("Action Priority", "Whether to place or explode first", ActionPriority.PLACE_EXPLODE);

    /* Targeting Settings */
    private final BooleanSetting targeting = new BooleanSetting("Targeting", "Settings for targeting players", true);
    private final ModeSetting<TargetPriority> targetPriority = (ModeSetting<TargetPriority>) new ModeSetting<>("Priority", "What players to prioritise", TargetPriority.DISTANCE).setParentSetting(targeting);
    private final NumberSetting targetingRange = (NumberSetting) new NumberSetting("Range", "The range to search for targets", 7, 2, 15, 1).setParentSetting(targeting);
    private final BooleanSetting targetFriends = (BooleanSetting) new BooleanSetting("Target Friends", "Target friends as well as enemies", false).setParentSetting(targeting);

    /* Place Settings */
    private final BooleanSetting place = new BooleanSetting("Place", "Automatically place crystals", true);
    private final ModeSetting<PlaceWhen> placeWhen = (ModeSetting<PlaceWhen>) new ModeSetting<>("When", "When to place crystals", PlaceWhen.HOLDING).setParentSetting(place);
    private final NumberSetting placeRange = (NumberSetting) new NumberSetting("Range", "The maximum range to place crystals", 5, 1, 7, 1).setParentSetting(place);
    private final ModeSetting<Rotate> placeRotate = (ModeSetting<Rotate>) new ModeSetting<>("Rotate", "How to rotate to the position", Rotate.PACKET).setParentSetting(place);
    private final BooleanSetting packetPlace = (BooleanSetting) new BooleanSetting("Packet Place", "Place by sending a packet", false).setParentSetting(place);

    /* Explode Settings */
    private final BooleanSetting explode = new BooleanSetting("Explode", "Automatically explode crystals", true);
    private final ModeSetting<ExplodeSwing> explodeSwing = (ModeSetting<ExplodeSwing>) new ModeSetting<>("Swing", "How to swing your arm when breaking the crystal", ExplodeSwing.PACKET).setParentSetting(explode);
    private final ModeSetting<ExplodeFilter> explodeFilter = (ModeSetting<ExplodeFilter>) new ModeSetting<>("Filter", "What crystal filter to apply", ExplodeFilter.SMART).setParentSetting(explode);
    private final NumberSetting explodeRange = (NumberSetting) new NumberSetting("Range", "The maximum range to explode crystals", 5, 1, 7, 1).setParentSetting(explode);
    private final ModeSetting<Rotate> explodeRotate = (ModeSetting<Rotate>) new ModeSetting<>("Rotate", "How to rotate to the crystal", Rotate.PACKET).setParentSetting(explode);
    private final ModeSetting<AntiWeakness> antiWeakness = (ModeSetting<AntiWeakness>) new ModeSetting<>("Anti Weakness", "Attack crystals whilst having the weakness effect", AntiWeakness.SWITCH).setParentSetting(explode);
    private final BooleanSetting setDead = (BooleanSetting) new BooleanSetting("Set Dead", "Instantly set the crystal's alive state to dead when you attack it", true).setParentSetting(explode);

    /* Parameters */
    private final BooleanSetting parameters = new BooleanSetting("Parameters", "Parameter preferences", true);
    private final NumberSetting minimumDamage = (NumberSetting) new NumberSetting("Minimum Damage", "The minimum damage to do to the target", 4, 0, 36, 1).setParentSetting(parameters);
    private final NumberSetting maximumLocal = (NumberSetting) new NumberSetting("Maximum Local", "The maximum local damage to inflict upon yourself", 6, 0, 36, 1).setParentSetting(parameters);
    private final BooleanSetting antiSuicide = (BooleanSetting) new BooleanSetting("Anti Suicide", "Does not explode or place crystals if they will kill us", true).setParentSetting(parameters);

    /* Render */
    private final BooleanSetting render = new BooleanSetting("Render", "Highlight the block we are placing on", true);
    private final BooleanSetting fill = (BooleanSetting) new BooleanSetting("Fill", "Fill the block with a colour", true).setParentSetting(render);
    private final BooleanSetting outline = (BooleanSetting) new BooleanSetting("Outline", "Render an outline on the block", true).setParentSetting(render);
    private final NumberSetting outlineWidth = (NumberSetting) new NumberSetting("Line Width", "The width of the outlines", 1, 1, 3, 1).setParentSetting(render).setVisiblity(outline::isEnabled);
    private final ColourSetting colour = (ColourSetting) new ColourSetting("Colour", "The render colour", new Color(185, 19, 211)).setParentSetting(render);
    private final BooleanSetting nametag = (BooleanSetting) new BooleanSetting("Damage Nametag", "Renders a nametag at the place position to show you the damage", true).setParentSetting(render);
    private final BooleanSetting selfDamage = (BooleanSetting) new BooleanSetting("Self Damage", "Renders the damage done to you in the nametag", true).setParentSetting(render).setVisiblity(nametag::isEnabled);
    private final ModeSetting<HUDData> hudData = new ModeSetting<>("HUD Data", "The data to show in the array list", HUDData.TARGET);
    private final BooleanSetting targetHealth = (BooleanSetting) new BooleanSetting("Health", "Show the targets health", true).setParentSetting(render).setVisiblity(() -> hudData.getCurrentMode() == HUDData.TARGET);

    // The player we are targeting
    private EntityPlayer target;

    // The current position we are placing at
    private BlockPos placePosition;
    private CrystalPosition crystalPosition;

    // A list of self placed crystals. Only initialised to prevent possible NullPointerException crashes
    private final ArrayList<BlockPos> selfPlacedCrystals = new ArrayList<>();

    public AutoCrystal() {
        super("AutoCrystal", ModuleCategory.COMBAT, "Automatically explodes and places crystals");
        this.addSettings(actionPriority, targeting, place, explode, parameters, render);
    }

    @Override
    public void onEnable() {
        // Clear crystals
        selfPlacedCrystals.clear();
    }

    @Override
    public void onDisable() {
        // Clear crystals
        selfPlacedCrystals.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (nullCheck()) {
            return;
        }

        this.target = targetPlayer();

        // If it's null, we don't want to do anything except set the place position to null
        if (this.target == null) {
            placePosition = null;
            return;
        }

        try {
            switch (actionPriority.getCurrentMode()) {
                case PLACE_EXPLODE:
                    // Place
                    if (place.isEnabled()) {
                        placeCrystals();
                    }

                    // Explode
                    if (explode.isEnabled()) {
                        explodeCrystals();
                    }

                    break;
                case EXPLODE_PLACE:
                    // Explode
                    if (explode.isEnabled()) {
                        explodeCrystals();
                    }

                    // Place
                    if (place.isEnabled()) {
                        placeCrystals();
                    }

                    break;
            }
        }

        // WHY DOES IT THROW A FUCKING NPE
        catch (NullPointerException exception) {
            exception.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (crystalPosition != null && target != null && place.isEnabled()) {
            // Make sure render is enabled
            if (render.isEnabled()) {
                // Render fill
                if (fill.isEnabled()) {
                    RenderUtil.drawFilledBox(BlockUtil.getBlockBox(crystalPosition.getPosition()), ColourUtil.integrateAlpha(colour.getColour(), 200));
                }

                // Render outline
                if (outline.isEnabled()) {
                    RenderUtil.drawBoundingBox(BlockUtil.getBlockBox(crystalPosition.getPosition()), outlineWidth.getValue(), ColourUtil.integrateAlpha(colour.getColour(), 255));
                }

                // Render nametag
                if (nametag.isEnabled()) {
                    float target = Math.round(crystalPosition.getTargetDamage());
                    float self = Math.round(crystalPosition.getSelfDamage());
                    RenderUtil.drawNametagText(target
                            + (selfDamage.isEnabled() ?
                            " Self " + self : ""),
                            new Vec3d(crystalPosition.getPosition().getX() + 0.5f, crystalPosition.getPosition().getY() + 0.5f, crystalPosition.getPosition().getZ() + 0.5f), -1);
                }
            }
        }
    }

    @Listener
    public void onPacket(PacketEvent.PreReceive preReceive) {
        // Check if it's a CPacketPlayerTryUseItemOnBlock packet
        if (preReceive.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            // Check that we are holding end crystals when we try to place
            if (mc.player.getHeldItem(((CPacketPlayerTryUseItemOnBlock) preReceive.getPacket()).getHand()).getItem() == Items.END_CRYSTAL) {
                // Check that the position is valid
                if (isPosObsidianOrBedrock(((CPacketPlayerTryUseItemOnBlock) preReceive.getPacket()).getPos())) {
                    selfPlacedCrystals.add(((CPacketPlayerTryUseItemOnBlock) preReceive.getPacket()).getPos().up());
                }
            }
        }
    }

    /* Main Methods */
    public void explodeCrystals() {
        for (Entity entity : mc.world.loadedEntityList) {
            // Make sure it's a crystal
            if (entity instanceof EntityEnderCrystal) {
                // Make sure it is close enough
                if (!EntityUtil.isTooFarAwayFromSelf(entity, explodeRange.getValue())) {
                    // Check it isn't dead
                    if (entity.isDead) {
                        continue;
                    }

                    // If the crystal will kill us, don't explode
                    if (antiSuicide.isEnabled()) {
                        float local = calculateCrystalDamage(new Vec3d(entity.posX, entity.posY, entity.posZ), mc.player);

                        if (local >= mc.player.getHealth()) {
                            continue;
                        }
                    }

                    if (explodeFilter.getCurrentMode() == ExplodeFilter.SELF) {
                        // Check that the crystal's position is in our self placed crystals list
                        if (!selfPlacedCrystals.contains(entity.getPosition())) {
                            continue;
                        }
                    }

                    // Explode the crystal
                    explodeCrystal((EntityEnderCrystal) entity);
                }
            }
        }
    }

    public void placeCrystals() {
        // Make sure we want to place crystals
        switch (placeWhen.getCurrentMode()) {
            case HOLDING:
                // If we aren't holding crystals, return
                if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {
                    crystalPosition = null;
                    return;
                }
                break;

            case SWITCH:
                // If we aren't holding crystals, try to switch
                if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {
                    // If we couldn't switch, return
                    if (!InventoryUtil.switchToItem(Items.END_CRYSTAL, false)) {
                        crystalPosition = null;
                        return;
                    }
                }
                break;

            case SILENT_SWITCH:
                // If we aren't holding crystals, try to switch
                if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {
                    // If we couldn't switch, return
                    if (!InventoryUtil.switchToItem(Items.END_CRYSTAL, true)) {
                        crystalPosition = null;
                        return;
                    }
                }
                break;
        }

        // Get best placement
        CrystalPosition placement = getPlacePosition(target);

        // Make sure there actually is a place position
        if (placement != null) {
            crystalPosition = placement;

            // Rotate to place position
            Vec2f placeVec = RotationUtil.getRotationToBlockPos(placement.getPosition());
            placeRotate.getCurrentMode().doRotate(placeRotate.getCurrentMode(), placeVec.x, placeVec.y);

            // Place crystal
            placeCrystalOnBlock(placement.getPosition(), new Vec3d(placement.getPosition().getX() + 0.5f, placement.getPosition().getY() + 0.5f, placement.getPosition().getZ() + 0.5f), packetPlace.isEnabled());
        }
    }

    /**
     * Get the best player to target
     * @return The best player
     */
    public EntityPlayer targetPlayer() {
        EntityPlayer currentTarget = null;

        // Set this to a high value as it will never be that high
        float lastValue = 99999f;

        for (Entity entity : mc.world.loadedEntityList) {
            // Check that the entity is a player
            if (!(entity instanceof EntityPlayer)) {
                continue;
            }

            // Make sure we aren't the target
            if (entity.equals(mc.player)) {
                // But allow the entity if it is a fake player
                if (!(entity instanceof EntityFakePlayer)) {
                    continue;
                }
            }

            // Cast to player
            EntityPlayer player = (EntityPlayer) entity;

            // Check the basic parameters for targeting
            if (EntityUtil.isTooFarAwayFromSelf(player, targetingRange.getValue())) { // Make sure they are within our targeting range
                continue;
            }

            // Make sure we want to attack them based on our social relationship with them
            if (!targetFriends.isEnabled()) {
                if (Paragon.INSTANCE.getSocialManager().isFriend(player.getName())) {
                    continue;
                }
            }

            // If the current target is null, set it the player
            if (currentTarget == null) {
                currentTarget = player;
                continue;
            }

            switch (targetPriority.getCurrentMode()) {
                case DISTANCE:
                    // Check that the player's distance from us is less than the last player's distance
                    if (player.getDistance(mc.player) <= lastValue) {
                        currentTarget = player;
                        lastValue = player.getDistance(mc.player);
                    }

                    break;

                case HEALTH:
                    // Check that the player's health from us is less than the last player's health
                    if (player.getHealth() <= currentTarget.getHealth()) {
                        currentTarget = player;
                        lastValue = player.getHealth();
                    }

                    break;

                case ARMOUR:
                    // Check that the player's total armour durability is less than the last player's total armour durability

                    float total = 0f;

                    for (ItemStack itemStack : player.inventory.armorInventory) {
                        total += itemStack.getItemDamage();
                    }

                    if (total > lastValue) {
                        currentTarget = player;
                        lastValue = total;
                    }

                    break;
            }
        }


        return currentTarget;
    }


    /** Explode Utils */
    public void explodeCrystal(EntityEnderCrystal entityEnderCrystal) {
        // Get the current slot so we can switch back
        int antiWeaknessSlot = mc.player.inventory.currentItem;

        if (antiWeakness.getCurrentMode() != AntiWeakness.OFF) {
            // Weakness and strength effects
            PotionEffect weakness = mc.player.getActivePotionEffect(MobEffects.WEAKNESS);
            PotionEffect strength = mc.player.getActivePotionEffect(MobEffects.STRENGTH);

            // Check that we want to switch
            if (weakness != null && (strength == null || strength.getAmplifier() < weakness.getAmplifier())) {
                int swordSlot = InventoryUtil.getItemInHotbar(Items.DIAMOND_SWORD);

                // Check that we have found the item
                if (swordSlot > -1) {
                    InventoryUtil.switchToSlot(swordSlot, antiWeakness.getCurrentMode() == AntiWeakness.SILENT);
                } else if (swordSlot == -1) {
                    // It will bug out slightly - we don't want that
                    return;
                }
            }
        }

        // Throws an NPE!?
        if (entityEnderCrystal == null) {
            return;
        }

        // Rotate towards the crystal
        Vec2f rotationVec = RotationUtil.getRotationToVec3d(new Vec3d(entityEnderCrystal.posX, entityEnderCrystal.posY, entityEnderCrystal.posZ));
        explodeRotate.getCurrentMode().doRotate(explodeRotate.getCurrentMode(), rotationVec.x, rotationVec.y);

        // Attack the crystal
        mc.playerController.attackEntity(mc.player, entityEnderCrystal);

        // Instantly set the crystal to dead
        if (setDead.isEnabled()) {
            entityEnderCrystal.setDead();
        }

        switch (explodeSwing.getCurrentMode()) {
            case PACKET:
                // Send animation packet
                mc.player.connection.sendPacket(new CPacketAnimation(mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND));
                break;
            case MAIN:
                // Swing main hand
                mc.player.swingArm(EnumHand.MAIN_HAND);
                break;
            case OFF:
                // Swing offhand
                mc.player.swingArm(EnumHand.OFF_HAND);
                break;
            default:
                break;
        }

        // Remove the position from our self placed crystals
        selfPlacedCrystals.remove(entityEnderCrystal.getPosition());

        if (antiWeakness.getCurrentMode() != AntiWeakness.OFF) {
            PotionEffect weakness = mc.player.getActivePotionEffect(MobEffects.WEAKNESS);
            PotionEffect strength = mc.player.getActivePotionEffect(MobEffects.STRENGTH);

            if (weakness != null && (strength == null || strength.getAmplifier() < weakness.getAmplifier())) {
                // Switch back to original slot
                InventoryUtil.switchToSlot(antiWeaknessSlot, antiWeakness.getCurrentMode() == AntiWeakness.SILENT);
            }
        }
    }

    /** Place Utils */
    public void placeCrystalOnBlock(BlockPos position, Vec3d vec, boolean packet) {
        if (packet) {
            // Send packet place
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(position, EnumFacing.getDirectionFromEntityLiving(position, mc.player), mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND, (float) vec.x, (float) vec.y, (float) vec.z));
        } else {
            // Legit click pos
            mc.playerController.processRightClickBlock(mc.player, mc.world, position, EnumFacing.getDirectionFromEntityLiving(position, mc.player), vec, mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
        }

        // Add the position above the place position, which is where the crystal will be placed
        selfPlacedCrystals.add(position.up());
    }

    public CrystalPosition getPlacePosition(EntityPlayer playerTarget) {
        CrystalPosition bestPosition = null;

        for (BlockPos pos : BlockUtil.getSphere(placeRange.getValue(), true)) {
            // Check we can place the crystal
            if (!canPlaceCrystal(pos)) {
                continue;
            }

            float selfDamage = 9999f;

            // If the crystal will kill us, don't place
            if (antiSuicide.isEnabled()) {
                selfDamage = calculateCrystalDamage(new Vec3d(pos.getX() + 0.5f, pos.getY() + 1, pos.getZ()), mc.player);

                if (selfDamage >= mc.player.getHealth()) {
                    continue;
                }
            }

            // Make sure it is close enough
            if (mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > placeRange.getValue()) {
                continue;
            }

            float targetDamage = calculateCrystalDamage(new Vec3d(pos.getX() + 0.5f, pos.getY() + 1, pos.getZ()), playerTarget);

            // Set best position A. if the previous position is null or B. the damage is higher than that of the previous position
            if (bestPosition == null || calculateCrystalDamage(new Vec3d(pos.getX() + 0.5f, pos.getY() + 1, pos.getZ()), playerTarget) > calculateCrystalDamage(new Vec3d(bestPosition.getPosition().getX() + 0.5f, bestPosition.getPosition().getY() + 1, bestPosition.getPosition().getZ()), playerTarget)) {
                bestPosition = new CrystalPosition(pos, targetDamage, selfDamage);
            }
        }

        // Return position
        return bestPosition;
    }

    public boolean canPlaceCrystal(BlockPos pos) {
        if (!isPosObsidianOrBedrock(pos)) {
            return false;
        }

        BlockPos nativePosition = pos.up();

        if (!mc.world.isAirBlock(nativePosition) && !mc.world.getBlockState(nativePosition).getMaterial().isReplaceable() && !mc.world.getBlockState(nativePosition).getMaterial().equals(Material.FIRE)) {
            return false;
        }

        if (BlockUtil.isIntercepted(pos)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a given position is obsidian or bedrock
     * @param blockPos Position to check
     * @return If the given position is obsidian or bedrock
     */
    public boolean isPosObsidianOrBedrock(BlockPos blockPos) {
        return BlockUtil.getBlockAtPos(blockPos) instanceof BlockObsidian || BlockUtil.getBlockAtPos(blockPos) == Blocks.BEDROCK;
    }

    /* Other Utils */
    /**
     * @author GameSense
     * @param vec Vec3D of positions
     * @param entity The entity to calculate damage to
     * @return The damage from the crystal
     */
    public float calculateCrystalDamage(Vec3d vec, Entity entity) {
        float doubleExplosionSize = 12.0F;
        double distancedSize = entity.getDistance(vec.x, vec.y, vec.z) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(vec.x, vec.y, vec.z);
        double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        double v = (1.0D - distancedSize) * blockDensity;
        float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));
        double finald = 1.0D;

        if (entity instanceof EntityLivingBase) {
            float damageMultiplied = damage * (mc.world.getDifficulty().getDifficultyId() == 0 ? 0 : (mc.world.getDifficulty().getDifficultyId() == 2 ? 1 : (mc.world.getDifficulty().getDifficultyId() == 1 ? 0.5f : 1.5f)));
            finald = getBlastReduction((EntityLivingBase) entity, damageMultiplied, new Explosion(mc.world, entity, vec.x, vec.y, vec.z, 6F, false, true));
        }
        return (float) finald;
    }

    /**
     * @author GameSense, Wolfsurge
     * @param entityLivingBase The entity to calculate damage for
     * @param damage The original, unmodified damage
     * @param explosion The explosion to calculate
     * @return Damage minus blast reduction
     */
    public float getBlastReduction(EntityLivingBase entityLivingBase, float damage, Explosion explosion) {
        if (entityLivingBase instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLivingBase;
            DamageSource damageSource = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) player.getTotalArmorValue(), (float) player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

            int enchantmentModifierDamage = EnchantmentHelper.getEnchantmentModifierDamage(player.getArmorInventoryList(), damageSource);
            float clampedModifierDamage = MathHelper.clamp(enchantmentModifierDamage, 0.0F, 20.0F);
            damage *= 1.0F - clampedModifierDamage / 25.0F;

            // If player has resistance, decrease damage
            if (player.isPotionActive(MobEffects.RESISTANCE)) {
                damage = damage - (damage / 4);
            }

            damage = Math.max(damage, 0.0F);
            return damage;
        }

        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entityLivingBase.getTotalArmorValue(), (float) entityLivingBase.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    public boolean isPositionWithinDamageParameters(BlockPos position, EntityPlayer playerTarget) {
        Vec3d vec = new Vec3d(position.getX() + 0.5f, position.getY() + 1f, position.getZ() + 0.5f);
        float targetDamage = calculateCrystalDamage(vec, playerTarget);
        float playerDamage = calculateCrystalDamage(vec, mc.player);

        return targetDamage >= minimumDamage.getValue() && playerDamage <= maximumLocal.getValue();
    }

    /** Other Stuff */
    public String getModuleInfo() {
        String moduleInfo = " ";

        if (hudData.getCurrentMode() == HUDData.TARGET) {
            moduleInfo += (target == null ? "No Target" : target.getName());
            if (targetHealth.isEnabled()) {
                moduleInfo += (target == null ? "" : EntityUtil.getTextColourFromEntityHealth(target) + " " + target.getHealth());
            }
        } else if (hudData.getCurrentMode() == HUDData.CRYSTAL_COUNT) {
            moduleInfo += String.valueOf(InventoryUtil.getCountOfItem(Items.END_CRYSTAL, true));
        }

        return moduleInfo;
    }

    public enum ActionPriority {
        /**
         * Place then explode
         */
        PLACE_EXPLODE,

        /**
         * Explode then place
         */
        EXPLODE_PLACE
    }

    public enum TargetPriority {
        /**
         * Sort by distance
         */
        DISTANCE,

        /**
         * Sort by health
         */
        HEALTH,

        /**
         * Sort by total armour durability
         */
        ARMOUR
    }

    public enum PlaceWhen {
        /**
         * Place when holding crystals
         */
        HOLDING,

        /**
         * Switch to crystals
         */
        SWITCH,

        /**
         * Switch with a packet to crystals
         */
        SILENT_SWITCH
    }

    public enum ExplodeSwing {
        /**
         * Packet swing
         */
        PACKET,

        /**
         * Swing main hand
         */
        MAIN,

        /**
         * Swing off hand
         */
        OFF,

        /**
         * Don't swing
         */
        NO_SWING
    }

    public enum ExplodeFilter {
        /**
         * Explode any crystal if they are within the damage parameters
         */
        SMART,

        /**
         * Only explode crystals placed by you
         */
        SELF,

        /**
         * Explode all crystals
         */
        ALL
    }

    public enum AntiWeakness {
        /**
         * Legit switch
         */
        SWITCH,

        /**
         * Switch with a packet
         */
        SILENT,

        /**
         * Don't switch at all
         */
        OFF
    }

    public enum Rotate {
        /**
         * Packet rotate
         */
        PACKET,

        /**
         * Legit rotate - set player's yaw and pitch
         */
        LEGIT,

        /**
         * Do not rotate
         */
        NONE;

        public void doRotate(Rotate in, float yaw, float pitch) {
            switch (in) {
                case PACKET:
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, mc.player.onGround));
                    break;
                case LEGIT:
                    mc.player.rotationYaw = yaw;
                    mc.player.rotationYawHead = yaw;
                    mc.player.rotationPitch = pitch;
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, mc.player.onGround));
                    break;
                case NONE:
                    break;
            }
        }
    }

    public enum HUDData {
        /**
         * Show target info
         */
        TARGET,

        /**
         * Show crystal count
         */
        CRYSTAL_COUNT
    }

    class CrystalPosition {

        // Position to place
        private BlockPos position;

        // Damage done to target
        private float targetDamage;

        // Damage done to us
        private float selfDamage;

        public CrystalPosition(BlockPos position, float targetDamage, float selfDamage) {
            this.position = position;
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