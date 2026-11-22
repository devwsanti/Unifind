package com.example.unifind.routing

import com.example.unifind.model.Gate

/**
 * OOP Concept: Single Responsibility Principle / Strategy
 * Pure class that evaluates routes from available campus gates to a destination
 * to determine the nearest gate and rank all gate distances.
 */
data class GateRouteOption(
    val gate: Gate,
    val route: RouteResult?
)

data class NearestGateResult(
    val bestGate: Gate,
    val bestRoute: RouteResult,
    val allOptions: List<GateRouteOption>
)

class NearestGateFinder(
    private val routeCalculator: (fromGateId: String, toLocationId: String) -> RouteResult?
) {
    fun findNearestGate(
        gates: List<Gate>,
        toLocationId: String
    ): NearestGateResult? {
        if (gates.isEmpty()) return null

        val options = gates.map { gate ->
            val route = routeCalculator(gate.id, toLocationId)
            GateRouteOption(gate, route)
        }

        val validOptions = options.filter { it.route != null }
            .sortedBy { it.route!!.distanceMeters }

        if (validOptions.isEmpty()) return null

        val best = validOptions.first()
        val sortedAll = options.sortedWith(
            compareBy<GateRouteOption> { it.route == null }
                .thenBy { it.route?.distanceMeters ?: Double.MAX_VALUE }
        )

        return NearestGateResult(
            bestGate = best.gate,
            bestRoute = best.route!!,
            allOptions = sortedAll
        )
    }
}
