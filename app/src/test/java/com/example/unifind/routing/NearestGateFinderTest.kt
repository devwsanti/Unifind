package com.example.unifind.routing

import com.example.unifind.model.Gate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NearestGateFinderTest {

    private val gate1 = Gate("g1", "Gate 1", 0f, 0f)
    private val gate2 = Gate("g2", "Gate 2", 0f, 0f)
    private val gate3 = Gate("g3", "Gate 3", 0f, 0f)

    @Test
    fun testPicksShortestGate() {
        val finder = NearestGateFinder { fromGateId, _ ->
            when (fromGateId) {
                "g1" -> RouteResult(listOf(WaypointNode("g1", 0f, 0f)), 300.0, WaypointNode("dest", 0f, 0f))
                "g2" -> RouteResult(listOf(WaypointNode("g2", 0f, 0f)), 150.0, WaypointNode("dest", 0f, 0f))
                "g3" -> RouteResult(listOf(WaypointNode("g3", 0f, 0f)), 450.0, WaypointNode("dest", 0f, 0f))
                else -> null
            }
        }

        val result = finder.findNearestGate(listOf(gate1, gate2, gate3), "dest_loc")
        assertNotNull(result)
        assertEquals("Gate 2", result!!.bestGate.name)
        assertEquals(150.0, result.bestRoute.distanceMeters, 0.01)

        val sortedGateNames = result.allOptions.map { it.gate.name }
        assertEquals(listOf("Gate 2", "Gate 1", "Gate 3"), sortedGateNames)
    }

    @Test
    fun testHandlesGateWithNoPath() {
        val finder = NearestGateFinder { fromGateId, _ ->
            when (fromGateId) {
                "g1" -> RouteResult(listOf(WaypointNode("g1", 0f, 0f)), 200.0, WaypointNode("dest", 0f, 0f))
                "g2" -> null
                "g3" -> RouteResult(listOf(WaypointNode("g3", 0f, 0f)), 400.0, WaypointNode("dest", 0f, 0f))
                else -> null
            }
        }

        val result = finder.findNearestGate(listOf(gate1, gate2, gate3), "dest_loc")
        assertNotNull(result)
        assertEquals("Gate 1", result!!.bestGate.name)
        assertEquals(200.0, result.bestRoute.distanceMeters, 0.01)

        assertEquals(3, result.allOptions.size)
        assertEquals("Gate 1", result.allOptions[0].gate.name)
        assertEquals("Gate 3", result.allOptions[1].gate.name)
        assertEquals("Gate 2", result.allOptions[2].gate.name)
        assertNull(result.allOptions[2].route)
    }

    @Test
    fun testAllGatesUnreachableReturnsNull() {
        val finder = NearestGateFinder { _, _ -> null }
        val result = finder.findNearestGate(listOf(gate1, gate2), "dest_loc")
        assertNull(result)
    }
}
