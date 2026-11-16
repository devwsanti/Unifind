package com.example.unifind.ui.directions

import kotlin.math.ceil

/**
 * OOP Concept: Utility / Pure Function
 * Formats route distance and time for display.
 */
object RouteSummaryFormatter {
    fun formatSummary(distanceMeters: Double): String {
        val roundedDistance = (Math.round(distanceMeters / 5.0) * 5.0).toInt()
        val minutes = ceil(distanceMeters / 1.4 / 60.0).toInt()
        val walkingMinutes = if (minutes < 1) 1 else minutes
        return "Approx. $roundedDistance m - about $walkingMinutes min walk"
    }
}