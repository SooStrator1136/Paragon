package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.entity.CameraClipEvent;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;

/**
 * @author Surge
 * @since 14/05/22
 */
public class ViewClip extends Module {

    public static ViewClip INSTANCE;

    public static Setting<Double> distance = new Setting<>("Distance", 10.0, 1.0, 20.0, 0.1)
            .setDescription("How far your camera is from your body");

    public ViewClip() {
        super("ViewClip", Category.RENDER, "Lets your third person view clip to the world");

        INSTANCE = this;
    }

    @Listener
    public void onCameraClip(CameraClipEvent event) {
        event.cancel();
        event.setDistance(distance.getValue());
    }

}
