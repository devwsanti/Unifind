package com.example.unifind.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.unifind.UniFindApplication
import com.example.unifind.search.LocationType
import com.example.unifind.search.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * OOP Concept: State Encapsulation / Observer Pattern (via StateFlow)
 * Manages the UI state for the Search Screen.
 */
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val searchEngine = (application as UniFindApplication).searchEngine

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow<LocationType?>(null)
    val filter: StateFlow<LocationType?> = _filter.asStateFlow()

    private val _results = MutableStateFlow<List<SearchResult>>(emptyList())
    val results: StateFlow<List<SearchResult>> = _results.asStateFlow()

    fun updateQuery(newQuery: String) {
        _query.update { newQuery }
        executeSearch()
    }

    fun updateFilter(newFilter: LocationType?) {
        _filter.update { newFilter }
        executeSearch()
    }

    private fun executeSearch() {
        val currentQuery = _query.value
        val currentFilter = _filter.value

        var newResults = if (currentQuery.isBlank()) {
            emptyList()
        } else {
            searchEngine.search(currentQuery)
        }

        if (currentFilter != null) {
            newResults = newResults.filter { result ->
                when (currentFilter) {
                    LocationType.GATE -> result.location is com.example.unifind.model.Gate && result.room == null
                    LocationType.FACILITY -> result.location is com.example.unifind.model.Facility && result.room == null
                    LocationType.BUILDING -> result.location is com.example.unifind.model.Building && result.room == null
                    LocationType.ROOM -> result.room != null
                }
            }
        }

        _results.update { newResults }
    }
}
