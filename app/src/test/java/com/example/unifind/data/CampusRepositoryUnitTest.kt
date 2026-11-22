package com.example.unifind.data

import com.example.unifind.data.dto.GraphDto
import com.example.unifind.model.Gate
import com.example.unifind.routing.CampusGraph
import com.example.unifind.routing.DijkstraRouteFinder
import com.example.unifind.routing.RoutePlanner
import com.example.unifind.routing.WaypointNode
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * OOP Concept: Testing / Unit Testing
 * Validates that every location in CampusRepository resolves to itself by its own id
 * using the findById function used by Map, and that no two locations resolve to the same object.
 */
class CampusRepositoryUnitTest {

    @Test
    fun testEveryLocationResolvesToItselfById() {
        val jsonFile = File("src/main/assets/campus.json")
        val repository = CampusRepository({ jsonFile.readText() })

        val allLocations = repository.getAllLocations()
        assertNotNull(allLocations)
        assertTrue(allLocations.isNotEmpty())

        val resolvedSet = mutableSetOf<Any>()

        for (location in allLocations) {
            val resolved = repository.findById(location.id)
            assertNotNull("Location with id '${location.id}' should be found", resolved)
            assertSame("Location '${location.id}' should resolve to itself", location, resolved)
            resolvedSet.add(resolved!!)
        }

        assertEquals(
            "Every location must resolve to a unique object",
            allLocations.size,
            resolvedSet.size
        )
    }

    @Test
    fun testGetCollegesAndBuildingsForCollege() {
        val jsonFile = File("src/main/assets/campus.json")
        val repository = CampusRepository({ jsonFile.readText() })

        val colleges = repository.getColleges()
        assertNotNull(colleges)
        assertTrue(colleges.isNotEmpty())
        assertTrue("Colleges must contain Laboratory School", colleges.contains("Laboratory School"))

        val cicsBuildings = repository.getBuildingsForCollege("College of Information and Computing Sciences")
        assertNotNull(cicsBuildings)
        assertTrue("CICS must have associated buildings", cicsBuildings.isNotEmpty())
    }

    @Test
    fun testGateResolutionAndCoordinates() {
        val campusFile = File("src/main/assets/campus.json")
        val graphFile = File("src/main/assets/graph.json")

        val repository = CampusRepository({ campusFile.readText() })
        val json = Json { ignoreUnknownKeys = true }
        val dto = json.decodeFromString<GraphDto>(graphFile.readText())
        val nodes = dto.nodes.map { WaypointNode(it.id, it.mapX, it.mapY, it.locationId) }
        val edges = dto.edges.map { Pair(it[0], it[1]) }
        val graph = CampusGraph(nodes, edges, dto.imageWidth, dto.imageHeight, dto.metersPerPixel)

        val gates = repository.getGates()
        assertEquals(4, gates.size)

        val gate2 = gates.find { it.id == "gate_2" }
        assertNotNull("Gate 2 must exist", gate2)
        assertEquals("Gate 2", gate2?.name)
        assertEquals(0.5647f, gate2!!.mapX, 0.001f)
        assertEquals(0.8497f, gate2.mapY, 0.001f)

        val gate3 = gates.find { it.id == "gate_3" }
        assertNotNull("Gate 3 must exist", gate3)
        assertEquals("Gate 3", gate3?.name)
        assertEquals(0.4494f, gate3!!.mapX, 0.001f)
        assertEquals(0.54f, gate3.mapY, 0.001f)

        // Verify every gate resolves to an entry node with matching locationId
        for (gate in gates) {
            val entryNodes = graph.entryNodes(gate.id)
            assertTrue("Gate '${gate.id}' must resolve to an entry node in graph.json", entryNodes.isNotEmpty())
            assertEquals(gate.id, entryNodes.first().locationId)
        }
    }

    @Test
    fun testGate1ToLibraryRouteUnchanged() {
        val graphFile = File("src/main/assets/graph.json")
        val json = Json { ignoreUnknownKeys = true }
        val dto = json.decodeFromString<GraphDto>(graphFile.readText())
        val nodes = dto.nodes.map { WaypointNode(it.id, it.mapX, it.mapY, it.locationId) }
        val edges = dto.edges.map { Pair(it[0], it[1]) }
        val graph = CampusGraph(nodes, edges, dto.imageWidth, dto.imageHeight, dto.metersPerPixel)

        val planner = RoutePlanner(graph, DijkstraRouteFinder(graph))
        val route = planner.fromLocation("gate_1", "library")

        assertNotNull("Route from Gate 1 to Library must exist", route)
        assertEquals("gate_1", route!!.nodes.first().locationId)
        assertEquals("library", route.nodes.last().locationId)
        assertTrue("Distance should be greater than 0", route.distanceMeters > 0)
    }

    @Test
    fun testTotalRoomCount() {
        val campusFile = File("src/main/assets/campus.json")
        val repository = CampusRepository({ campusFile.readText() })

        val totalRooms = repository.getBuildings().sumOf { building ->
            building.floors.sumOf { floor -> floor.rooms.size }
        }

        // 164 original rooms + 2 extra rooms from splitting "Male & Female CR" and "Male and Female CR"
        assertEquals(166, totalRooms)
    }
}
