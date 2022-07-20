package com.paragon.client.ui.configuration.window.element.setting

import com.paragon.api.setting.Setting
import com.paragon.client.ui.configuration.window.element.Element
import com.paragon.client.ui.configuration.window.window.Window

/**
 * @author Wolfsurge
 */
open class SettingElement<T>(val setting: Setting<T>, window: Window, x: Float, y: Float, width: Float, height: Float) : Element(window, x, y, width, height) {

}