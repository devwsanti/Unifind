package com.example.unifind.model

/**
 * OOP Concept: Inheritance / Polymorphism
 * Represents a specific facility on campus (e.g., LIBRARY, CAFETERIA, GYM).
 */
class Facility(
    id: String,
    name: String,
    mapX: Float,
    mapY: Float,
    val category: String,
    latitude: Double? = null,
    longitude: Double? = null
) : Location(id, name, mapX, mapY, latitude, longitude) {

    override fun describe(): String {
        return "Facility ($category): $name"
    }
}
