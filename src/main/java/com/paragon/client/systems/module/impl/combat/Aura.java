package com.paragon.client.systems.module.impl.combat;

import com.paragon.Paragon;
import com.paragon.api.util.calculations.Timer;
import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.player.EntityFakePlayer;
import com.paragon.api.util.player.InventoryUtil;
import com.paragon.api.util.player.RotationUtil;
import com.paragon.client.managers.rotation.Rotate;
import com.paragon.client.managers.rotation.Rotation;
import com.paragon.client.managers.rotation.RotationPriority;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Basic Aura module.
 *
 * @author Wolfsurge
 */
public class Aura extends Module {

    public static Aura INSTANCE;

    // How to sort the targets
    public static Setting<Sort> sort = new Setting<>("Sort", Sort.DISTANCE)
            .setDescription("How to sort the targets");

    // Filters
    public static Setting<Boolean> players = new Setting<>("Players", true)
            .setDescription("Attack players");

    private static Setting<Boolean> mobs = new Setting<>("Mobs", true)
            .setDescription("Attack mobs");

    public static Setting<Boolean> passives = new Setting<>("Passives", true)
            .setDescription("Attack passives");

    // Main settings
    public static Setting<Float> range = new Setting<>("Range", 5f, 0f, 5f, 0.1f)
            .setDescription("The range to attack");

    public static Setting<Double> delay = new Setting<>("Delay", 700D, 0D, 2000D, 1D)
            .setDescription("The delay between attacking in milliseconds");

    public static Setting<When> when = new Setting<>("When", When.HOLDING)
            .setDescription("When to attack");

