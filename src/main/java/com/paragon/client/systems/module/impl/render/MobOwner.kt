package com.paragon.client.systems.module.impl.render

import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.util.entity.EntityUtil
import com.paragon.api.util.render.RenderUtil.drawNametagText
import net.minecraft.entity.Entity
import net.minecraft.entity.IEntityOwnable
import net.minecraft.entity.passive.AbstractHorse
import net.minecraft.util.math.Vec3d

object MobOwner : Module("MobOwner", Category.RENDER, "Displays the owner of a mob") {

    override fun onRender3D() {
        minecraft.world.loadedEntityList.forEach { entity: Entity ->
            if (entity is IEntityOwnable) {
                val name = if (entity.customNameTag == "") entity.name else entity.customNameTag
                val owner = if ((entity as IEntityOwnable).owner == null) {
                    "No Owner"
                } else (entity as IEntityOwnable).owner?.name

                val vec = EntityUtil.getInterpolatedPosition(entity)

                if (entity is AbstractHorse) {
                    drawNametagText(
                        "Name: " + name + ", Speed: " + (entity as AbstractHorse).aiMoveSpeed + ", Owner: " + owner,
                        Vec3d(vec.x, vec.y + 1.25, vec.z),
                        -1
                    )
                } else {
                    drawNametagText("Name: $name, Owner: $owner", Vec3d(vec.x, vec.y + 1.25, vec.z), -1)
                }
            }
        }
    }

}