package com.example.unifind.routing

import kotlin.math.hypot

/**
 * OOP Concept: Data Transfer Object (DTO)
 * Represents the result of snapping a point on the map to the nearest edge segment on the graph.
 */
data class EdgePointResult(
    val nodeA: WaypointNode,
    val nodeB: WaypointNode,
    val snappedX: Float,
    val snappedY: Float,
    val distanceToEdgeMeters: Double,
    val distanceToNodeAMeters: Double,
    val distanceToNodeBMeters: Double,
    val fraction: Double
)

/**
 * OOP Concept: Single Responsibility Principle
 * Pure utility class responsible for finding the closest point on any graph edge segment.
 */
class NearestEdgePointFinder(private val graph: CampusGraph) {

    companion object {
        const val MAX_SNAP_DISTANCE_M = 35.0
    }

    fun findNearestEdgePoint(mapX: Float, mapY: Float): EdgePointResult? {
        if (graph.edges.isEmpty() || graph.nodes.isEmpty()) return null

        val nodeMap = graph.nodes.associateBy { it.id }
        var bestResult: EdgePointResult? = null

        val px = mapX * graph.imageWidth
        val py = mapY * graph.imageHeight

        for (edge in graph.edges) {
            val u = nodeMap[edge.first] ?: continue
            val v = nodeMap[edge.second] ?: continue

            val ax = u.mapX * graph.imageWidth
            val ay = u.mapY * graph.imageHeight
            val bx = v.mapX * graph.imageWidth
            val by = v.mapY * graph.imageHeight

            val dx = bx - ax
            val dy = by - ay
            val segLenSq = dx * dx + dy * dy

            val t = if (segLenSq == 0f) {
                0.0
            } else {
                val apx = px - ax
                val apy = py - ay
                val dot = apx * dx + apy * dy
                (dot / segLenSq).toDouble().coerceIn(0.0, 1.0)
            }

            val sx = ax + (t * dx).toFloat()
            val sy = ay + (t * dy).toFloat()

            val snappedMapX = u.mapX + (t * (v.mapX - u.mapX)).toFloat()
            val snappedMapY = u.mapY + (t * (v.mapY - u.mapY)).toFloat()

            val distToEdge = hypot((px - sx).toDouble(), (py - sy).toDouble()) * graph.metersPerPixel
            val distToNodeA = hypot((sx - ax).toDouble(), (sy - ay).toDouble()) * graph.metersPerPixel
            val distToNodeB = hypot((bx - sx).toDouble(), (by - sy).toDouble()) * graph.metersPerPixel

            val candidate = EdgePointResult(
                nodeA = u,
                nodeB = v,
                snappedX = snappedMapX,
                snappedY = snappedMapY,
                distanceToEdgeMeters = distToEdge,
                distanceToNodeAMeters = distToNodeA,
                distanceToNodeBMeters = distToNodeB,
                fraction = t
            )

            if (bestResult == null || candidate.distanceToEdgeMeters < bestResult.distanceToEdgeMeters) {
                bestResult = candidate
            }
        }

        return bestResult
    }
}
