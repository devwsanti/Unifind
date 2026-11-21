package com.example.unifind.routing

import com.example.unifind.model.Building
import com.example.unifind.model.BuildingStatus
import com.example.unifind.model.Gate
import com.example.unifind.model.Room
import com.example.unifind.search.SearchResult
import org.junit.Assert.assertEquals
import org.junit.Test

class DestinationRankerTest {

    private val ranker = DestinationRanker()

    @Test
    fun rank_sortsAscendingByDistance() {
        val g1 = Gate("g1", "Gate 1", 0.1f, 0.1f)
        val g2 = Gate("g2", "Gate 2", 0.2f, 0.2f)
        val g3 = Gate("g3", "Gate 3", 0.3f, 0.3f)
        val locations = listOf(g1, g2, g3)

        val distances = mapOf(
            "g1" to 300.0,
            "g2" to 50.0,
            "g3" to 120.0
        )

        val ranked = ranker.rank(locations) { loc -> distances[loc.id] }

        assertEquals(listOf("g2", "g3", "g1"), ranked.map { it.location.id })
        assertEquals(listOf(50.0, 120.0, 300.0), ranked.map { it.distanceMeters })
    }

    @Test
    fun rank_placesUnreachableDestinationsLast() {
        val g1 = Gate("g1", "Gate 1", 0.1f, 0.1f)
        val g2 = Gate("g2", "Gate 2", 0.2f, 0.2f)
        val g3 = Gate("g3", "Gate 3", 0.3f, 0.3f)
        val locations = listOf(g1, g2, g3)

        val distances = mapOf(
            "g1" to 100.0,
            "g2" to null,
            "g3" to 50.0
        )

        val ranked = ranker.rank(locations) { loc -> distances[loc.id] }

        assertEquals(listOf("g3", "g1", "g2"), ranked.map { it.location.id })
        assertEquals(listOf(50.0, 100.0, null), ranked.map { it.distanceMeters })
    }

    @Test
    fun rank_emptyList_returnsEmpty() {
        val ranked = ranker.rank(emptyList()) { 10.0 }
        assertEquals(0, ranked.size)
    }

    @Test
    fun rankSearchResults_ranksRoomByBuildingDistance() {
        val b1 = Building("b1", "Building One", 0.1f, 0.1f, "Col", BuildingStatus.ACTIVE, emptyList())
        val b2 = Building("b2", "Building Two", 0.2f, 0.2f, "Col", BuildingStatus.ACTIVE, emptyList())
        val r1 = Room("r1", "CTEB101", "1st Floor", null, emptyList())
        val r2 = Room("r2", "CTEB201", "2nd Floor", null, emptyList())

        val resRoom1 = SearchResult(location = b1, room = r1, floorLabel = "1st Floor", title = "CTEB101", subtitle = "Building One, 1st Floor")
        val resRoom2 = SearchResult(location = b2, room = r2, floorLabel = "2nd Floor", title = "CTEB201", subtitle = "Building Two, 2nd Floor")

        val distances = mapOf(
            "b1" to 50.0,
            "b2" to 200.0
        )

        val ranked = ranker.rankSearchResults(listOf(resRoom2, resRoom1), getDistanceMeters = { loc -> distances[loc.id] })

        assertEquals("CTEB101", ranked[0].result.title)
        assertEquals(50.0, ranked[0].distanceMeters)
        assertEquals("CTEB201", ranked[1].result.title)
        assertEquals(200.0, ranked[1].distanceMeters)
    }
}
