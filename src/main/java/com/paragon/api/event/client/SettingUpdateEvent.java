package com.paragon.api.event.client;

import com.paragon.api.setting.Setting;
import me.wolfsurge.cerauno.event.Event;

public class SettingUpdateEvent extends Event {

    private final Setting<?> setting;

    public SettingUpdateEvent(Setting<?> setting) {
        this.setting = setting;
    }

    public Setting<?> getSetting() {
        return setting;
    }

}
