package com.example.unifind.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.unifind.UniFindApplication
import com.example.unifind.model.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * OOP Concept: Encapsulation / Observer Pattern
 * Manages the state for the map screen, including selected location and calibration mode.
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as UniFindApplication).repository

    private val _selectedLocation = MutableStateFlow<Location?>(null)
    val selectedLocation: StateFlow<Location?> = _selectedLocation.asStateFlow()

    private val _isCalibrationMode = MutableStateFlow(false)
    val isCalibrationMode: StateFlow<Boolean> = _isCalibrationMode.asStateFlow()

    val allLocations = repository.getAllLocations()
    val placedLocations = allLocations.filter { it.mapX != 0f || it.mapY != 0f }

    fun selectLocation(location: Location?) {
        _selectedLocation.update { location }
    }

    fun selectLocationById(id: String?) {
        if (id != null) {
            _selectedLocation.update { repository.findById(id) }
        } else {
            _selectedLocation.update { null }
        }
    }

    fun toggleCalibrationMode() {
        _isCalibrationMode.update { !it }
    }
}
