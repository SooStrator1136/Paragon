package com.paragon.client.systems.module.impl.render;

import com.paragon.api.event.render.gui.RenderChatEvent;
import com.paragon.api.module.Category;
import com.paragon.api.module.Module;
import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;

import java.awt.*;

/**
 * @author Surge
 * @since 19/06/22
 */
public class ClearChat extends Module {

    public static Setting<Color> colour = new Setting<>("Colour", new Color(0, 0, 0, 0))
            .setDescription("The colour of the chat background");

    public ClearChat() {
        super("ClearChat", Category.RENDER, "Removes the chat background");
    }

    @Listener
    public void onRenderChatBackground(RenderChatEvent event) {
        event.setColour(colour.getValue().getRGB());
    }

}
