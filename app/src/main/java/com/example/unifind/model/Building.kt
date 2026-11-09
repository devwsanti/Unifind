package com.example.unifind.model

/**
 * OOP Concept: Inheritance / Polymorphism / Composition
 * Extends Location and acts as a composite containing Floor objects.
 */
class Building(
    id: String,
    name: String,
    mapX: Float,
    mapY: Float,
    val college: String,
    val status: BuildingStatus,
    val floors: List<Floor>,
    latitude: Double? = null,
    longitude: Double? = null
) : Location(id, name, mapX, mapY, latitude, longitude) {

    override fun describe(): String {
        return if (status == BuildingStatus.UNDER_CONSTRUCTION) {
            "$name: Coming soon"
        } else {
            "Building: $name, College: $college"
        }
    }

    override fun matches(query: String): Boolean {
        // Match the building name itself
        if (super.matches(query)) return true

        // Match any room inside the building
        return floors.any { floor ->
            floor.rooms.any { room -> room.matches(query) }
        }
    }
}
