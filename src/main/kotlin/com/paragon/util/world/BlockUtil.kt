package com.paragon.util.world

import com.paragon.util.Wrapper
import net.minecraft.block.Block
import net.minecraft.block.BlockLiquid
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.MobEffects
import net.minecraft.util.CombatRules
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @author SooStrator1136
 */
object BlockUtil : Wrapper {

    /**
     * Gets a sphere of blocks around the player
     * @param radius The radius of the sphere
     * @param ignoreAir Whether to ignore air blocks or not
     * @return A list of block positions
     */
    @JvmStatic
    fun getSphere(radius: Float, ignoreAir: Boolean): List<BlockPos> {
        val sphere = ArrayList<BlockPos>()

        val pos = BlockPos(minecraft.player.positionVector)

        val posX = pos.x
        val posY = pos.y
        val posZ = pos.z

        var x = posX - radius.toInt()

        while (x.toFloat() <= posX.toFloat() + radius) {
            var z = posZ - radius.toInt()

            while (z.toFloat() <= posZ.toFloat() + radius) {
                var y = posY - radius.toInt()

                while (y.toFloat() < posY.toFloat() + radius) {
                    if (((posX - x) * (posX - x) + (posZ - z) * (posZ - z) + (posY - y) * (posY - y)).toDouble() < (radius * radius).toDouble()) {
                        val position = BlockPos(x, y, z)

                        if (minecraft.world.getBlockState(position).block != Blocks.AIR || !ignoreAir) {
                            sphere.add(position)
                        }
                    }

                    y++
                }

                z++
            }

            x++
        }

        return sphere
    }

    /**
     * Gets the block at a position
     * @return The block
     */
    @JvmStatic
    fun BlockPos.getBlockAtPos(): Block = minecraft.world.getBlockState(this).block

    /**
     * Gets the surrounding blocks of a position
     * Ordered by North, East, South, West
     * @return The blocks surrounding blocks
     */
    @JvmStatic
    fun BlockPos.getSurroundingBlocks(): Array<Block> = arrayOf(this.north().getBlockAtPos(), this.east().getBlockAtPos(), this.west().getBlockAtPos(), this.south().getBlockAtPos())

