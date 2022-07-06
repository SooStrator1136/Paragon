package com.paragon.client.systems.module.impl.combat;

import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.function.VoidFunction;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.player.RotationUtil;
import com.paragon.api.util.render.ColourUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.util.world.BlockUtil;
import com.paragon.asm.mixins.accessor.IPlayerControllerMP;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Wolfsurge
 */
public class AutoCrystalRewrite extends Module {

    public static AutoCrystalRewrite INSTANCE;

    public static Setting<Order> order = new Setting<>("Order", Order.PLACE_EXPLODE)
            .setDescription("The order in which to complete actions");

    // TARGETING

    public static Setting<TargetSort> targeting = new Setting<>("Targeting", TargetSort.DISTANCE)
            .setDescription("The target priority to use");

    public static Setting<Double> targetRange = new Setting<>("Range", 10.0D, 1.0D, 15.0D, 0.1D)
            .setDescription("The furthest a target can be away from you")
            .setParentSetting(targeting);

    public static Setting<Boolean> players = new Setting<>("Players", true)
            .setDescription("Whether to target players")
            .setParentSetting(targeting);

    public static Setting<Boolean> mobs = new Setting<>("Mobs", true)
            .setDescription("Whether to target mobs")
            .setParentSetting(targeting);

    public static Setting<Boolean> animals = new Setting<>("Animals", true)
            .setDescription("Whether to target animals")
            .setParentSetting(targeting);

    // PLACEMENTS

    public static Setting<Boolean> place = new Setting<>("Place", true)
            .setDescription("Whether to place crystals");

    public static Setting<Float> placeDelay = new Setting<>("Delay", 50f, 0f, 1000f, 1f)
            .setDescription("The delay between placing crystals")
            .setParentSetting(place);

    public static Setting<Perform> placePerform = new Setting<>("Perform", Perform.SILENT_SWITCH)
            .setDescription("When to perform the action of placing crystals")
            .setParentSetting(place);

    public static Setting<Double> placeRange = new Setting<>("Range", 5.0D, 1.0D, 7.0D, 0.1D)
            .setDescription("The furthest distance a crystal can be placed")
            .setParentSetting(place);

    public static Setting<Boolean> multiplace = new Setting<>("Multiplace", false)
            .setDescription("Allow placing multiple crystals at different positions")
            .setParentSetting(place);

    public static Setting<Double> placeMinimum = new Setting<>("Minimum", 4.0D, 1.0D, 36.0D, 1D)
            .setDescription("The minimum damage the crystal must deal to place")
            .setParentSetting(place);

    public static Setting<Double> placeMaximum = new Setting<>("Maximum", 10.0D, 1.0D, 36.0D, 1D)
            .setDescription("The maximum damage the crystal can deal to you to place")
            .setParentSetting(place);

    public static Setting<Raytrace> placeRaytrace = new Setting<>("Raytrace", Raytrace.HALF)
            .setDescription("The raytrace method to use when placing crystals")
            .setParentSetting(place);

    public static Setting<Place> placeMode = new Setting<>("Place", Place.VANILLA)
            .setDescription("How to place the crystal")
            .setParentSetting(place);

    public static Setting<Boolean> placeSwing = new Setting<>("Swing", true)
            .setDescription("Whether to swing the item when placing")
            .setParentSetting(place);

    // EXPLODE

    public static Setting<Boolean> explode = new Setting<>("Explode", true)
            .setDescription("Whether to explode crystals");

    // RENDER
    public static Setting<Boolean> render = new Setting<>("Render", true)
            .setDescription("Whether to render the placement position");

    public static Setting<Render> renderMode = new Setting<>("Mode", Render.BOTH)
            .setDescription("The render mode to use")
            .setParentSetting(render);

    public static Setting<Float> renderOutlineWidth = new Setting<>("Width", 1.0F, 0.0F, 5.0F, 0.1F)
            .setDescription("The width of the outline")
            .setParentSetting(render)
            .setVisibility(() -> !renderMode.getValue().equals(Render.FILL));

    public static Setting<Color> renderColour = new Setting<>("Colour", new Color(185, 17, 255))
            .setDescription("The colour of the render")
            .setParentSetting(render);

    public static Setting<Color> renderOutlineColour = new Setting<>("OutlineColour", new Color(185, 17, 255))
            .setDescription("The colour of the render")
            .setParentSetting(render)
            .setVisibility(() -> !renderMode.getValue().equals(Render.FILL));

    private BlockPos placementPosition;
    private Timer placeTimer = new Timer();
    private float placementDamage;

    public AutoCrystalRewrite() {
        super("AutoCrystalRewrite", Category.COMBAT, "Automatically places and explodes ender crystals");

        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        order.getValue().run();
    }

