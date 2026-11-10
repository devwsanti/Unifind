package com.example.unifind.model

/**
 * OOP Concept: Abstract Class / Abstraction / Encapsulation
 * Serves as the base class for physical locations.
 * Encapsulation is achieved via Kotlin properties (private backing fields with generated getters).
 */
abstract class Location(
    val id: String,
    val name: String,
    val mapX: Float,
    val mapY: Float,
    val latitude: Double? = null,
    val longitude: Double? = null
) : Searchable, Navigable {

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