    public static Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.PACKET)
            .setDescription("How to rotate to the target");

    public static Setting<Boolean> rotateBack = new Setting<>("Rotate Back", true)
            .setDescription("Rotate back to your original rotation").setParentSetting(rotate);

    public static Setting<Where> where = new Setting<>("Where", Where.BODY)
            .setDescription("Where to attack");

    public static Setting<Boolean> packetAttack = new Setting<>("Packet Attack", false)
            .setDescription("Attack with a packet");

    private final Timer attackTimer = new Timer();
    private EntityLivingBase target;

    public Aura() {
        super("Aura", Category.COMBAT, "Automatically attacks entities");

        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        target = null;

        // Check the delay has passed
        if (attackTimer.hasMSPassed(delay.getValue())) {
            // Filter entities
            List<Entity> entities = mc.world.loadedEntityList.stream().filter(EntityLivingBase.class::isInstance).collect(Collectors.toList());

            // Filter entities based on settings
            entities = entities.stream().filter(entity -> entity.getDistance(mc.player) <= range.getValue() && entity != mc.player && !entity.isDead && (EntityUtil.isEntityAllowed(entity, players.getValue(), mobs.getValue(), passives.getValue()) || entity instanceof EntityFakePlayer) && (!(entity instanceof EntityPlayer) || !Paragon.INSTANCE.getSocialManager().isFriend(entity.getName()))).collect(Collectors.toList());

            // Sort entities
            entities.sort(Comparator.comparingDouble(entityLivingBase -> sort.getValue().getSort((EntityLivingBase) entityLivingBase)));

            // Check we have targets
            if (!entities.isEmpty()) {
                // Get the target
                EntityLivingBase entityLivingBase = (EntityLivingBase) entities.get(0);

                target = entityLivingBase;

                // Get our old slot
                int oldSlot = mc.player.inventory.currentItem;

                // Check we want to attack
                switch (when.getValue()) {
                    case SILENT_SWITCH:
                    case SWITCH:
                        // If we aren't holding a sword, switch to it
                        if (!InventoryUtil.isHoldingSword()) {
                            int swordSlot = InventoryUtil.getItemSlot(Items.DIAMOND_SWORD);

                            if (swordSlot > -1) {
                                InventoryUtil.switchToSlot(swordSlot, false);
                            } else {
                                return;
                            }
                        }

                    case HOLDING:
                        // Return if we aren't holding a sword
                        if (!InventoryUtil.isHoldingSword()) {
                            return;
                        }
                }

                // Get our original rotation
                Vec2f originalRotation = new Vec2f(mc.player.rotationYaw, mc.player.rotationPitch);

                // Get our target rotation
                Vec2f rotationVec = RotationUtil.getRotationToVec3d(new Vec3d(entityLivingBase.posX, entityLivingBase.posY + where.getValue().getWhere(entityLivingBase), entityLivingBase.posZ));
                Rotation rotation = new Rotation(rotationVec.x, rotationVec.y, rotate.getValue(), RotationPriority.HIGH);

                // Rotate to the target
                Paragon.INSTANCE.getRotationManager().addRotation(rotation);

                // Attack the target
                if (packetAttack.getValue()) {
                    mc.player.connection.sendPacket(new CPacketUseEntity(entityLivingBase, EnumHand.MAIN_HAND));
                } else {
                    mc.playerController.attackEntity(mc.player, entityLivingBase);
                }

                // Swing hand
                mc.player.swingArm(EnumHand.MAIN_HAND);

                // Reset our cooldown
                mc.player.resetCooldown();

                // Rotate back to the original rotation
                if (rotateBack.getValue() && !rotate.getValue().equals(Rotate.NONE)) {
                    Rotation rotationBack = new Rotation(originalRotation.x, originalRotation.y, rotate.getValue(), RotationPriority.NORMAL);

                    Paragon.INSTANCE.getRotationManager().addRotation(rotationBack);
                }

                // Switch back to the old slot
                if (oldSlot != mc.player.inventory.currentItem && when.getValue().equals(When.SILENT_SWITCH)) {
                    InventoryUtil.switchToSlot(oldSlot, false);
                }
            }

            attackTimer.reset();
        }
    }

    @Override
    public String getArrayListInfo() {
        return " " + (target == null ? "No target" : target.getName());
    }

    public enum Sort {
        /**
         * Sort by distance
         */
        DISTANCE((e) -> mc.player.getDistance(e)),

        /**
         * Sort by health
         */
        HEALTH(EntityLivingBase::getHealth),

        /**
         * Sort by armour
         */
        ARMOUR(entityLivingBase -> {
            float totalArmourDamage = 0;

            for (ItemStack itemStack : entityLivingBase.getArmorInventoryList()) {
                totalArmourDamage += itemStack.getItemDamage();
            }

            return totalArmourDamage;
        });

        // The function to sort by
        Function<EntityLivingBase, Float> function;

        Sort(Function<EntityLivingBase, Float> func) {
            this.function = func;
        }

        /**
         * Gets the function to sort by
         *
         * @return The function to sort by
         */
        public float getSort(EntityLivingBase entityLivingBase) {
            return function.apply(entityLivingBase);
        }
    }

    public enum When {
        /**
         * Only attack when we are holding a sword
         */
        HOLDING,

        /**
         * Switch to a sword
         */
        SWITCH,

        /**
         * Silent switch to a sword
         */
        SILENT_SWITCH
    }

    public enum Where {
        /**
         * Rotate to feet of target
         */
        FEET(entityLivingBase -> 0f),

        /**
         * Rotate to body of target
         */
        BODY(entityLivingBase -> entityLivingBase.width / 2f),

        /**
         * Rotate to head of target
         */
        HEAD(entityLivingBase -> entityLivingBase.height);

        // The function to get the added height
        Function<EntityLivingBase, Float> function;

        Where(Function<EntityLivingBase, Float> func) {
            this.function = func;
        }

        /**
         * Gets the height to add to the rotation
         *
         * @param entityLivingBase The entity to get the height for
         * @return The height to add to the rotation
         */
        public float getWhere(EntityLivingBase entityLivingBase) {
            return function.apply(entityLivingBase);
        }
    }
}
