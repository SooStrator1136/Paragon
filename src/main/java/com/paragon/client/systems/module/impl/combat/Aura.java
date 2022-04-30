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
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;
import com.paragon.client.systems.module.settings.impl.NumberSetting;
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
 * @author Wolfsurge
 */
public class Aura extends Module {

    public static Aura INSTANCE;

    // How to sort the targets
    private final ModeSetting<Sort> sort = new ModeSetting<>("Sort", "How to sort the targets", Sort.DISTANCE);

    // Filters
    private final BooleanSetting players = new BooleanSetting("Players", "Attack players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", "Attack mobs", true);
    private final BooleanSetting passives = new BooleanSetting("Passives", "Attack passives", true);

    // Main settings
    private final NumberSetting range = new NumberSetting("Range", "The range to attack", 5, 0, 5, 1);
    private final NumberSetting delay = new NumberSetting("Delay", "The delay between attacking in milliseconds", 700, 0, 2000, 1);
    private final ModeSetting<When> when = new ModeSetting<>("When", "When to attack", When.HOLDING);
    private final ModeSetting<Rotate> rotate = new ModeSetting<>("Rotate", "How to rotate to the target", Rotate.PACKET);
    private final BooleanSetting rotateBack = (BooleanSetting) new BooleanSetting("Rotate Back", "Rotate back to your original rotation", true).setParentSetting(rotate);
    private final ModeSetting<Where> where = new ModeSetting<>("Where", "Where to attack", Where.BODY);
    private final BooleanSetting packetAttack = new BooleanSetting("Packet Attack", "Attack with a packet", false);

    private final Timer attackTimer = new Timer();

    public Aura() {
        super("Aura", ModuleCategory.COMBAT, "Automatically attacks entities");
        this.addSettings(sort, players, mobs, passives, range, delay, when, rotate, packetAttack, where);

        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        // Check the delay has passed
        if (attackTimer.hasMSPassed((long) delay.getValue())) {
            // Filter entities
            List<Entity> entities = mc.world.loadedEntityList.stream().filter(EntityLivingBase.class::isInstance).collect(Collectors.toList());

            // Filter entities based on settings
            entities = entities.stream().filter(entity -> entity.getDistance(mc.player) <= range.getValue() && entity != mc.player && !entity.isDead && (EntityUtil.isEntityAllowed(entity, players.isEnabled(), mobs.isEnabled(), passives.isEnabled()) || entity instanceof EntityFakePlayer) && (!(entity instanceof EntityPlayer) || !Paragon.INSTANCE.getSocialManager().isFriend(entity.getName()))).collect(Collectors.toList());

            // Sort entities
            entities.sort(Comparator.comparingDouble(entityLivingBase -> sort.getCurrentMode().getSort((EntityLivingBase) entityLivingBase)));

            // Check we have targets
            if (!entities.isEmpty()) {
                // Get the target
                EntityLivingBase entityLivingBase = (EntityLivingBase) entities.get(0);

                // Get our old slot
                int oldSlot = mc.player.inventory.currentItem;

                // Check we want to attack
                switch (when.getCurrentMode()) {
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
                Vec2f rotationVec = RotationUtil.getRotationToVec3d(new Vec3d(entityLivingBase.posX, entityLivingBase.posY + where.getCurrentMode().getWhere(entityLivingBase), entityLivingBase.posZ));
                Rotation rotation = new Rotation(rotationVec.x, rotationVec.y, rotate.getCurrentMode(), RotationPriority.HIGH);

                // Rotate to the target
                Paragon.INSTANCE.getRotationManager().addRotation(rotation);

                // Attack the target
                if (packetAttack.isEnabled()) {
                    mc.player.connection.sendPacket(new CPacketUseEntity(entityLivingBase, EnumHand.MAIN_HAND));
                } else {
                    mc.playerController.attackEntity(mc.player, entityLivingBase);
                }

                // Swing hand
                mc.player.swingArm(EnumHand.MAIN_HAND);

                // Reset our cooldown
                mc.player.resetCooldown();

                // Rotate back to the original rotation
                if (rotateBack.isEnabled() && !rotate.getCurrentMode().equals(Rotate.NONE)) {
                    Rotation rotationBack = new Rotation(originalRotation.x, originalRotation.y, rotate.getCurrentMode(), RotationPriority.NORMAL);

                    Paragon.INSTANCE.getRotationManager().addRotation(rotationBack);
                }

                // Switch back to the old slot
                if (oldSlot != mc.player.inventory.currentItem) {
                    InventoryUtil.switchToSlot(oldSlot, false);
                }
            }

            attackTimer.reset();
        }
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
         * @param entityLivingBase The entity to get the height for
         * @return The height to add to the rotation
         */
        public float getWhere(EntityLivingBase entityLivingBase) {
            return function.apply(entityLivingBase);
        }
    }
}
