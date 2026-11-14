package com.example.unifind.routing.editor

import com.example.unifind.data.dto.GraphDto
import com.example.unifind.data.dto.GraphNodeDto
import com.example.unifind.routing.WaypointNode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * OOP Concept: Memento / State
 * Manages the state and history of the map graph being edited.
 */
class GraphEditorState {

    data class Snapshot(
        val nodes: List<WaypointNode>,
        val edges: List<Pair<String, String>>,
        val nextId: Int
    )

    var nodes = mutableListOf<WaypointNode>()
    var edges = mutableListOf<Pair<String, String>>()
    var nextId = 1
    private val history = ArrayDeque<Snapshot>()

    private val json = Json { prettyPrint = true }

    fun addNode(mapX: Float, mapY: Float): String {
        saveSnapshot()
        val id = "n$nextId"
        nextId++
        nodes.add(WaypointNode(id, mapX, mapY, null))
        return id
    }

    fun connect(a: String, b: String) {
        if (a == b) return
        saveSnapshot()
        val edge1 = Pair(a, b)
        val edge2 = Pair(b, a)
        if (edges.contains(edge1)) {
            edges.remove(edge1)
        } else if (edges.contains(edge2)) {
            edges.remove(edge2)
        } else {
            edges.add(edge1)
        }
    }

    fun moveNode(id: String, mapX: Float, mapY: Float) {
        saveSnapshot()
        val index = nodes.indexOfFirst { it.id == id }
        if (index != -1) {
            nodes[index] = nodes[index].copy(mapX = mapX, mapY = mapY)
        }
    }

    fun deleteNode(id: String) {
        saveSnapshot()
        nodes.removeAll { it.id == id }
        edges.removeAll { it.first == id || it.second == id }
    }

    fun setLocationId(nodeId: String, locationId: String?) {
        saveSnapshot()
        val index = nodes.indexOfFirst { it.id == nodeId }
        if (index != -1) {
            nodes[index] = nodes[index].copy(locationId = locationId)
        }
    }

    fun clearAll() {
        saveSnapshot()
        nodes.clear()
        edges.clear()
        nextId = 1
    }

    private fun saveSnapshot() {
        if (history.size >= 20) {
            history.removeFirst()
        }
        history.addLast(Snapshot(nodes.toList(), edges.toList(), nextId))
    }

    fun undo() {
        if (history.isNotEmpty()) {
            val last = history.removeLast()
            nodes = last.nodes.toMutableList()
            edges = last.edges.toMutableList()
            nextId = last.nextId
        }
    }

    fun toJson(imageWidth: Int = 941, imageHeight: Int = 1171, metersPerPixel: Double = 0.2568): String {
        val dto = GraphDto(
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            metersPerPixel = metersPerPixel,
            nodes = nodes.map { GraphNodeDto(it.id, it.mapX, it.mapY, it.locationId) },
            edges = edges.map { listOf(it.first, it.second) }
        )
        return json.encodeToString(dto)
    }

    fun fromJson(jsonString: String) {
        try {
            val dto = json.decodeFromString<GraphDto>(jsonString)
            nodes = dto.nodes.map { WaypointNode(it.id, it.mapX, it.mapY, it.locationId) }.toMutableList()
            edges = dto.edges.map { Pair(it[0], it[1]) }.toMutableList()
            val maxId = nodes.mapNotNull { it.id.removePrefix("n").toIntOrNull() }.maxOrNull() ?: 0
            nextId = maxId + 1
            history.clear()
        } catch (e: Exception) {
            // Error handling ignored for brevity
        }
    }

    fun validate(allLocationIds: List<String>): ValidationReport {
        val nodeCount = nodes.size
        val edgeCount = edges.size

        val adj = mutableMapOf<String, MutableList<String>>()
        nodes.forEach { adj[it.id] = mutableListOf() }
        edges.forEach {
            adj[it.first]?.add(it.second)
            adj[it.second]?.add(it.first)
        }

        val nodesWithNoEdges = adj.filter { it.value.isEmpty() }.keys.toList()

        val visited = mutableSetOf<String>()
        var connectedGroupsCount = 0
        for (node in nodes) {
            if (!visited.contains(node.id)) {
                connectedGroupsCount++
                val queue = ArrayDeque<String>()
                queue.add(node.id)
                visited.add(node.id)
                while (queue.isNotEmpty()) {
                    val curr = queue.removeFirst()
                    adj[curr]?.forEach { neighbor ->
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor)
                            queue.add(neighbor)
                        }
                    }
                }
            }
        }

        val presentLocationIds = nodes.mapNotNull { it.locationId }.toSet()
        val missingLocationIds = allLocationIds.filter { !presentLocationIds.contains(it) }

        return ValidationReport(
            nodeCount,
            edgeCount,
            connectedGroupsCount,
            nodesWithNoEdges,
            missingLocationIds
        )
    }
}

data class ValidationReport(
    val nodeCount: Int,
    val edgeCount: Int,
    val connectedGroupsCount: Int,
    val nodesWithNoEdges: List<String>,
    val missingLocationIds: List<String>
)
