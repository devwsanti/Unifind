package com.example.unifind.routing

/**
 * OOP Concept: Composition / Delegation
 * Uses composition to wrap a primary RouteFinder and CampusGraph to search for an alternative route
 * by temporarily penalizing the edges used by the main route.
 */
class AlternativeRouteFinder(
    private val graph: CampusGraph,
    private val finderFactory: (CampusGraph) -> RouteFinder = { DijkstraRouteFinder(it) }
) {
    fun findAlternative(
        fromNodeId: String,
        toNodeId: String
    ): RouteResult? {
        val primaryFinder = finderFactory(graph)
        val mainRoute = primaryFinder.findRoute(fromNodeId, toNodeId) ?: return null
        return findAlternativeForRoute(mainRoute, fromNodeId, toNodeId)
    }

    fun findAlternativeForRoute(
        mainRoute: RouteResult,
        fromNodeId: String,
        toNodeId: String
    ): RouteResult? {
        if (mainRoute.nodes.size < 2) return null

        val mainEdges = mutableSetOf<Pair<String, String>>()
        for (i in 0 until mainRoute.nodes.size - 1) {
            val u = mainRoute.nodes[i].id
            val v = mainRoute.nodes[i + 1].id
            mainEdges.add(if (u < v) Pair(u, v) else Pair(v, u))
        }

        val penalizedGraph = graph.withPenalizedEdges(mainEdges, penaltyFactor = 3.0)
        val altFinder = finderFactory(penalizedGraph)
        val penalizedAltRoute = altFinder.findRoute(fromNodeId, toNodeId) ?: return null

        val altEdges = mutableSetOf<Pair<String, String>>()
        for (i in 0 until penalizedAltRoute.nodes.size - 1) {
            val u = penalizedAltRoute.nodes[i].id
            val v = penalizedAltRoute.nodes[i + 1].id
            altEdges.add(if (u < v) Pair(u, v) else Pair(v, u))
        }

        if (altEdges == mainEdges) {
            return null
        }

        var realDistance = 0.0
        for (i in 0 until penalizedAltRoute.nodes.size - 1) {
            val u = penalizedAltRoute.nodes[i]
            val v = penalizedAltRoute.nodes[i + 1]
            realDistance += graph.distanceMeters(u.mapX, u.mapY, v.mapX, v.mapY)
        }

        if (realDistance > mainRoute.distanceMeters * 1.6) {
            return null
        }

        return RouteResult(
            nodes = penalizedAltRoute.nodes,
            distanceMeters = realDistance,
            entranceNode = penalizedAltRoute.entranceNode
        )
    }
}
