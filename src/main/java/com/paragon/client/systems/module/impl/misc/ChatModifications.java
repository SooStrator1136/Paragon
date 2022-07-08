package com.paragon.client.systems.module.impl.misc;

import com.paragon.Paragon;
import com.paragon.api.module.Module;
import com.paragon.api.module.Category;
import com.paragon.api.setting.Setting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Wolfsurge
 */
public class ChatModifications extends Module {

    public static ChatModifications INSTANCE;

    public static Setting<Boolean> coloured = new Setting<>("Coloured", false)
            .setDescription("Adds a '>' before the message");

    public static Setting<Boolean> suffix = new Setting<>("Suffix", true)
            .setDescription("Adds a Paragon suffix to the end of the message");

    public ChatModifications() {
        super("ChatModifications", Category.MISC, "Changes the way you send messages");

        INSTANCE = this;
    }

    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        if (Paragon.INSTANCE.getCommandManager().startsWithPrefix(event.getMessage())) {
            return;
        }

        if (coloured.getValue()) {
            event.setMessage("> " + event.getMessage());
        }

        if (suffix.getValue()) {
            event.setMessage(event.getMessage() + " | Paragon");
        }
    }

}
