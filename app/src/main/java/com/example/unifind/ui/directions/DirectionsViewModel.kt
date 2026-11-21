package com.example.unifind.ui.directions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.unifind.UniFindApplication
import com.example.unifind.data.GraphRepository
import com.example.unifind.model.Gate
import com.example.unifind.model.Location
import com.example.unifind.model.MyLocation
import com.example.unifind.routing.AlternativeRouteFinder
import com.example.unifind.routing.DijkstraRouteFinder
import com.example.unifind.routing.NearestGateFinder
import com.example.unifind.routing.NearestGateResult
import com.example.unifind.routing.NearestNodeFinder
import com.example.unifind.routing.RoutePlanner
import com.example.unifind.routing.RouteResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * OOP Concept: Encapsulation / Observer
 * ViewModel managing the Directions screen state and route calculations.
 */
class DirectionsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as UniFindApplication
    private val repository = app.repository
    private val graphRepo = GraphRepository(application)
    private val routeFinder = DijkstraRouteFinder(graphRepo.graph)
    private val routePlanner = RoutePlanner(graphRepo.graph, routeFinder)
    private val altRouteFinder = AlternativeRouteFinder(graphRepo.graph)
    private val nearestGateFinder = NearestGateFinder { fromGateId, toLocationId ->
        routePlanner.fromLocation(fromGateId, toLocationId)
    }
    private val nearestNodeFinder = NearestNodeFinder(graphRepo.graph.imageWidth, graphRepo.graph.imageHeight)

    val searchEngine = app.searchEngine
    val routePlannerInstance = routePlanner

    val allLocations = repository.getAllLocations().filter { it.mapX != 0f || it.mapY != 0f }
    val gates: List<Gate> = repository.getGates()

    private val _fromLocation = MutableStateFlow<Location?>(null)
    val fromLocation: StateFlow<Location?> = _fromLocation.asStateFlow()

    private val _toLocation = MutableStateFlow<Location?>(null)
    val toLocation: StateFlow<Location?> = _toLocation.asStateFlow()

    private val _targetRoom = MutableStateFlow<com.example.unifind.model.Room?>(null)
    val targetRoom: StateFlow<com.example.unifind.model.Room?> = _targetRoom.asStateFlow()

    private val _isNearestGateAuto = MutableStateFlow(false)
    val isNearestGateAuto: StateFlow<Boolean> = _isNearestGateAuto.asStateFlow()

    private val _isFromMyLocation = MutableStateFlow(false)
    val isFromMyLocation: StateFlow<Boolean> = _isFromMyLocation.asStateFlow()

    private val _currentMyLocation = MutableStateFlow<MyLocation?>(null)
    val currentMyLocation: StateFlow<MyLocation?> = _currentMyLocation.asStateFlow()

    private val _routeResult = MutableStateFlow<RouteResult?>(null)
    val routeResult: StateFlow<RouteResult?> = _routeResult.asStateFlow()

    private val _altRouteResult = MutableStateFlow<RouteResult?>(null)
    val altRouteResult: StateFlow<RouteResult?> = _altRouteResult.asStateFlow()

    private val _nearestGateResult = MutableStateFlow<NearestGateResult?>(null)
    val nearestGateResult: StateFlow<NearestGateResult?> = _nearestGateResult.asStateFlow()

    private val _isFastestSelected = MutableStateFlow(true)
    val isFastestSelected: StateFlow<Boolean> = _isFastestSelected.asStateFlow()

    fun setFromLocation(location: Location?) {
        _isNearestGateAuto.update { false }
        _isFromMyLocation.update { false }
        _fromLocation.update { location }
        calculateRoute()
    }

    fun setFromNearestGateAuto() {
        _isNearestGateAuto.update { true }
        _isFromMyLocation.update { false }
        _fromLocation.update { null }
        calculateRoute()
    }

    fun setFromMyLocation(myLocation: MyLocation?) {
        _isNearestGateAuto.update { false }
        _isFromMyLocation.update { true }
        _fromLocation.update { null }
        _currentMyLocation.update { myLocation }
        calculateRoute()
    }

    fun updateMyLocation(myLocation: MyLocation?) {
        _currentMyLocation.update { myLocation }
        if (_isFromMyLocation.value) {
            calculateRoute()
        }
    }

    fun setToLocation(location: Location?, room: com.example.unifind.model.Room? = null) {
        _toLocation.update { location }
        _targetRoom.update { room }
        calculateRoute()
    }

    fun setToLocationById(id: String?, roomId: String? = null) {
        if (id != null) {
            val loc = repository.findById(id)
            val room = if (roomId != null && loc is com.example.unifind.model.Building) {
                loc.floors.flatMap { it.rooms }.find { it.id == roomId }
            } else null
            setToLocation(loc, room)
        }
    }

    fun selectFastestRoute() {
        _isFastestSelected.update { true }
    }

    fun selectAltRoute() {
        _isFastestSelected.update { false }
    }

    fun swapLocations() {
        _targetRoom.update { null }
        if (_isNearestGateAuto.value || _isFromMyLocation.value) {
            _isNearestGateAuto.update { false }
            _isFromMyLocation.update { false }
            _fromLocation.update { _toLocation.value }
            _toLocation.update { null }
        } else {
            val temp = _fromLocation.value
            _fromLocation.update { _toLocation.value }
            _toLocation.update { temp }
        }
        calculateRoute()
    }

    private fun calculateRoute() {
        _isFastestSelected.update { true }
        val to = _toLocation.value

        if (to == null) {
            _routeResult.update { null }
            _altRouteResult.update { null }
            _nearestGateResult.update { null }
            return
        }

        if (_isFromMyLocation.value) {
            _nearestGateResult.update { null }
            val myLoc = _currentMyLocation.value
            if (myLoc != null) {
                val nearestNode = nearestNodeFinder.findNearestNode(graphRepo.graph.nodes, myLoc.mapX, myLoc.mapY)
                if (nearestNode != null) {
                    val endNodes = graphRepo.graph.entryNodes(to.id)
                    var bestRoute: RouteResult? = null
                    for (e in endNodes) {
                        val route = routeFinder.findRoute(nearestNode.id, e.id)
                        if (route != null) {
                            val snapDist = graphRepo.graph.distanceMeters(myLoc.mapX, myLoc.mapY, nearestNode.mapX, nearestNode.mapY)
                            val totalDist = route.distanceMeters + snapDist
                            val adjusted = RouteResult(route.nodes, totalDist, route.entranceNode)
                            if (bestRoute == null || adjusted.distanceMeters < bestRoute.distanceMeters) {
                                bestRoute = adjusted
                            }
                        }
                    }
                    _routeResult.update { bestRoute }
                    if (bestRoute != null) {
                        val startNodeId = bestRoute.nodes.firstOrNull()?.id
                        val endNodeId = bestRoute.nodes.lastOrNull()?.id
                        if (startNodeId != null && endNodeId != null) {
                            _altRouteResult.update {
                                altRouteFinder.findAlternativeForRoute(bestRoute, startNodeId, endNodeId)
                            }
                        } else {
                            _altRouteResult.update { null }
                        }
                    } else {
                        _altRouteResult.update { null }
                    }
                } else {
                    _routeResult.update { null }
                    _altRouteResult.update { null }
                }
            } else {
                _routeResult.update { null }
                _altRouteResult.update { null }
            }
        } else if (_isNearestGateAuto.value) {
            val gateRes = nearestGateFinder.findNearestGate(gates, to.id)
            _nearestGateResult.update { gateRes }
            if (gateRes != null) {
                _fromLocation.update { gateRes.bestGate }
                _routeResult.update { gateRes.bestRoute }
                val startNodeId = gateRes.bestRoute.nodes.firstOrNull()?.id
                val endNodeId = gateRes.bestRoute.nodes.lastOrNull()?.id
                if (startNodeId != null && endNodeId != null) {
                    _altRouteResult.update {
                        altRouteFinder.findAlternativeForRoute(gateRes.bestRoute, startNodeId, endNodeId)
                    }
                } else {
                    _altRouteResult.update { null }
                }
            } else {
                _routeResult.update { null }
                _altRouteResult.update { null }
            }
        } else {
            _nearestGateResult.update { null }
            val from = _fromLocation.value
            if (from != null) {
                val main = routePlanner.fromLocation(from.id, to.id)
                _routeResult.update { main }
                if (main != null) {
                    val startNodeId = main.nodes.firstOrNull()?.id
                    val endNodeId = main.nodes.lastOrNull()?.id
                    if (startNodeId != null && endNodeId != null) {
                        _altRouteResult.update {
                            altRouteFinder.findAlternativeForRoute(main, startNodeId, endNodeId)
                        }
                    } else {
                        _altRouteResult.update { null }
                    }
                } else {
                    _altRouteResult.update { null }
                }
            } else {
                _routeResult.update { null }
                _altRouteResult.update { null }
            }
        }
    }
}
