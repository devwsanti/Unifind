package com.example.unifind.routing

/**
 * OOP Concept: Data Transfer Object (DTO) / Entity
 * Represents a point on the map graph for routing purposes.
 */
data class WaypointNode(
    val id: String,
    val mapX: Float,
    val mapY: Float,
    val locationId: String? = null
)
