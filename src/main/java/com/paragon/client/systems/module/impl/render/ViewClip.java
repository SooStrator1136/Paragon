package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.entity.CameraClipEvent;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;

/**
 * @author Wolfsurge
 * @since 14/05/22
 */
public class ViewClip extends Module {

    private final Setting<Double> distance = new Setting<>("Distance", 10.0, 1.0, 20.0, 0.1)
            .setDescription("How far your camera is from your body");

    public ViewClip() {
        super("ViewClip", Category.RENDER, "Lets your third person view clip to the world");
        this.addSettings(distance);
    }

    @Listener
    public void onCameraClip(CameraClipEvent event) {
        event.cancel();
        event.setDistance(distance.getValue());
    }

}
