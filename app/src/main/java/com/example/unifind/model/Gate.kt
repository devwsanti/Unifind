package com.example.unifind.model

/**
 * OOP Concept: Inheritance / Polymorphism
 * Represents a specific type of Location (a Gate on campus).
 */
class Gate(
    id: String,
    name: String,
    mapX: Float,
    mapY: Float,
    latitude: Double? = null,
    longitude: Double? = null
) : Location(id, name, mapX, mapY, latitude, longitude) {

    override fun describe(): String {
        return "Gate: $name"
    }
}
