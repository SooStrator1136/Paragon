package com.paragon.client.systems.module.impl.movement;

import com.paragon.api.util.player.PlayerUtil;
import com.paragon.api.util.string.StringUtil;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.Category;
import com.paragon.client.systems.module.setting.Setting;

/**
 * @author Wolfsurge
 */
public class Sprint extends Module {

    public static Sprint INSTANCE;

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.LEGIT)
            .setDescription("The mode to sprint in");

    public static Setting<Boolean> onlyWhenMoving = new Setting<>("WhenMoving", true)
            .setDescription("Only omni sprint when actually moving")
            .setVisibility(() -> mode.getValue().equals(Mode.OMNI));

    public Sprint() {
        super("Sprint", Category.MOVEMENT, "Automatically sprint");

        INSTANCE = this;
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

        switch (mode.getValue()) {
            case OMNI:
                if (onlyWhenMoving.getValue()) {
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
        return " " + StringUtil.getFormattedText(mode.getValue());
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
