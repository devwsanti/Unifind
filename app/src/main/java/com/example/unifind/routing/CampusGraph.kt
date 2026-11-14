package com.example.unifind.routing

import kotlin.math.hypot

/**
 * OOP Concept: Encapsulation / Graph Data Structure
 * Encapsulates the network of waypoints and edges, and provides methods to query them.
 */
class CampusGraph(
    val nodes: List<WaypointNode>,
    val edges: List<Pair<String, String>>,
    val imageWidth: Int,
    val imageHeight: Int,
    val metersPerPixel: Double
) {
    private val nodeMap: Map<String, WaypointNode>
    private val adjacencyList: Map<String, List<Pair<WaypointNode, Double>>>

    init {
        nodeMap = nodes.associateBy { it.id }

        val adj = mutableMapOf<String, MutableList<Pair<WaypointNode, Double>>>()
        for (node in nodes) {
            adj[node.id] = mutableListOf()
        }

        for (edge in edges) {
            val u = nodeMap[edge.first] ?: throw IllegalArgumentException("Edge references unknown node: ${edge.first}")
            val v = nodeMap[edge.second] ?: throw IllegalArgumentException("Edge references unknown node: ${edge.second}")
            val dist = distanceMeters(u.mapX, u.mapY, v.mapX, v.mapY)
            adj[u.id]?.add(Pair(v, dist))
            adj[v.id]?.add(Pair(u, dist))
        }
        adjacencyList = adj
    }

    fun neighbors(nodeId: String): List<Pair<WaypointNode, Double>> {
        return adjacencyList[nodeId] ?: emptyList()
    }

    fun entryNodes(locationId: String): List<WaypointNode> {
        return nodes.filter { it.locationId == locationId }
    }

    fun nearestNode(mapX: Float, mapY: Float): WaypointNode {
        if (nodes.isEmpty()) throw IllegalArgumentException("Graph is empty")
        return nodes.minByOrNull { distanceMeters(mapX, mapY, it.mapX, it.mapY) }!!
    }

    fun distanceMeters(mapX1: Float, mapY1: Float, mapX2: Float, mapY2: Float): Double {
        val dx = (mapX1 - mapX2) * imageWidth
        val dy = (mapY1 - mapY2) * imageHeight
        return hypot(dx.toDouble(), dy.toDouble()) * metersPerPixel
    }
}
