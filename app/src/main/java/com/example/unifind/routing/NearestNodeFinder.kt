package com.example.unifind.routing

import kotlin.math.hypot

/**
 * OOP Concept: Single Responsibility Principle
 * Pure utility class responsible for finding the nearest WaypointNode to given map coordinates.
 */
class NearestNodeFinder(
    private val imageWidth: Int = 941,
    private val imageHeight: Int = 1171
) {
    fun findNearestNode(nodes: List<WaypointNode>, mapX: Float, mapY: Float): WaypointNode? {
        if (nodes.isEmpty()) return null
        return nodes.minByOrNull { node ->
            val dx = (mapX - node.mapX) * imageWidth
            val dy = (mapY - node.mapY) * imageHeight
            hypot(dx.toDouble(), dy.toDouble())
        }
    }
}
