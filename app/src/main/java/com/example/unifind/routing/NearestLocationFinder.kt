package com.example.unifind.routing

import com.example.unifind.model.Location
import kotlin.math.hypot

/**
 * OOP Concept: Single Responsibility Principle / Utility / Encapsulation
 * Pure utility class responsible for finding the nearest named Location to map coordinates.
 */
class NearestLocationFinder(
    private val imageWidth: Int = 941,
    private val imageHeight: Int = 1171
) {
    /**
     * Finds the closest location to (mapX, mapY).
     * If list is empty, returns null.
     * Ties are broken deterministically by location ID.
     */
    fun findNearestLocation(locations: List<Location>, mapX: Float, mapY: Float): Location? {
        if (locations.isEmpty()) return null
        return locations.minWithOrNull(
            compareBy<Location> { loc ->
                val dx = (mapX - loc.mapX) * imageWidth
                val dy = (mapY - loc.mapY) * imageHeight
                hypot(dx.toDouble(), dy.toDouble())
            }.thenBy { it.id }
        )
    }
}
