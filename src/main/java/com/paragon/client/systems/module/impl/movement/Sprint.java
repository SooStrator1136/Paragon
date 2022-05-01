package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.string.EnumFormatter;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.settings.impl.BooleanSetting;
import com.paragon.client.systems.module.settings.impl.ModeSetting;

/**
 * @author Wolfsurge
 */
public class Sprint extends Module {

    private final ModeSetting<Mode> mode = new ModeSetting<>("Mode", "The mode to sprint in", Mode.LEGIT);

    private final BooleanSetting onlyWhenMoving = (BooleanSetting) new BooleanSetting("When Moving", "Only omni sprint when actually moving", true).setVisiblity(() -> mode.getCurrentMode() == Mode.OMNI);

    public Sprint() {
        super("Sprint", ModuleCategory.MOVEMENT, "Automatically sprint");
        this.addSettings(mode, onlyWhenMoving);
    }

    @Override
    public void onDisable() {
        // Stop us sprinting when we disable
        mc.player.setSprinting(false);
    }

    @Override
    public void onTick() {
        if (nullCheck()) {
            return;
        }

        switch (mode.getCurrentMode()) {
            case OMNI:
                if (onlyWhenMoving.isEnabled()) {
                    // If we aren't moving, do not make us sprint
                    if (!PlayerUtil.isMoving()) {
                        return;
                    }
                }

                // Make us sprint
                mc.player.setSprinting(true);
                break;
            case LEGIT:
                // Make us sprint if we are pressing forward
                mc.player.setSprinting(mc.player.movementInput.moveForward > 0);
                break;
        }
    }

    @Override
    public String getArrayListInfo() {
        return " " + EnumFormatter.getFormattedText(mode.getCurrentMode());
    }

    public enum Mode {
        /**
         * Only sprint if you are walking forwards
         */
        LEGIT,

        /**
         * Always sprint
         */
        OMNI
    }

}
