package com.example.unifind.routing

import com.example.unifind.model.Location
import com.example.unifind.search.SearchResult

/**
 * OOP Concept: Single Responsibility Principle / Encapsulation
 * Pure domain service responsible for ranking routable locations by walking distance.
 */
class DestinationRanker {

    /**
     * Ranks locations by walking distance in ascending order.
     * Unreachable locations (distanceMeters == null) are placed last.
     * Ties are broken deterministically by location name.
     */
    fun rank(
        locations: List<Location>,
        getDistanceMeters: (Location) -> Double?
    ): List<RankedDestination> {
        val list = locations.map { loc ->
            RankedDestination(loc, getDistanceMeters(loc))
        }
        return list.sortedWith(
            compareBy<RankedDestination> {
                it.distanceMeters == null
            }.thenBy {
                it.distanceMeters ?: Double.MAX_VALUE
            }.thenBy {
                it.location.name
            }
        )
    }

    /**
     * Ranks locations using a RoutePlanner and map coordinates.
     */
    fun rank(
        locations: List<Location>,
        routePlanner: RoutePlanner,
        mapX: Float,
        mapY: Float
    ): List<RankedDestination> {
        return rank(locations) { loc ->
            routePlanner.fromPoint(mapX, mapY, loc.id)?.distanceMeters
        }
    }

    /**
     * Ranks search results by text match (if query is not blank) and walking distance.
     * When query is empty, results are sorted purely by walking distance (unreachable last).
     * When query is not empty, results are sorted by best text match tier first, then walking distance.
     */
    fun rankSearchResults(
        results: List<SearchResult>,
        getDistanceMeters: (Location) -> Double?,
        query: String = ""
    ): List<RankedSearchResult> {
        val list = results.map { res ->
            RankedSearchResult(res, getDistanceMeters(res.location))
        }
        val normQuery = query.replace(Regex("[ .-]"), "").lowercase()
        return if (normQuery.isBlank()) {
            list.sortedWith(
                compareBy<RankedSearchResult> {
                    it.distanceMeters == null
                }.thenBy {
                    it.distanceMeters ?: Double.MAX_VALUE
                }.thenBy {
                    it.result.title
                }
            )
        } else {
            list.sortedWith(
                compareBy<RankedSearchResult> { item ->
                    val normTitle = item.result.title.replace(Regex("[ .-]"), "").lowercase()
                    when {
                        normTitle == normQuery -> 0
                        normTitle.startsWith(normQuery) -> 1
                        else -> 2
                    }
                }.thenBy {
                    it.distanceMeters == null
                }.thenBy {
                    it.distanceMeters ?: Double.MAX_VALUE
                }.thenBy {
                    it.result.title
                }
            )
        }
    }
}
