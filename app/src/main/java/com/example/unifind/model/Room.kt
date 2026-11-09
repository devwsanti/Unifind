package com.example.unifind.model

/**
 * OOP Concept: Encapsulation
 * Represents a room. Properties are encapsulated (Kotlin generates private fields and public getters).
 */
class Room(
    val id: String,
    val name: String,
    val floorLabel: String,
    val ownerCollege: String?,
    val sharedWith: List<String>
) : Searchable {

    override fun matches(query: String): Boolean {
        return name.contains(query, ignoreCase = true)
    }
}
