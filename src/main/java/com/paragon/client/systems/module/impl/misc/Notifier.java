package com.paragon.client.systems.module.impl.misc;

import com.paragon.Paragon;
import com.paragon.api.event.client.ModuleToggleEvent;
import com.paragon.api.event.combat.PlayerDeathEvent;
import com.paragon.api.event.combat.TotemPopEvent;
import com.paragon.client.managers.CommandManager;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import me.wolfsurge.cerauno.listener.Listener;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Wolfsurge
 */
public class Notifier extends Module {

    private final BooleanSetting moduleEnabled = new BooleanSetting("Module Toggle", "Notifies you when you toggle a module", false);
    private final BooleanSetting pop = new BooleanSetting("Pop", "Notifies you when a player pops a totem", true);
    private final BooleanSetting death = new BooleanSetting("Death", "Notifies you when a player dies", true);
    private final BooleanSetting noPops = (BooleanSetting) new BooleanSetting("No Pops", "Notifies you even if the player hasn't popped any totems", true).setParentSetting(death);

    public Notifier() {
        super("Notifier", ModuleCategory.MISC, "Notifies you when events happen");
        this.addSettings(moduleEnabled, pop, death);
    }

    @Listener
    public void onModuleToggle(ModuleToggleEvent moduleToggleEvent) {
        if (moduleEnabled.isEnabled()) {
            CommandManager.sendClientMessage(moduleToggleEvent.getModule().getName() + " was " + (!moduleToggleEvent.getModule().isEnabled() ? TextFormatting.RED + "Disabled!" : TextFormatting.GREEN + "Enabled!"), false);
        }
    }

    @Listener
    public void onTotemPop(TotemPopEvent event) {
        if (pop.isEnabled()) {
            CommandManager.sendClientMessage(event.getPlayer().getName() + " has popped " + Paragon.INSTANCE.getPopManager().getPops(event.getPlayer()) + " totems!", false);
        }
    }

    @Listener
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (death.isEnabled()) {
            if (!noPops.isEnabled()) {
                if (event.getPops() == 0) {
                    return;
                }
            }

            CommandManager.sendClientMessage(event.getEntityPlayer().getName() + " has died after popping " + event.getPops() + " totems!", false);
        }
    }

}
