package com.example.unifind.search

import com.example.unifind.model.Building
import com.example.unifind.model.BuildingStatus
import com.example.unifind.model.Facility
import com.example.unifind.model.Floor
import com.example.unifind.model.Gate
import com.example.unifind.model.Room
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * OOP Concept: Testing
 * Validates SearchEngine functionality independently of Android components.
 */
class SearchEngineTest {

    private lateinit var searchEngine: SearchEngine

    @Before
    fun setup() {
        val library = Facility("library", "Library", 0f, 0f, "LIBRARY")
        val gate = Gate("g1", "Gate 1", 0f, 0f)

        val roomHeb101 = Room("heb_101", "HEB101", "1st Floor", null, emptyList())
        val roomHeb102 = Room("heb_102", "HEB102", "1st Floor", null, emptyList())
        val hebBuilding = Building(
            "heb", "Higher Education Building", 0f, 0f, "Shared", BuildingStatus.ACTIVE,
            listOf(Floor("1st Floor", listOf(roomHeb101, roomHeb102)))
        )

        val cashier = Room("ermita_cashier", "Cashier", "1st Floor", "Administration", emptyList())
        val ermitaBuilding = Building(
            "ermita", "Ermita Building", 0f, 0f, "Administration", BuildingStatus.ACTIVE,
            listOf(Floor("1st Floor", listOf(cashier)))
        )

        val crimeLab = Room("casb_crime", "Crime Laboratory", "2nd Floor", "CCJE", listOf("CAS"))
        val casbBuilding = Building(
            "casb", "CAS Building", 0f, 0f, "CAS", BuildingStatus.ACTIVE,
            listOf(Floor("2nd Floor", listOf(crimeLab)))
        )

        val stoTomas = Building(
            "sto", "Sto. Tomas Building", 0f, 0f, "Unassigned", BuildingStatus.UNDER_CONSTRUCTION,
            emptyList()
        )

        val locations = listOf(library, gate, hebBuilding, ermitaBuilding, casbBuilding, stoTomas)
        searchEngine = SearchEngine(locations)
    }

    @Test
    fun testSearchHeb101() {
        val results = searchEngine.search("heb 101")
        assertTrue(results.isNotEmpty())
        val roomMatch = results.find { it.room?.id == "heb_101" }
        assertTrue(roomMatch != null)
        assertEquals("Higher Education Building - 1st Floor", roomMatch?.subtitle)
    }

    @Test
    fun testSearchCashier() {
        val results = searchEngine.search("cashier")
        assertTrue(results.isNotEmpty())
        assertEquals("Cashier", results.first().title)
    }

    @Test
    fun testSearchCrime() {
        val results = searchEngine.search("crime")
        assertTrue(results.isNotEmpty())
        assertEquals("Crime Laboratory", results.first().title)
    }

    @Test
    fun testSearchLibrary() {
        val results = searchEngine.search("library")
        assertTrue(results.isNotEmpty())
        assertEquals("Library", results.first().title)
    }

    @Test
    fun testBlankQuery() {
        val results = searchEngine.search("   ")
        assertTrue(results.isEmpty())
    }

    @Test
    fun testRanking() {
        // "HEB101" should exactly match room "HEB101" (rank 0)
        // It will also match "Higher Education Building" via building deep match, which should be lower rank.
        val results = searchEngine.search("HEB101")
        assertEquals(2, results.size)
        assertEquals("HEB101", results[0].title) // Exact match first
        assertEquals("Higher Education Building", results[1].title) // Contains/building match next
    }
}