    /**
     * Gets the bounding box of a block
     *
     * @param blockPos The block
     * @return The bounding box of the entity
     */
    @JvmStatic
    fun getBlockBox(blockPos: BlockPos): AxisAlignedBB = AxisAlignedBB(
        blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), (blockPos.x + 1).toDouble(), (blockPos.y + 1).toDouble(), (blockPos.z + 1).toDouble()
    ).offset(
        -minecraft.renderManager.viewerPosX, -minecraft.renderManager.viewerPosY, -minecraft.renderManager.viewerPosZ
    )

    /**
     * Checks if the player can see a position
     * @param pos The position to check
     * @return Whether the player can see the position
     */
    @JvmStatic
    fun canSeePos(pos: BlockPos): Boolean {
        for (facing in EnumFacing.values()) {
            if (minecraft.world.rayTraceBlocks(
                    Vec3d(
                        minecraft.player.posX, minecraft.player.posY + minecraft.player.getEyeHeight().toDouble(), minecraft.player.posZ
                    ), Vec3d(
                        pos.offset(facing).x + 0.5, (pos.offset(facing).y + 1).toDouble(), pos.offset(facing).z + 0.5
                    ), false, true, false
                ) == null
            ) {
                return true
            }
        }

        return minecraft.world.rayTraceBlocks(
            Vec3d(
                minecraft.player.posX, minecraft.player.posY + minecraft.player.getEyeHeight().toDouble(), minecraft.player.posZ
            ), Vec3d(pos.x + 0.5, (pos.y + 1).toDouble(), pos.z + 0.5), false, true, false
        ) == null
    }

    /**
     * Checks if a block is surrounded by blocks
     *
     * @param pos The position
     * @param obbyBedrock Whether to only check if the hole is obsidian or bedrock
     * @return Whether the block is surrounded by blocks
     */
    @JvmStatic
    fun isSafeHole(pos: BlockPos, obbyBedrock: Boolean): Boolean {
        if (pos.getBlockAtPos().isReplaceable(minecraft.world, pos).not()) { //This is chinese on another level and not tested 📈
            return false
        }

        for (facing in EnumFacing.values()) {
            if (facing == EnumFacing.UP) {
                continue
            }

            val block = pos.offset(facing).getBlockAtPos()

            if (block === Blocks.AIR || block !== Blocks.OBSIDIAN && block !== Blocks.BEDROCK && obbyBedrock || block!!.isReplaceable(minecraft.world, pos.offset(facing))) {
                return false
            }
        }

        return true
    }

    /**
     * Gets the first side we can see on a block position
     * @param pos The position to check
     * @return The side we can see (or null, if we can't see any)
     */
    @JvmStatic
    fun getFacing(pos: BlockPos): EnumFacing? {
        for (facing in EnumFacing.values()) {
            if (canSeePos(pos.offset(facing))) {
                return facing
            }
        }

        return null
    }

    /**
     * Checks if a position is a hole
     * @param pos The position to check
     * @return Whether the position is a hole or not
     */
    @JvmStatic
    fun isHole(pos: BlockPos) = !arrayOf(
        pos.down(), pos.north(), pos.east(), pos.south(), pos.west()
    ).any { pos.getBlockAtPos() == Blocks.AIR }

    val BlockPos.isSource: Boolean
        get() = this.getBlockAtPos() is BlockLiquid && minecraft.world.getBlockState(this).getValue(BlockLiquid.LEVEL) == 0

    val BlockPos.distanceToEyes: Double
        get() = sqrt(
            (x - minecraft.player.posX).pow(2) + (y - (minecraft.player.posY + minecraft.player.getEyeHeight())).pow(2) + (z - minecraft.player.posZ).pow(2)
        )

    /**
     * Calculates the explosion damage based on a Vec3D
     * @param vec The vector to calculate damage from
     * @param entity The target
     * @return The damage done to the target
     */
    fun calculateExplosionDamage(vec: Vec3d, entity: EntityLivingBase): Float {
        var finalDamage = 0.0f

        try {
            val doubleExplosionSize = 12.0f
            val distancedSize = entity.getDistance(vec.x, vec.y, vec.z) / doubleExplosionSize.toDouble()
            val blockDensity = entity.world.getBlockDensity(Vec3d(vec.x, vec.y, vec.z), entity.entityBoundingBox).toDouble()
            val v = (1.0 - distancedSize) * blockDensity
            val damage = ((v * v + v) / 2.0 * 7.0 * doubleExplosionSize.toDouble() + 1.0).toInt().toFloat()
            val diff = minecraft.world.difficulty.difficultyId

            finalDamage = getBlastReduction(entity, damage * if (diff == 0) 0f else if (diff == 2) 1f else if (diff == 1) 0.5f else 1.5f, Explosion(minecraft.world, null, vec.x, vec.y, vec.z, 6f, false, true))
        } catch (ignored: NullPointerException) {

        }

        return finalDamage
    }

    /**
     * Gets the blast reduction
     *
     * @param entity The entity to calculate damage for
     * @param damage The original damage
     * @param explosion The explosion
     * @return The blast reduction
     */
    private fun getBlastReduction(entity: EntityLivingBase, damage: Float, explosion: Explosion?): Float {
        var reductedDamage = damage

        if (entity is EntityPlayer) {
            val ds = DamageSource.causeExplosionDamage(explosion)
            reductedDamage = CombatRules.getDamageAfterAbsorb(reductedDamage, entity.totalArmorValue.toFloat(), entity.getEntityAttribute(
                SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat())

            val k = EnchantmentHelper.getEnchantmentModifierDamage(entity.armorInventoryList, ds)
            val f = MathHelper.clamp(k.toFloat(), 0.0f, 20.0f)
            reductedDamage *= 1.0f - f / 25.0f

            if (entity.isPotionActive(MobEffects.WEAKNESS)) {
                reductedDamage -= reductedDamage / 4
            }

            reductedDamage = reductedDamage.coerceAtLeast(0.0f)
            return reductedDamage
        }

        reductedDamage = CombatRules.getDamageAfterAbsorb(
            reductedDamage, entity.totalArmorValue.toFloat(), entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat()
        )
        return reductedDamage
    }

}