    @Override
    public void onRender3D() {
        if (render.getValue() && placementPosition != null) {
            // Render fill
            if (renderMode.getValue().equals(Render.FILL) || renderMode.getValue().equals(Render.BOTH)) {
                RenderUtil.drawFilledBox(BlockUtil.getBlockBox(placementPosition), renderColour.getValue());
            }

            // Render outline
            if (renderMode.getValue().equals(Render.OUTLINE) || renderMode.getValue().equals(Render.BOTH)) {
                RenderUtil.drawBoundingBox(BlockUtil.getBlockBox(placementPosition), renderOutlineWidth.getValue(), renderOutlineColour.getValue());
            }
        }
    }

    /**
     * Finds the best target
     *
     * @param positions A list of positions to sort for damage
     * @param positionOffset The Y offset for positions
     *
     * @return The best target
     */
    private EntityLivingBase findTarget(ArrayList<BlockPos> positions, float positionOffset) {
        List<EntityLivingBase> validEntities = mc.world.loadedEntityList.stream().filter(entity -> mc.player.getDistance(entity) <= targetRange.getValue() && EntityUtil.isEntityAllowed(entity, players.getValue(), mobs.getValue(), animals.getValue())).sorted(Comparator.comparingDouble(entity -> {
            switch (targeting.getValue()) {
                case DISTANCE:
                    return mc.player.getDistance(entity);

                case HEALTH:
                    return ((EntityLivingBase) entity).getHealth();

                case DAMAGE:
                    positions.sort(Comparator.comparingDouble(pos -> calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + positionOffset, pos.getZ() + 0.5), (EntityLivingBase) entity)));

                    return calculateDamage(new Vec3d(positions.get(0).getX() + 0.5, positions.get(0).getY() + positionOffset, positions.get(0).getZ() + 0.5), (EntityLivingBase) entity);
            }

            return 0;
        })).map(EntityLivingBase.class::cast).collect(Collectors.toList());

        if (validEntities.isEmpty()) {
            return null;
        }

        return validEntities.get(0);
    }

    private void explodeCrystals() {
        if (!explode.getValue()) {
            return;
        }
    }

    private void placeCrystals() {
        if (!place.getValue() || !placeTimer.hasMSPassed(placeDelay.getValue() / 2)) {
            return;
        }

        ArrayList<BlockPos> placeablePositions = BlockUtil.getSphere(placeRange.getValue().floatValue(), true).stream().filter(this::isPlaceable).collect(Collectors.toCollection(ArrayList::new));

        EntityLivingBase target = findTarget(placeablePositions, 1);

        if (target == null) {
            return;
        }

        BlockPos placement = findBestPlacement(target, placeablePositions);

        if (placement == null) {
            placementPosition = null;
            return;
        }

        placementPosition = placement;
        placementDamage = Math.round(calculateDamage(new Vec3d(placement.getX() + 0.5, placement.getY() + 1, placement.getZ() + 0.5), target));

        int previousSlot = mc.player.inventory.currentItem;

        // For silent switch
        boolean alreadyHolding = false;

        switch (placePerform.getValue()) {
            case HOLDING:
                if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {
                    return;
                }

                break;

            case SILENT_SWITCH:
            case KEEP:
                int crystalSlot = InventoryUtil.getItemInHotbar(Items.END_CRYSTAL);

                if (crystalSlot > -1) {
                    if (!InventoryUtil.isHolding(Items.END_CRYSTAL)) {
                        alreadyHolding = true;
                    } else {
                        mc.player.inventory.currentItem = crystalSlot;
                        ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();
                    }
                } else {
                    return;
                }

                break;
        }

        EnumHand crystalHand = alreadyHolding ? InventoryUtil.getHandHolding(Items.END_CRYSTAL) : EnumHand.MAIN_HAND;

        if (crystalHand == null) {
            return;
        }

        double eye = mc.player.posY + mc.player.eyeHeight;

        EnumFacing facing = EnumFacing.UP;
        float distance = 0;

        Vec3d facingVec = new Vec3d(0.5, 0.5, 0.5);

        if (!placeRaytrace.getValue().equals(Raytrace.NONE)) {
            if (placement.getY() > eye) {
                for (float x = 0; x <= 1; x += placeRaytrace.getValue().getIncrease()) {
                    for (float y = 0; y <= 1; y += placeRaytrace.getValue().getIncrease()) {
                        for (float z = 0; z <= 1; z += placeRaytrace.getValue().getIncrease()) {
                            Vec3d vector = new Vec3d(placement).add(x, y, z);

                            RayTraceResult result = mc.world.rayTraceBlocks(mc.player.getPositionEyes(mc.getRenderPartialTicks()), vector, false, true, false);

                            if (result != null && result.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                                double vectorDistance = vector.distanceTo(mc.player.getPositionEyes(mc.getRenderPartialTicks()));

                                if (vectorDistance < distance || distance == 0) {
                                    distance = (float) vectorDistance;
                                    facing = result.sideHit;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (placeMode.getValue().equals(Place.VANILLA)) {
            mc.playerController.processRightClickBlock(mc.player, mc.world, placement, facing, facingVec, crystalHand);
        }

        else if (placeMode.getValue().equals(Place.PACKET)) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placement, facing, crystalHand, (float) facingVec.x, (float) facingVec.y, (float) facingVec.z));
        }

        if (placeSwing.getValue()) {
            mc.player.swingArm(crystalHand);
        }

        if (placePerform.getValue().equals(Perform.SILENT_SWITCH) && !alreadyHolding) {
            mc.player.inventory.currentItem = previousSlot;
            ((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();
        }

        placeTimer.reset();
    }

    private BlockPos findBestPlacement(EntityLivingBase target, ArrayList<BlockPos> positions) {
        if (positions.isEmpty()) {
            return null;
        }

        Collections.reverse(positions);

        BlockPos placement = positions.get(0);

        float placementDamage = calculateDamage(new Vec3d(placement.getX() + 0.5, placement.getY() + 1, placement.getZ() + 0.5), target);

        for (BlockPos pos : positions) {
            float positionDamage = calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), target);

            if (positionDamage <= placementDamage || positionDamage <= placeMinimum.getValue() || calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), mc.player) > placeMaximum.getValue()) {
                continue;
            }

            placement = pos;
        }

        return placement;
    }

    // UTILS

    private boolean isPlaceable(BlockPos pos) {
        BlockPos offset = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        BlockPos offset2 = new BlockPos(pos.getX(), pos.getY() + 2, pos.getZ());

        if (BlockUtil.getBlockAtPos(pos) != Blocks.BEDROCK && BlockUtil.getBlockAtPos(pos) != Blocks.OBSIDIAN) {
            return false;
        }

        if (BlockUtil.getBlockAtPos(offset) != Blocks.AIR || BlockUtil.getBlockAtPos(offset2) != Blocks.AIR) {
            return false;
        }

        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(offset))) {
            if (entity instanceof EntityEnderCrystal || !multiplace.getValue()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calculates the explosion damage based on a Vec3D
     * @param vec The vector to calculate damage from
     * @param entity The target
     * @return The damage done to the target
     */
    private float calculateDamage(Vec3d vec, EntityLivingBase entity) {
        float doubleExplosionSize = 12.0F;
        double distancedSize = entity.getDistance(vec.x, vec.y, vec.z) / (double) doubleExplosionSize;
        double blockDensity = entity.world.getBlockDensity(new Vec3d(vec.x, vec.y, vec.z), entity.getEntityBoundingBox());
        double v = (1.0D - distancedSize) * blockDensity;
        float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));

        int diff = mc.world.getDifficulty().getId();

        return getBlastReduction(entity, damage * (diff == 0 ? 0 : (diff == 2 ? 1 : (diff == 1 ? 0.5f : 1.5f))), new Explosion(mc.world, null, vec.x, vec.y, vec.z, 6F, false, true));
    }

    /**
     * Gets the blast reduction
     * @param entity The entity to calculate damage for
     * @param damage The original damage
     * @param explosion The explosion
     * @return The blast reduction
     */
    private float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        DamageSource source = DamageSource.causeExplosionDamage(explosion);
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

        int enchantmentModifier = MathHelper.clamp(EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), source), 0, 20);
        damage *= 1.0F - enchantmentModifier / 25.0F;

        if (entity.isPotionActive(MobEffects.WEAKNESS)) {
            damage = damage - (damage / 4);
        }

        return Math.max(damage, 0.0F);
    }

    @Override
    public String getData() {
        return placementDamage == 0 ? "" : " " + placementDamage;
    }

    public enum Order {
        /**
         * Explodes ender crystals then places them
         */
        EXPLODE_PLACE(() -> {
            INSTANCE.explodeCrystals();
            INSTANCE.placeCrystals();
        }),

        /**
         * Places ender crystals then explodes them
         */
        PLACE_EXPLODE(() -> {
            INSTANCE.placeCrystals();
            INSTANCE.explodeCrystals();
        });

        private final VoidFunction function;

        Order(VoidFunction run) {
            this.function = run;
        }

        public void run() {
            function.invoke();
        }
    }

    public enum TargetSort {
        /**
         * Sorts targets by distance
         */
        DISTANCE,

        /**
         * Sorts targets by health
         */
        HEALTH,

        /**
         * Sorts targets by amount of damage we can do to them
         */
        DAMAGE
    }

    public enum Perform {
        /**
         * Perform when holding crystals
         */
        HOLDING,

        /**
         * Switch to crystals to perform
         */
        KEEP,

        /**
         * Switch to crystals, then switch back once completed
         */
        SILENT_SWITCH
    }

    public enum Place {
        /**
         * Use a vanilla method
         */
        VANILLA,

        /**
         * Send a packet
         */
        PACKET
    }

    public enum Raytrace {
        /**
         * Don't raytrace
         */
        NONE(0),

        /**
         * Check every 0.5
         */
        HALF(0.5),

        /**
         * Check every 0.05
         */
        TWENTY(0.05),

        /**
         * Check every 0.01
         */
        HUNDRED(0.01);

        private double increase;

        Raytrace(double increase) {
            this.increase = increase;
        }

        public double getIncrease() {
            return increase;
        }
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

}