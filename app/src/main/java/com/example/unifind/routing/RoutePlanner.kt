package com.example.unifind.routing

/**
 * OOP Concept: Facade / Composition
 * High-level router bridging coordinates and locations to the underlying finding algorithm.
 */
class RoutePlanner(
    private val graph: CampusGraph,
    private val finder: RouteFinder = DijkstraRouteFinder(graph),
    private val finderFactory: (CampusGraph) -> RouteFinder = { DijkstraRouteFinder(it) }
) {

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

    fun fromPoint(
        mapX: Float,
        mapY: Float,
        toLocationId: String,
        maxSnapDistanceM: Double = NearestEdgePointFinder.MAX_SNAP_DISTANCE_M
    ): RouteResult? {
        if (graph.nodes.isEmpty()) return null

        val edgeFinder = NearestEdgePointFinder(graph)
        val snapResult = edgeFinder.findNearestEdgePoint(mapX, mapY) ?: return null

        if (snapResult.distanceToEdgeMeters > maxSnapDistanceM) {
            return null
        }

        val gapDist = snapResult.distanceToEdgeMeters
        val (augmentedGraph, startNodeId) = createAugmentedGraph(snapResult)

        val endNodes = augmentedGraph.entryNodes(toLocationId)
        val augmentedFinder = finderFactory(augmentedGraph)

        var bestRoute: RouteResult? = null
        val dotNode = WaypointNode(id = "MY_LOCATION", mapX = mapX, mapY = mapY)

        for (e in endNodes) {
            val route = augmentedFinder.findRoute(startNodeId, e.id)
            if (route != null) {
                val totalDist = route.distanceMeters + gapDist
                val finalNodes = listOf(dotNode) + route.nodes
                val adjustedRoute = RouteResult(
                    nodes = finalNodes,
                    distanceMeters = totalDist,
                    entranceNode = route.entranceNode
                )
                if (bestRoute == null || adjustedRoute.distanceMeters < bestRoute.distanceMeters) {
                    bestRoute = adjustedRoute
                }
            }
        }
        return bestRoute
    }

    fun alternativeFromPoint(
        mapX: Float,
        mapY: Float,
        mainRoute: RouteResult,
        maxSnapDistanceM: Double = NearestEdgePointFinder.MAX_SNAP_DISTANCE_M
    ): RouteResult? {
        if (graph.nodes.isEmpty()) return null
        val edgeFinder = NearestEdgePointFinder(graph)
        val snapResult = edgeFinder.findNearestEdgePoint(mapX, mapY) ?: return null
        if (snapResult.distanceToEdgeMeters > maxSnapDistanceM) return null

        val gapDist = snapResult.distanceToEdgeMeters
        val (augmentedGraph, startNodeId) = createAugmentedGraph(snapResult)

        val graphRouteNodes = if (mainRoute.nodes.firstOrNull()?.id == "MY_LOCATION") {
            mainRoute.nodes.drop(1)
        } else {
            mainRoute.nodes
        }
        if (graphRouteNodes.size < 2) return null

        val graphMainRoute = RouteResult(
            nodes = graphRouteNodes,
            distanceMeters = mainRoute.distanceMeters - gapDist,
            entranceNode = mainRoute.entranceNode
        )

        val altFinder = AlternativeRouteFinder(augmentedGraph, finderFactory)
        val endNodeId = mainRoute.entranceNode.id
        val altRouteOnGraph = altFinder.findAlternativeForRoute(graphMainRoute, startNodeId, endNodeId) ?: return null

        val dotNode = WaypointNode(id = "MY_LOCATION", mapX = mapX, mapY = mapY)
        val finalNodes = listOf(dotNode) + altRouteOnGraph.nodes
        return RouteResult(
            nodes = finalNodes,
            distanceMeters = altRouteOnGraph.distanceMeters + gapDist,
            entranceNode = altRouteOnGraph.entranceNode
        )
    }

    private fun createAugmentedGraph(snapResult: EdgePointResult): Pair<CampusGraph, String> {
        return if (snapResult.distanceToNodeAMeters <= 1.0) {
            Pair(graph, snapResult.nodeA.id)
        } else if (snapResult.distanceToNodeBMeters <= 1.0) {
            Pair(graph, snapResult.nodeB.id)
        } else {
            val virtualNode = WaypointNode(
                id = "VIRTUAL_START",
                mapX = snapResult.snappedX,
                mapY = snapResult.snappedY
            )
            val newNodes = graph.nodes + virtualNode
            val newEdges = graph.edges +
                    Pair(virtualNode.id, snapResult.nodeA.id) +
                    Pair(virtualNode.id, snapResult.nodeB.id)

            val augGraph = CampusGraph(
                nodes = newNodes,
                edges = newEdges,
                imageWidth = graph.imageWidth,
                imageHeight = graph.imageHeight,
                metersPerPixel = graph.metersPerPixel
            )
            Pair(augGraph, virtualNode.id)
        }
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
            val v = nodes[i + 1].id
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
