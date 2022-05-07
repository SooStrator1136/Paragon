package com.paragon.client.systems.module.impl.misc;

import com.paragon.Paragon;
import com.paragon.api.event.client.ModuleToggleEvent;
import com.paragon.api.event.combat.PlayerDeathEvent;
import com.paragon.api.event.combat.TotemPopEvent;
import com.paragon.client.managers.CommandManager;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.setting.Setting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Wolfsurge
 */
public class Notifier extends Module {

    private final Setting<Boolean> moduleEnabled = new Setting<>("Module Toggle", false)
            .setDescription("Notifies you when you toggle a module");

    private final Setting<Boolean> pop = new Setting<>("Pop", true)
            .setDescription("Notifies you when a player pops a totem");

    private final Setting<Boolean> death = new Setting<>("Death", true)
            .setDescription("Notifies you when a player dies");

    private final Setting<Boolean> noPops = new Setting<>("No Pops", true)
            .setDescription("Notifies you even if the player hasn't popped any totems")
            .setParentSetting(death);

    public Notifier() {
        super("Notifier", ModuleCategory.MISC, "Notifies you when events happen");
        this.addSettings(moduleEnabled, pop, death);
    }

    @Listener
    public void onModuleToggle(ModuleToggleEvent moduleToggleEvent) {
        if (moduleEnabled.getValue()) {
            CommandManager.sendClientMessage(moduleToggleEvent.getModule().getName() + " was " + (!moduleToggleEvent.getModule().isEnabled() ? TextFormatting.RED + "Disabled!" : TextFormatting.GREEN + "Enabled!"), false);
        }
    }

    @Listener
    public void onTotemPop(TotemPopEvent event) {
        if (pop.getValue()) {
            CommandManager.sendClientMessage(event.getPlayer().getName() + " has popped " + Paragon.INSTANCE.getPopManager().getPops(event.getPlayer()) + " totems!", false);
        }
    }

    @Listener
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (death.getValue() && !noPops.getValue()) {
            if (!noPops.getValue()) {
                if (event.getPops() == 0) {
                    return;
                }
            }

            CommandManager.sendClientMessage(event.getEntityPlayer().getName() + " has died after popping " + event.getPops() + " totems!", false);
        }
    }

}
