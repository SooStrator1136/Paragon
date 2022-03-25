package com.paragon.client.managers.rotation;

import com.paragon.api.util.Wrapper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.Comparator;

public class RotationManager implements Wrapper {

    private final ArrayList<Rotation> rotationsQueue = new ArrayList<>();

    public RotationManager() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (nullCheck()) {
            return;
        }

        if (!rotationsQueue.isEmpty()) {
            rotationsQueue.sort(Comparator.comparing(rotation -> rotation.getPriority().getPriority()));

            for (Rotation rotation : rotationsQueue) {
                rotation.doRotate();
            }

            rotationsQueue.clear();
        }
    }

    public void addRotation(Rotation rotation) {
        this.rotationsQueue.add(rotation);
    }

}
