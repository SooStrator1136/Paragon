package com.paragon.client.systems.module.impl.render;

import com.paragon.api.util.entity.EntityUtil;
import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class MobOwner extends Module {

    public MobOwner() {
        super("MobOwner", Category.RENDER, "Displays the owner of a mob");
    }

    @Override
    public void onRender3D() {
        mc.world.loadedEntityList.forEach(entity -> {
            if (entity instanceof IEntityOwnable) {
                String name = entity.getCustomNameTag().equals("") ? entity.getName() : entity.getCustomNameTag();
                String owner = ((IEntityOwnable) entity).getOwner() == null ? "No Owner" : Objects.requireNonNull(((IEntityOwnable) entity).getOwner()).getName();
                Vec3d vec = EntityUtil.getInterpolatedPosition(entity);

                if (entity instanceof AbstractHorse) {
                    RenderUtil.drawNametagText("Name: " + name + ", Speed: " + ((AbstractHorse) entity).getAIMoveSpeed() + ", Owner: " + owner, new Vec3d(vec.x, vec.y + 1.25, vec.z), -1);
                } else {
                    RenderUtil.drawNametagText("Name: " + name + ", Owner: " + owner, new Vec3d(vec.x, vec.y + 1.25, vec.z), -1);
                }
            }
        });
    }
}
