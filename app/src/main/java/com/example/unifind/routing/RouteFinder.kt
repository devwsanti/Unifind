package com.example.unifind.routing

/**
 * OOP Concept: Interface / Abstraction
 * Defines the contract for an algorithm that searches for routes in the graph.
 */
interface RouteFinder {
    fun findRoute(
        fromNodeId: String,
        toNodeId: String,
        bannedEdges: Set<Pair<String, String>> = emptySet()
    ): RouteResult?
}
