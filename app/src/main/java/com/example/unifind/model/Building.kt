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
        if (status == BuildingStatus.UNDER_CONSTRUCTION) {
            return "Coming soon"
        }
        val collegeLabel = if (college.isBlank() ||
            college.equals("Shared", ignoreCase = true) ||
            college.equals("Unassigned", ignoreCase = true) ||
            college.equals("None", ignoreCase = true)
        ) {
            "Shared building"
        } else {
            college
        }
        val floorCount = floors.size
        val roomCount = floors.sumOf { it.rooms.size }
        val floorStr = "$floorCount floor${if (floorCount == 1) "" else "s"}"
        val roomStr = "$roomCount room${if (roomCount == 1) "" else "s"}"
        return "$collegeLabel - $floorStr - $roomStr"
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
