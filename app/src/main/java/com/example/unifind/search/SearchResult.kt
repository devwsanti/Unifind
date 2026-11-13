package com.example.unifind.search

import com.example.unifind.model.Location
import com.example.unifind.model.Room

/**
 * OOP Concept: Data Transfer Object (DTO) / Composition
 * Encapsulates the display data for a search match.
 */
data class SearchResult(
    val location: Location,
    val room: Room?,
    val floorLabel: String?,
    val title: String,
    val subtitle: String
)
