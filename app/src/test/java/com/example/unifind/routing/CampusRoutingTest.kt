package com.example.unifind.routing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class CampusRoutingTest {

    private val testNodes = listOf(
        WaypointNode("A", 0.0f, 0.0f, "loc_a"),
        WaypointNode("B", 0.0f, 0.5f),
        WaypointNode("C", 0.5f, 0.0f),
        WaypointNode("D", 0.5f, 0.5f, "loc_b")
    )

    // Distances (width = 100, height = 100, mpp = 1.0)
    // A to B: 50m
    // A to C: 50m
    // B to D: 50m
    // C to D: 50m

    @Test
    fun testShortestPathPickedOverDetour() {
        val extraNode = WaypointNode("E", 1.0f, 1.0f)
        val allNodes = testNodes + extraNode

        // A -> C -> D is 100m. A -> E -> D is much longer (approx 141 + 70 = 211)
        val edges = listOf(
            Pair("A", "C"), Pair("C", "D"),
            Pair("A", "E"), Pair("E", "D")
        )
        val graph = CampusGraph(allNodes, edges, 100, 100, 1.0)
        val planner = RoutePlanner(graph, DijkstraRouteFinder(graph))

        val route = planner.fromLocation("loc_a", "loc_b")
        assertNotNull(route)
        assertEquals(3, route!!.nodes.size)
        assertEquals("A", route.nodes[0].id)
        assertEquals("C", route.nodes[1].id)
        assertEquals("D", route.nodes[2].id)
        assertEquals(100.0, route.distanceMeters, 0.01)
    }

    @Test
    fun testDistanceInMetersCalculation() {
        val edges = listOf(Pair("A", "D"))
        val graph = CampusGraph(testNodes, edges, 200, 100, 0.5) // (0,0) to (0.5, 0.5) -> dx=100, dy=50
        val expectedDist = hypot(100.0, 50.0) * 0.5
        assertEquals(expectedDist, graph.distanceMeters(0f, 0f, 0.5f, 0.5f), 0.01)
    }

    @Test
    fun testUnreachableDestination() {
        val edges = listOf(Pair("A", "B"), Pair("C", "D")) // Disconnected
        val graph = CampusGraph(testNodes, edges, 100, 100, 1.0)
        val planner = RoutePlanner(graph, DijkstraRouteFinder(graph))

        val route = planner.fromLocation("loc_a", "loc_b")
        assertNull(route)
    }

    @Test
    fun testDestinationWithTwoEntrancesPicksNearer() {
        // D is at (0.5, 0.5), E is at (0.0, 0.9) - both "loc_b"
        val nodeE = WaypointNode("E", 0.0f, 0.9f, "loc_b") // dist A->E is 90
        val nodes = testNodes + nodeE
        val edges = listOf(Pair("A", "C"), Pair("C", "D"), Pair("A", "E"))
        val graph = CampusGraph(nodes, edges, 100, 100, 1.0)
        val planner = RoutePlanner(graph, DijkstraRouteFinder(graph))

        val route = planner.fromLocation("loc_a", "loc_b")
        assertNotNull(route)
        assertEquals("E", route!!.entranceNode.id) // A->E is 90m. A->C->D is 100m.
        assertEquals(90.0, route.distanceMeters, 0.01)
    }

    @Test
    fun testFromPointAddsSnappingDistance() {
        val edges = listOf(Pair("A", "B"))
        val graph = CampusGraph(testNodes, edges, 100, 100, 1.0)
        val planner = RoutePlanner(graph, DijkstraRouteFinder(graph))

        // Target B is at (0.0, 0.5). "loc_b" normally is D, but let's change B to "loc_b" for this test.
        val targetNodes = listOf(
            WaypointNode("A", 0.0f, 0.0f),
            WaypointNode("B", 0.0f, 0.5f, "target_loc")
        )
        val graph2 = CampusGraph(targetNodes, listOf(Pair("A", "B")), 100, 100, 1.0)
        val planner2 = RoutePlanner(graph2, DijkstraRouteFinder(graph2))

        // Start point is at 0.0, -0.1. Snap to A is 10m. Path A->B is 50m. Total = 60m.
        val route = planner2.fromPoint(0.0f, -0.1f, "target_loc")
        assertNotNull(route)
        assertEquals(60.0, route!!.distanceMeters, 0.01)
    }

    @Test
    fun testAlternativeRoute() {
        val edges = listOf(Pair("A", "B"), Pair("B", "D"), Pair("A", "C"), Pair("C", "D"))
        val graph = CampusGraph(testNodes, edges, 100, 100, 1.0)
        val planner = RoutePlanner(graph, DijkstraRouteFinder(graph))

        val best = planner.fromLocation("loc_a", "loc_b")
        assertNotNull(best)
        // Assume best picked A->B->D (dist 100).
        val alt = planner.alternative(best!!, "A", "D")
        assertNotNull(alt)
        // Alternative should be A->C->D
        assertTrue(alt!!.nodes != best.nodes)
        assertEquals(100.0, alt.distanceMeters, 0.01) // Not shorter
    }

    @Test
    fun testWalkingMinutesRounding() {
        val route1 = RouteResult(emptyList(), 1.0, WaypointNode("n", 0f, 0f))
        assertEquals(1, route1.walkingMinutes)

        val route2 = RouteResult(emptyList(), 85.0, WaypointNode("n", 0f, 0f))
        assertEquals(2, route2.walkingMinutes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEdgeWithUnknownNodeThrows() {
        val edges = listOf(Pair("A", "Z"))
        CampusGraph(testNodes, edges, 100, 100, 1.0)
    }
}
