package com.example.unifind.routing

import com.example.unifind.model.Building
import com.example.unifind.model.BuildingStatus
import com.example.unifind.model.Gate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NearestLocationFinderTest {

    private val finder = NearestLocationFinder()

    @Test
    fun findNearestLocation_returnsClosestLocation() {
        val gate = Gate("g1", "Gate 1", 0.1f, 0.1f)
        val bldg = Building("b1", "Building 1", 0.8f, 0.8f, "Engineering", BuildingStatus.ACTIVE, emptyList())
        val locations = listOf(gate, bldg)

        val result = finder.findNearestLocation(locations, 0.12f, 0.11f)
        assertEquals(gate, result)
    }

    @Test
    fun findNearestLocation_emptyList_returnsNull() {
        val result = finder.findNearestLocation(emptyList(), 0.5f, 0.5f)
        assertNull(result)
    }

    @Test
    fun findNearestLocation_deterministicTies() {
        val g1 = Gate("a_gate", "Gate A", 0.5f, 0.5f)
        val g2 = Gate("b_gate", "Gate B", 0.5f, 0.5f)
        val locations = listOf(g2, g1)

        val result1 = finder.findNearestLocation(locations, 0.5f, 0.5f)
        val result2 = finder.findNearestLocation(listOf(g1, g2), 0.5f, 0.5f)

        assertEquals(g1, result1)
        assertEquals(g1, result2)
    }
}
