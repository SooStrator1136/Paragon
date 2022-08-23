package com.paragon.api.event.client

import com.paragon.api.setting.Setting
import com.paragon.bus.event.Event

/**
 * @author Surge
 */
class SettingUpdateEvent(val setting: Setting<*>) : Event()