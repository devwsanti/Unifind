package com.example.unifind.routing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AlternativeRouteFinderTest {

    @Test
    fun testGraphWithTwoPathsReturnsAlternative() {
        // Path 1: A -> B -> D (100m), Path 2: A -> C -> D (100m)
        val nodes = listOf(
            WaypointNode("A", 0f, 0f),
            WaypointNode("B", 0f, 0.5f),
            WaypointNode("C", 0.5f, 0f),
            WaypointNode("D", 0.5f, 0.5f)
        )
        val edges = listOf(
            Pair("A", "B"), Pair("B", "D"),
            Pair("A", "C"), Pair("C", "D")
        )
        val graph = CampusGraph(nodes, edges, 100, 100, 1.0)
        val altFinder = AlternativeRouteFinder(graph)

        val altRoute = altFinder.findAlternative("A", "D")
        assertNotNull(altRoute)
        assertEquals(3, altRoute!!.nodes.size)
        assertEquals(100.0, altRoute.distanceMeters, 0.01)
    }

    @Test
    fun testGraphWithOnlyOnePathReturnsNull() {
        // A -> B -> D (only 1 path)
        val nodes = listOf(
            WaypointNode("A", 0f, 0f),
            WaypointNode("B", 0f, 0.5f),
            WaypointNode("D", 0.5f, 0.5f)
        )
        val edges = listOf(
            Pair("A", "B"), Pair("B", "D")
        )
        val graph = CampusGraph(nodes, edges, 100, 100, 1.0)
        val altFinder = AlternativeRouteFinder(graph)

        val altRoute = altFinder.findAlternative("A", "D")
        assertNull(altRoute)
    }

    @Test
    fun testAlternativeRejectedIfLongerThan1Point6x() {
        // Path 1: A -> B -> D (dist 50 + 50 = 100m)
        // Path 2: A -> E -> F -> D (dist 90 + 90 + ~56.5 = 236.5m > 160m = 1.6 * 100m)
        val nodes = listOf(
            WaypointNode("A", 0f, 0f),
            WaypointNode("B", 0f, 0.5f),
            WaypointNode("D", 0.5f, 0.5f),
            WaypointNode("E", 0.9f, 0f),
            WaypointNode("F", 0.9f, 0.9f)
        )
        val edges = listOf(
            Pair("A", "B"), Pair("B", "D"),
            Pair("A", "E"), Pair("E", "F"), Pair("F", "D")
        )
        val graph = CampusGraph(nodes, edges, 100, 100, 1.0)
        val altFinder = AlternativeRouteFinder(graph)

        val altRoute = altFinder.findAlternative("A", "D")
        assertNull(altRoute)
    }
}
