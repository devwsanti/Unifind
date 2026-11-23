package com.example.unifind.ui.directions

import com.example.unifind.model.Room
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * OOP Concept: Utility / Pure Function
 * Formats route distance, time, start description, and arrival instructions for display.
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

    fun formatStartText(startLocationName: String, startRoom: Room? = null): String {
        return if (startRoom != null) {
            "Start at $startLocationName (from ${startRoom.floorLabel} - ${startRoom.name})"
        } else {
            "Start at $startLocationName"
        }
    }

    fun formatArrivalLines(destinationLocationName: String, destinationRoom: Room? = null): List<String> {
        return if (destinationRoom != null) {
            listOf(
                "Arrive at $destinationLocationName",
                "Go to ${destinationRoom.floorLabel} - ${destinationRoom.name}"
            )
        } else {
            listOf("Arrive at $destinationLocationName")
        }
    }
}
