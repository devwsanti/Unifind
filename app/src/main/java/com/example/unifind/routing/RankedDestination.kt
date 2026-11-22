package com.example.unifind.routing

import com.example.unifind.model.Location

/**
 * OOP Concept: Data Transfer Object (DTO) / Encapsulation
 * Encapsulates a location paired with its calculated walking distance from the user's location.
 */
data class RankedDestination(
    val location: Location,
    val distanceMeters: Double?
)
