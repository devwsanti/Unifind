package com.example.unifind.ui.directions

import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * OOP Concept: Utility / Pure Function
 * Formats route distance and time for display.
 */
object RouteSummaryFormatter {
    fun formatDistance(distanceMeters: Double): Int {
        return (distanceMeters / 5.0).roundToInt() * 5
    }

    fun formatSummary(distanceMeters: Double): String {
        val roundedDistance = formatDistance(distanceMeters)
        val minutes = ceil(distanceMeters / 1.4 / 60.0).toInt()
        val walkingMinutes = if (minutes < 1) 1 else minutes
        return "Approx. $roundedDistance m - about $walkingMinutes min walk"
    }
}