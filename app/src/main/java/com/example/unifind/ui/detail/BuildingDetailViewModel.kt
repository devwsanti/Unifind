package com.example.unifind.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.unifind.UniFindApplication
import com.example.unifind.model.Building
import com.example.unifind.model.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * OOP Concept: Encapsulation / State Management
 * Holds data state exclusively for the Building Detail Screen.
 */
class BuildingDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as UniFindApplication).repository

    private val _building = MutableStateFlow<Building?>(null)
    val building: StateFlow<Building?> = _building.asStateFlow()

    private val _selectedFloor = MutableStateFlow<String?>(null)
    val selectedFloor: StateFlow<String?> = _selectedFloor.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _highlightedRoomId = MutableStateFlow<String?>(null)
    val highlightedRoomId: StateFlow<String?> = _highlightedRoomId.asStateFlow()

    fun loadBuilding(id: String, floorLabel: String?, roomId: String?) {
        val b = repository.findById(id) as? Building
        _building.update { b }
        _highlightedRoomId.update { roomId }

        if (floorLabel != null && b?.floors?.any { it.label == floorLabel } == true) {
            _selectedFloor.update { floorLabel }
        } else {
            _selectedFloor.update { b?.floors?.firstOrNull()?.label }
        }
    }

    fun updateSelectedFloor(floor: String) {
        _selectedFloor.update { floor }
        _searchQuery.update { "" }
        _highlightedRoomId.update { null }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.update { query }
    }

    // OOP Concept: Utility logic matching SearchEngine internally
    fun getFilteredRooms(): List<Pair<String, Room>> {
        val b = _building.value ?: return emptyList()
        val query = _searchQuery.value.replace(Regex("[ .-]"), "").lowercase()

        val allRooms = b.floors.flatMap { f -> f.rooms.map { r -> f.label to r } }

        if (query.isBlank()) {
            val f = _selectedFloor.value
            return allRooms.filter { it.first == f }
        }

        return allRooms.filter { (_, r) ->
            val normName = r.name.replace(Regex("[ .-]"), "").lowercase()
            val normCollege = r.ownerCollege?.replace(Regex("[ .-]"), "")?.lowercase() ?: ""
            val inShared = r.sharedWith.any { it.replace(Regex("[ .-]"), "").lowercase().contains(query) }

            normName.contains(query) || (normCollege.contains(query) && query.isNotEmpty()) || inShared
        }
    }
}
