package com.example.unifind.routing.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GraphEditorStateTest {

    private lateinit var state: GraphEditorState

    @Before
    fun setup() {
        state = GraphEditorState()
    }

    @Test
    fun testAddNodeAndIds() {
        val id1 = state.addNode(0.1f, 0.2f)
        val id2 = state.addNode(0.3f, 0.4f)
        assertEquals("n1", id1)
        assertEquals("n2", id2)
        assertEquals(2, state.nodes.size)
    }

    @Test
    fun testConnectToggle() {
        val id1 = state.addNode(0.1f, 0.2f)
        val id2 = state.addNode(0.3f, 0.4f)

        state.connect(id1, id2)
        assertEquals(1, state.edges.size)

        // Connect again should remove
        state.connect(id1, id2)
        assertEquals(0, state.edges.size)

        // Connect reverse should also remove if added
        state.connect(id1, id2)
        state.connect(id2, id1)
        assertEquals(0, state.edges.size)
    }

    @Test
    fun testDeleteNodeRemovesEdges() {
        val id1 = state.addNode(0.1f, 0.2f)
        val id2 = state.addNode(0.3f, 0.4f)
        state.connect(id1, id2)

        state.deleteNode(id1)
        assertEquals(1, state.nodes.size)
        assertEquals(0, state.edges.size)
    }

    @Test
    fun testUndo() {
        val id1 = state.addNode(0.1f, 0.2f)
        assertEquals(1, state.nodes.size)

        state.undo()
        assertEquals(0, state.nodes.size)
    }

    @Test
    fun testToJsonFormat() {
        val id1 = state.addNode(0.1f, 0.2f)
        state.setLocationId(id1, "loc1")
        val json = state.toJson(100, 100, 1.0)

        assertTrue(json.contains("\"imageWidth\": 100"))
        assertTrue(json.contains("\"imageHeight\": 100"))
        assertTrue(json.contains("\"metersPerPixel\": 1.0"))
        assertTrue(json.contains("\"locationId\": \"loc1\""))
    }

    @Test
    fun testValidate() {
        val id1 = state.addNode(0.1f, 0.2f)
        val id2 = state.addNode(0.3f, 0.4f)
        state.setLocationId(id1, "loc1")

        val report = state.validate(listOf("loc1", "loc2"))

        assertEquals(2, report.nodeCount)
        assertEquals(0, report.edgeCount)
        assertEquals(2, report.connectedGroupsCount)
        assertTrue(report.nodesWithNoEdges.contains(id1))
        assertTrue(report.nodesWithNoEdges.contains(id2))
        assertTrue(report.missingLocationIds.contains("loc2"))
    }
}
