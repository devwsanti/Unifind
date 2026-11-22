package com.example.unifind.model

/**
 * OOP Concept: Abstract Class / Abstraction / Encapsulation
 * Serves as the base class for physical locations.
 * Encapsulation is achieved via Kotlin properties (private backing fields with generated getters).
 */
abstract class Location(
    val id: String,
    val name: String,
    mapX: Float,
    mapY: Float,
    val latitude: Double? = null,
    val longitude: Double? = null
) : Searchable, Navigable {

    val originalMapX: Float = mapX
    val originalMapY: Float = mapY

    var mapX: Float = mapX
        private set

    var mapY: Float = mapY
        private set

    /**
     * OOP Concept: Encapsulation
     * Controlled update of location map coordinates with clamping to [0, 1].
     */
    fun updateCoordinates(newX: Float, newY: Float) {
        mapX = newX.coerceIn(0f, 1f)
        mapY = newY.coerceIn(0f, 1f)
    }

    /**
     * OOP Concept: Encapsulation
     * Resets location map coordinates back to initial values from campus.json.
     */
    fun resetCoordinates() {
        mapX = originalMapX
        mapY = originalMapY
    }

    /**
     * OOP Concept: Polymorphism
     * Abstract method that must be implemented by subclasses to provide a specific description.
     */
    abstract fun describe(): String

    override fun matches(query: String): Boolean {
        val normName = name.replace(Regex("[ .-]"), "").lowercase()
        val normQuery = query.replace(Regex("[ .-]"), "").lowercase()
        return normName.contains(normQuery)
    }

    override fun getEntryPoint(): Pair<Float, Float> {
        return Pair(mapX, mapY)
    }
}
