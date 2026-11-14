package com.example.unifind.routing

import kotlin.math.ceil

/**
 * OOP Concept: Facade / Composition
 * High-level router bridging coordinates and locations to the underlying finding algorithm.
 */
class RoutePlanner(private val graph: CampusGraph, private val finder: RouteFinder) {

    fun fromLocation(fromLocationId: String, toLocationId: String): RouteResult? {
        val startNodes = graph.entryNodes(fromLocationId)
        val endNodes = graph.entryNodes(toLocationId)

        var bestRoute: RouteResult? = null
        for (s in startNodes) {
            for (e in endNodes) {
                val route = finder.findRoute(s.id, e.id)
                if (route != null) {
                    if (bestRoute == null || route.distanceMeters < bestRoute.distanceMeters) {
                        bestRoute = route
                    }
                }
            }
        }
        return bestRoute
    }

    fun fromPoint(mapX: Float, mapY: Float, toLocationId: String): RouteResult? {
        if (graph.nodes.isEmpty()) return null
        val startNode = graph.nearestNode(mapX, mapY)
        val endNodes = graph.entryNodes(toLocationId)

        var bestRoute: RouteResult? = null
        for (e in endNodes) {
            val route = finder.findRoute(startNode.id, e.id)
            if (route != null) {
                val snapDist = graph.distanceMeters(mapX, mapY, startNode.mapX, startNode.mapY)
                val totalDist = route.distanceMeters + snapDist
                val adjustedRoute = RouteResult(route.nodes, totalDist, route.entranceNode)
                if (bestRoute == null || adjustedRoute.distanceMeters < bestRoute.distanceMeters) {
                    bestRoute = adjustedRoute
                }
            }
        }
        return bestRoute
    }

    fun fromNearestGate(toLocationId: String, gateLocationIds: List<String>): Pair<String, RouteResult>? {
        var bestPair: Pair<String, RouteResult>? = null
        for (gateId in gateLocationIds) {
            val route = fromLocation(gateId, toLocationId)
            if (route != null) {
                if (bestPair == null || route.distanceMeters < bestPair.second.distanceMeters) {
                    bestPair = Pair(gateId, route)
                }
            }
        }
        return bestPair
    }

    fun alternative(best: RouteResult, fromNodeId: String, toNodeId: String): RouteResult? {
        val nodes = best.nodes
        if (nodes.size < 2) return null

        var bestAlt: RouteResult? = null

        for (i in 0 until nodes.size - 1) {
            val u = nodes[i].id
            val v = nodes[i+1].id
            val banned = setOf(Pair(u, v))

            val altRoute = finder.findRoute(fromNodeId, toNodeId, bannedEdges = banned)
            if (altRoute != null) {
                // Must differ
                if (altRoute.nodes != best.nodes) {
                    if (bestAlt == null || altRoute.distanceMeters < bestAlt.distanceMeters) {
                        bestAlt = altRoute
                    }
                }
            }
        }
        return bestAlt
    }
}
