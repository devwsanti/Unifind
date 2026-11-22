package com.example.unifind.ui.location

import androidx.lifecycle.ViewModel
import com.example.unifind.model.MyLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * OOP Concept: Encapsulation / Single Source of Truth
 * An activity-scoped ViewModel maintaining the user's manual "My location" state in memory.
 */
class CurrentLocationHolder : ViewModel() {

    private val _myLocation = MutableStateFlow<MyLocation?>(null)
    val myLocation: StateFlow<MyLocation?> = _myLocation.asStateFlow()

    fun setLocation(location: MyLocation?) {
        _myLocation.update { location }
    }

    fun setLocation(mapX: Float, mapY: Float, label: String) {
        _myLocation.update { MyLocation(mapX, mapY, label) }
    }

    fun clearLocation() {
        _myLocation.update { null }
    }
}
