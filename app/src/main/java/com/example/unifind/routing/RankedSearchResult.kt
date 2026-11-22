package com.example.unifind.routing

import com.example.unifind.search.SearchResult

/**
 * OOP Concept: Data Transfer Object (DTO) / Encapsulation
 * Encapsulates a SearchResult paired with its calculated walking distance from the user's location.
 */
data class RankedSearchResult(
    val result: SearchResult,
    val distanceMeters: Double?
)
