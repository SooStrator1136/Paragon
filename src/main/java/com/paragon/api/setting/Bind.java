package com.paragon.api.setting;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * @author Wolfsurge 
 * @since 09/06/22
 */
public class Bind {

    // The button index
    private int buttonCode;
    
    // Input device
    private Device device;
    
    // Prevent possible spam
    private boolean alreadyPressed;

    public Bind(int buttonCode, Device device) {
        this.buttonCode = buttonCode;
        this.device = device;
    }

    public boolean isPressed() {
        if (buttonCode == 0) {
            return false;
        }

        // Our bind is pressed
        boolean pressed = Keyboard.isKeyDown(buttonCode) && device.equals(Device.KEYBOARD) || Mouse.isButtonDown(buttonCode) && device.equals(Device.MOUSE);

        if (pressed) {
            // We haven't already pressed the key
            if (!alreadyPressed) {
                this.alreadyPressed = true;
                return true;
            } else {
                return false;
            }
        } else {
            this.alreadyPressed = false;
            return false;
        }
    }

    public enum Device {
        /**
         * A key on the keyboard
         */
        KEYBOARD,

        /**
         * A mouse button
         */
        MOUSE
    }

    /**
     * Gets the button name for the GUI
     * @return The button name
     */
    public String getButtonName() {
        return device.equals(Device.KEYBOARD) ? Keyboard.getKeyName(buttonCode) : Mouse.getButtonName(buttonCode);
    }

    /**
     * Gets the button code
     * @return The button code
     */
    public int getButtonCode() {
        return buttonCode;
    }

    /**
     * Sets the button code
     * @param newCode The new button code
     */
    public void setButtonCode(int newCode) {
        this.buttonCode = newCode;
    }

    /**
     * Gets the input device
     * @return The input device
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Sets the input device
     * @param device The new input device
     */
    public void setDevice(Device device) {
        this.device = device;
    }

}
