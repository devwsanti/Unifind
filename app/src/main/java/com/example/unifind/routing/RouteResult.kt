package com.example.unifind.routing

import kotlin.math.ceil

/**
 * OOP Concept: Data Transfer Object (DTO)
 * Represents the calculated path output containing total metrics and node sequence.
 */
data class RouteResult(
    val nodes: List<WaypointNode>,
    val distanceMeters: Double,
    val entranceNode: WaypointNode
) {
    val walkingMinutes: Int
        get() {
            if (distanceMeters <= 0) return 0
            val minutes = ceil(distanceMeters / 1.4 / 60.0).toInt()
            return if (minutes < 1) 1 else minutes
        }
}
