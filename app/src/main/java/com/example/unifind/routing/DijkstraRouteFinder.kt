package com.example.unifind.routing

import java.util.PriorityQueue
import kotlin.math.ceil

/**
 * OOP Concept: Strategy Implementation
 * Solves the shortest path using Dijkstra's algorithm.
 */
class DijkstraRouteFinder(private val graph: CampusGraph) : RouteFinder {

    private data class NodeState(val node: WaypointNode, val dist: Double) : Comparable<NodeState> {
        override fun compareTo(other: NodeState): Int = dist.compareTo(other.dist)
    }

    override fun findRoute(
        fromNodeId: String,
        toNodeId: String,
        bannedEdges: Set<Pair<String, String>>
    ): RouteResult? {
        val startNode = graph.nodes.find { it.id == fromNodeId } ?: return null
        val endNode = graph.nodes.find { it.id == toNodeId } ?: return null

        val dist = mutableMapOf<String, Double>().withDefault { Double.POSITIVE_INFINITY }
        val prev = mutableMapOf<String, WaypointNode>()
        val pq = PriorityQueue<NodeState>()

        dist[fromNodeId] = 0.0
        pq.add(NodeState(startNode, 0.0))
        val visited = mutableSetOf<String>()

        while (pq.isNotEmpty()) {
            val polled = pq.poll() ?: break
            val u = polled.node
            val d = polled.dist

            if (u.id == toNodeId) break
            if (!visited.add(u.id)) continue

            for ((v, weight) in graph.neighbors(u.id)) {
                if (bannedEdges.contains(Pair(u.id, v.id)) || bannedEdges.contains(Pair(v.id, u.id))) {
                    continue
                }

                val alt = d + weight
                if (alt < dist.getValue(v.id)) {
                    dist[v.id] = alt
                    prev[v.id] = u
                    pq.add(NodeState(v, alt))
                }
            }
        }

        if (dist.getValue(toNodeId) == Double.POSITIVE_INFINITY) {
            return null
        }

        val path = mutableListOf<WaypointNode>()
        var current: WaypointNode? = endNode
        while (current != null) {
            path.add(current)
            current = prev[current.id]
        }
        path.reverse()

        val totalDist = dist.getValue(toNodeId)
        return RouteResult(
            nodes = path,
            distanceMeters = totalDist,
            entranceNode = endNode
        )
    }
}
