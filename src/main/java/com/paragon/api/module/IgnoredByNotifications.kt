package com.paragon.api.module

/**
 * This annotation is used to mark a module to be ignored by the notification system.
 * Examples: ClickGUI, HUDEditor
 *
 * @author Wolfsurge
 */
@Target(AnnotationTarget.CLASS)
annotation class IgnoredByNotifications