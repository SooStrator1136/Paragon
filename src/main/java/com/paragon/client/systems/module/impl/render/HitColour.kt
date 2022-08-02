package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.entity.EntityHighlightOnHitEvent;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;

import java.awt.*;

/**
 * @author Surge
 * @since 22/05/22
 */
public class HitColour extends Module {

    public static Setting<Color> colour = new Setting<>("Colour", new Color(185, 17, 255, 85))
            .setDescription("The highlight colour");

    public HitColour() {
        super("HitColour", Category.RENDER, "Change the colour entities are rendered in when hit");
    }

    @Listener
    public void onEntityHighlight(EntityHighlightOnHitEvent event) {
        event.cancel();
        event.setColour(colour.getValue());
    }

}
