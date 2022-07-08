package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.AspectEvent;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;

/**
 * @author Wolfsurge
 * @since 23/05/22
 */
public class AspectRatio extends Module {

    public static Setting<Float> ratio = new Setting<>("Ratio", 1f, 0.5f, 10f, 0.01f)
            .setDescription("The ratio of the screen");

    public AspectRatio() {
        super("AspectRatio", Category.RENDER, "Changes the aspect ratio of the game");
    }

    @Listener
    public void onAspectRatioEvent(AspectEvent event) {
        event.cancel();
        event.setRatio(ratio.getValue());
    }

}
