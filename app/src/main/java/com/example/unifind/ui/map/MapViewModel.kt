package com.example.unifind.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.unifind.UniFindApplication
import com.example.unifind.config.AppConfig
import com.example.unifind.data.GraphRepository
import com.example.unifind.model.Location
import com.example.unifind.routing.DijkstraRouteFinder
import com.example.unifind.routing.RoutePlanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

/**
 * OOP Concept: Sealed Class / State Representation
 * Represents the current mode of the map screen.
 */
sealed class MapMode {
    object View : MapMode()
    object Calibrate : MapMode()
}

/**
 * OOP Concept: Encapsulation / Observer Pattern
 * Manages the state for the map screen, including selected location, mode transitions, and calibration logic.
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as UniFindApplication).repository
    private val graphRepo = GraphRepository(application)
    private val routeFinder = DijkstraRouteFinder(graphRepo.graph)

    val routePlanner = RoutePlanner(graphRepo.graph, routeFinder)
    val graphNodes = graphRepo.graph.nodes

    private val _selectedLocation = MutableStateFlow<Location?>(null)
    val selectedLocation: StateFlow<Location?> = _selectedLocation.asStateFlow()

    private val _selectedRoom = MutableStateFlow<com.example.unifind.model.Room?>(null)
    val selectedRoom: StateFlow<com.example.unifind.model.Room?> = _selectedRoom.asStateFlow()

    private val _mapMode = MutableStateFlow<MapMode>(MapMode.View)
    val mapMode: StateFlow<MapMode> = _mapMode.asStateFlow()

    private val _isCalibrationMode = MutableStateFlow(false)
    val isCalibrationMode: StateFlow<Boolean> = _isCalibrationMode.asStateFlow()

    private val _locationsVersion = MutableStateFlow(0)
    val locationsVersion: StateFlow<Int> = _locationsVersion.asStateFlow()

    private val undoStack = ArrayDeque<LocationPositionChange>()

    val allLocations: List<Location>
        get() = repository.getAllLocations()

    val placedLocations: List<Location>
        get() = allLocations.filter { it.mapX != 0f || it.mapY != 0f }

    fun setMapMode(mode: MapMode) {
        val targetMode = if (!AppConfig.DEVELOPER_MODE && mode is MapMode.Calibrate) MapMode.View else mode
        _mapMode.update { targetMode }
        _isCalibrationMode.update { targetMode is MapMode.Calibrate }
        if (targetMode is MapMode.Calibrate) {
            selectLocation(null, null)
        }
    }

    fun toggleCalibrationMode() {
        val nextMode = if (_mapMode.value is MapMode.Calibrate) MapMode.View else MapMode.Calibrate
        setMapMode(nextMode)
    }

    fun selectLocation(location: Location?, room: com.example.unifind.model.Room? = null) {
        _selectedLocation.update { location }
        _selectedRoom.update { room }
    }

    fun selectLocationById(id: String?, roomId: String? = null) {
        if (id != null) {
            val loc = repository.findById(id)
            val room = if (roomId != null && loc is com.example.unifind.model.Building) {
                loc.floors.flatMap { it.rooms }.find { it.id == roomId }
            } else null
            selectLocation(loc, room)
        } else {
            selectLocation(null, null)
        }
    }

    fun moveLocation(id: String, newMapX: Float, newMapY: Float) {
        val loc = repository.findById(id) ?: return
        val clampedX = newMapX.coerceIn(0f, 1f)
        val clampedY = newMapY.coerceIn(0f, 1f)
        if (loc.mapX == clampedX && loc.mapY == clampedY) return

        undoStack.addLast(LocationPositionChange(id, loc.mapX, loc.mapY, clampedX, clampedY))
        loc.updateCoordinates(clampedX, clampedY)
        _locationsVersion.update { it + 1 }
    }

    fun resetLocation(id: String) {
        val loc = repository.findById(id) ?: return
        if (loc.mapX == loc.originalMapX && loc.mapY == loc.originalMapY) return

        undoStack.addLast(LocationPositionChange(id, loc.mapX, loc.mapY, loc.originalMapX, loc.originalMapY))
        loc.resetCoordinates()
        _locationsVersion.update { it + 1 }
    }

    fun undo(): Boolean {
        if (undoStack.isEmpty()) return false
        val lastChange = undoStack.removeLast()
        val loc = repository.findById(lastChange.locationId) ?: return false
        loc.updateCoordinates(lastChange.oldX, lastChange.oldY)
        _locationsVersion.update { it + 1 }
        return true
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()

    fun resetAllLocations() {
        undoStack.clear()
        repository.getAllLocations().forEach { loc ->
            loc.resetCoordinates()
        }
        _locationsVersion.update { it + 1 }
    }

    fun getCalibratedJson(): String {
        val items = repository.getAllLocations().joinToString(",\n  ") { loc ->
            val xStr = String.format(Locale.US, "%.4f", loc.mapX)
            val yStr = String.format(Locale.US, "%.4f", loc.mapY)
            "{\n    \"id\": \"${loc.id}\",\n    \"mapX\": $xStr,\n    \"mapY\": $yStr\n  }"
        }
        return "[\n  $items\n]"
    }
}

/**
 * OOP Concept: Data Transfer Object / Value Object
 * Holds a snapshot of coordinate change for undo functionality.
 */
data class LocationPositionChange(
    val locationId: String,
    val oldX: Float,
    val oldY: Float,
    val newX: Float,
    val newY: Float
)
