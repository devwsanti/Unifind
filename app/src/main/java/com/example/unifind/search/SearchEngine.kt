package com.example.unifind.search

import com.example.unifind.model.Building
import com.example.unifind.model.BuildingStatus
import com.example.unifind.model.Facility
import com.example.unifind.model.Gate
import com.example.unifind.model.Location
import com.example.unifind.model.Searchable

/**
 * OOP Concept: Aggregation / Strategy
 * Aggregates Locations and performs polymorphic searches.
 */
class SearchEngine(private val locations: List<Location>) {

    // Pre-flatten everything into searchable pairs so we don't need type-checking later for the search mapping
    private val searchables = mutableListOf<Pair<Searchable, SearchResult>>()

    init {
        locations.forEach { loc ->
            val locSubtitle = if (loc is Building && loc.status == BuildingStatus.UNDER_CONSTRUCTION) {
                "Coming soon"
            } else {
                loc.describe()
            }

            searchables.add(loc to SearchResult(
                location = loc,
                room = null,
                floorLabel = null,
                title = loc.name,
                subtitle = locSubtitle
            ))

            if (loc is Building) {
                loc.floors.forEach { floor ->
                    floor.rooms.forEach { room ->
                        searchables.add(room to SearchResult(
                            location = loc,
                            room = room,
                            floorLabel = floor.label,
                            title = room.name,
                            subtitle = "${loc.name} - ${floor.label}"
                        ))
                    }
                }
            }
        }
    }

    private fun String.normalizeForSearch(): String {
        return this.replace(Regex("[ .-]"), "").lowercase()
    }

    fun search(query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        // Polymorphic call to matches() without checking if it is a Location or Room
        val matchedPairs = searchables.filter { (searchable, _) ->
            searchable.matches(query)
        }

        val normQuery = query.normalizeForSearch()

        return matchedPairs.map { it.second }
            .sortedBy { result ->
                val normTitle = result.title.normalizeForSearch()
                when {
                    normTitle == normQuery -> 0
                    normTitle.startsWith(normQuery) -> 1
                    else -> 2
                }
            }
    }

    fun filterByCollege(college: String): List<SearchResult> {
        val normCollege = college.normalizeForSearch()
        return searchables.map { it.second }.filter { result ->
            val b = result.location as? Building
            val r = result.room
            if (r != null) {
                r.ownerCollege?.normalizeForSearch() == normCollege ||
                r.sharedWith.any { it.normalizeForSearch() == normCollege }
            } else if (b != null) {
                b.college.normalizeForSearch() == normCollege
            } else {
                false
            }
        }
    }

    fun filterByType(type: LocationType): List<SearchResult> {
        return searchables.map { it.second }.filter { result ->
            when (type) {
                LocationType.GATE -> result.location is Gate && result.room == null
                LocationType.FACILITY -> result.location is Facility && result.room == null
                LocationType.BUILDING -> result.location is Building && result.room == null
                LocationType.ROOM -> result.room != null
            }
        }
    }
}
