package com.example.unifind.ui.directions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.unifind.UniFindApplication
import com.example.unifind.data.GraphRepository
import com.example.unifind.model.Location
import com.example.unifind.routing.DijkstraRouteFinder
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
    private val routePlanner = RoutePlanner(graphRepo.graph, DijkstraRouteFinder(graphRepo.graph))

    val allLocations = repository.getAllLocations().filter { it.mapX != 0f || it.mapY != 0f }

    private val _fromLocation = MutableStateFlow<Location?>(null)
    val fromLocation: StateFlow<Location?> = _fromLocation.asStateFlow()

    private val _toLocation = MutableStateFlow<Location?>(null)
    val toLocation: StateFlow<Location?> = _toLocation.asStateFlow()

    private val _routeResult = MutableStateFlow<RouteResult?>(null)
    val routeResult: StateFlow<RouteResult?> = _routeResult.asStateFlow()

    fun setFromLocation(location: Location?) {
        _fromLocation.update { location }
        calculateRoute()
    }

    fun setToLocation(location: Location?) {
        _toLocation.update { location }
        calculateRoute()
    }

    fun setToLocationById(id: String?) {
        if (id != null) {
            setToLocation(repository.findById(id))
        }
    }

    fun swapLocations() {
        val temp = _fromLocation.value
        _fromLocation.update { _toLocation.value }
        _toLocation.update { temp }
        calculateRoute()
    }

    private fun calculateRoute() {
        val from = _fromLocation.value
        val to = _toLocation.value
        if (from != null && to != null) {
            _routeResult.update { routePlanner.fromLocation(from.id, to.id) }
        } else {
            _routeResult.update { null }
        }
    }
}