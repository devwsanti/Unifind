package com.example.unifind.routing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for NearestNodeFinder.
 */
class NearestNodeFinderTest {

    private val finder = NearestNodeFinder(imageWidth = 1000, imageHeight = 1000)

    @Test
    fun findNearestNode_emptyList_returnsNull() {
        val result = finder.findNearestNode(emptyList<WaypointNode>(), 0.5f, 0.5f)
        assertNull(result)
    }

    @Test
    fun findNearestNode_picksNearestNode() {
        val n1 = WaypointNode("n1", 0.1f, 0.1f)
        val n2 = WaypointNode("n2", 0.5f, 0.5f)
        val n3 = WaypointNode("n3", 0.9f, 0.9f)
        val nodes = listOf(n1, n2, n3)

        val result = finder.findNearestNode(nodes, 0.48f, 0.52f)
        assertEquals("n2", result?.id)
    }
}
