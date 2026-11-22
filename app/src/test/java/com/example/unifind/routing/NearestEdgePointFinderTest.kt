package com.example.unifind.routing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for NearestEdgePointFinder, virtual start node routing, and 35m snap threshold.
 */
class NearestEdgePointFinderTest {

    private fun createSimpleGraph(
        nodes: List<WaypointNode>,
        edges: List<Pair<String, String>>,
        metersPerPixel: Double = 1.0
    ): CampusGraph {
        return CampusGraph(
            nodes = nodes,
            edges = edges,
            imageWidth = 1000,
            imageHeight = 1000,
            metersPerPixel = metersPerPixel
        )
    }

    @Test
    fun projection_interiorPoint() {
        val nodeA = WaypointNode("A", 0.1f, 0.5f) // (100, 500)
        val nodeB = WaypointNode("B", 0.9f, 0.5f) // (900, 500)
        val graph = createSimpleGraph(listOf(nodeA, nodeB), listOf(Pair("A", "B")))

        val finder = NearestEdgePointFinder(graph)
        // Dot at (0.5, 0.6) -> (500, 600)
        val result = finder.findNearestEdgePoint(0.5f, 0.6f)

        assertNotNull(result)
        assertEquals(0.5f, result!!.snappedX, 0.001f)
        assertEquals(0.5f, result.snappedY, 0.001f)
        assertEquals(100.0, result.distanceToEdgeMeters, 0.01)
        assertEquals(400.0, result.distanceToNodeAMeters, 0.01)
        assertEquals(400.0, result.distanceToNodeBMeters, 0.01)
        assertEquals(0.5, result.fraction, 0.001)
    }

    @Test
    fun projection_clampedToStartEnd() {
        val nodeA = WaypointNode("A", 0.2f, 0.5f) // (200, 500)
        val nodeB = WaypointNode("B", 0.8f, 0.5f) // (800, 500)
        val graph = createSimpleGraph(listOf(nodeA, nodeB), listOf(Pair("A", "B")))

        val finder = NearestEdgePointFinder(graph)

        // Point before node A: (0.1, 0.5) -> (100, 500)
        val resultA = finder.findNearestEdgePoint(0.1f, 0.5f)
        assertNotNull(resultA)
        assertEquals(0.2f, resultA!!.snappedX, 0.001f)
        assertEquals(0.5f, resultA.snappedY, 0.001f)
        assertEquals(100.0, resultA.distanceToEdgeMeters, 0.01)
        assertEquals(0.0, resultA.distanceToNodeAMeters, 0.01)
        assertEquals(600.0, resultA.distanceToNodeBMeters, 0.01)

        // Point after node B: (0.9, 0.5) -> (900, 500)
        val resultB = finder.findNearestEdgePoint(0.9f, 0.5f)
        assertNotNull(resultB)
        assertEquals(0.8f, resultB!!.snappedX, 0.001f)
        assertEquals(0.5f, resultB.snappedY, 0.001f)
        assertEquals(100.0, resultB.distanceToEdgeMeters, 0.01)
        assertEquals(600.0, resultB.distanceToNodeAMeters, 0.01)
        assertEquals(0.0, resultB.distanceToNodeBMeters, 0.01)
    }

    @Test
    fun projection_exactlyOnSegment() {
        val nodeA = WaypointNode("A", 0.1f, 0.5f)
        val nodeB = WaypointNode("B", 0.9f, 0.5f)
        val graph = createSimpleGraph(listOf(nodeA, nodeB), listOf(Pair("A", "B")))

        val finder = NearestEdgePointFinder(graph)
        val result = finder.findNearestEdgePoint(0.5f, 0.5f)

        assertNotNull(result)
        assertEquals(0.5f, result!!.snappedX, 0.001f)
        assertEquals(0.5f, result.snappedY, 0.001f)
        assertEquals(0.0, result.distanceToEdgeMeters, 0.001)
    }

    @Test
    fun projection_zeroLengthSegment() {
        val nodeA = WaypointNode("A", 0.5f, 0.5f)
        val nodeB = WaypointNode("B", 0.5f, 0.5f)
        val graph = createSimpleGraph(listOf(nodeA, nodeB), listOf(Pair("A", "B")))

        val finder = NearestEdgePointFinder(graph)
        val result = finder.findNearestEdgePoint(0.5f, 0.6f) // (500, 600)

        assertNotNull(result)
        assertEquals(0.5f, result!!.snappedX, 0.001f)
        assertEquals(0.5f, result.snappedY, 0.001f)
        assertEquals(100.0, result.distanceToEdgeMeters, 0.01)
    }

    @Test
    fun virtualNode_routeMiddleOfEdgeToSameEdgeNode() {
        val nodeA = WaypointNode("A", 0.1f, 0.5f) // (100, 500)
        val nodeB = WaypointNode("B", 0.9f, 0.5f, locationId = "LOC_B") // (900, 500)
        val graph = createSimpleGraph(listOf(nodeA, nodeB), listOf(Pair("A", "B")))
        val planner = RoutePlanner(graph)

        // Point directly on segment at (0.5, 0.5) -> 400m to B, gap = 0
        val route = planner.fromPoint(0.5f, 0.5f, "LOC_B")
        assertNotNull(route)
        assertEquals(400.0, route!!.distanceMeters, 0.1)
    }

    @Test
    fun virtualNode_routeToNodeOnAnotherEdge() {
        val nodeA = WaypointNode("A", 0.1f, 0.5f) // (100, 500)
        val nodeB = WaypointNode("B", 0.5f, 0.5f) // (500, 500)
        val nodeC = WaypointNode("C", 0.9f, 0.5f, locationId = "LOC_C") // (900, 500)
        val graph = createSimpleGraph(
            listOf(nodeA, nodeB, nodeC),
            listOf(Pair("A", "B"), Pair("B", "C"))
        )
        val planner = RoutePlanner(graph)

        // Point on A-B at (0.3, 0.5) -> (300, 500). Partial A-B to B = 200m, B-C = 400m. Total = 600m
        val route = planner.fromPoint(0.3f, 0.5f, "LOC_C")
        assertNotNull(route)
        assertEquals(600.0, route!!.distanceMeters, 0.1)
    }

    @Test
    fun threshold_35m() {
        val nodeA = WaypointNode("A", 0.1f, 0.5f)
        val nodeB = WaypointNode("B", 0.9f, 0.5f, locationId = "LOC_B")
        val graph = createSimpleGraph(listOf(nodeA, nodeB), listOf(Pair("A", "B")))
        val planner = RoutePlanner(graph)

        // Point at (0.5, 0.534) -> dy = 34 pixels -> 34 meters
        val route34 = planner.fromPoint(0.5f, 0.534f, "LOC_B")
        assertNotNull(route34)

        // Point at (0.5, 0.536) -> dy = 36 pixels -> 36 meters
        val route36 = planner.fromPoint(0.5f, 0.536f, "LOC_B")
        assertNull(route36)
    }
}
