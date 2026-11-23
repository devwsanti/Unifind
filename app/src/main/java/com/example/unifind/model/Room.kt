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

    /**
     * OOP Concept: Helper / Encapsulation
     * Formats secondary display text for a room given its building name.
     */
    fun describe(buildingName: String): String {
        return "$buildingName - $floorLabel"
    }

    override fun matches(query: String): Boolean {
        val normName = name.replace(Regex("[ .-]"), "").lowercase()
        val normQuery = query.replace(Regex("[ .-]"), "").lowercase()
        if (normName.contains(normQuery)) return true
        
        val normCollege = ownerCollege?.replace(Regex("[ .-]"), "")?.lowercase() ?: ""
        if (normCollege.contains(normQuery) && normQuery.isNotEmpty()) return true
        
        return sharedWith.any {
            it.replace(Regex("[ .-]"), "").lowercase().contains(normQuery)
        }
    }
}
