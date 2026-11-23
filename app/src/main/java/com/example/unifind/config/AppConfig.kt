package com.example.unifind.config
// Declares that this file belongs to the config module of the UniFind application
/**
 * Global application configuration settings.
 * OOP Concept: Encapsulation / Singleton (Grouping application configuration constants in a single read-only object).
 *
 * true = developer tools visible on the Map tab (Calibrate mode, Edit paths, Validate graph). false = user mode: no developer tools.
 */
object AppConfig {
    const val DEVELOPER_MODE = true
    // You directly reference it anywhere in the project as AppConfig.DEVELOPER_MODE
}
