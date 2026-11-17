package com.example.unifind.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * OOP Concept: Sealed Class / Abstraction / Polymorphism
 * Represents the distinct destinations in the app navigation.
 */
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Search : Screen("search?filter={filter}&query={query}", "Search", Icons.Filled.Search) {
        fun createRoute(filter: String? = null, query: String? = null): String {
            val params = mutableListOf<String>()
            if (filter != null) params.add("filter=$filter")
            if (query != null) params.add("query=$query")
            return if (params.isNotEmpty()) "search?${params.joinToString("&")}" else "search"
        }
    }
    object Map : Screen("map?locationId={locationId}&openImHere={openImHere}", "Map", Icons.Filled.LocationOn) {
        fun createRoute(locationId: String? = null, openImHere: Boolean = false): String {
            val params = mutableListOf<String>()
            if (locationId != null) params.add("locationId=$locationId")
            if (openImHere) params.add("openImHere=true")
            return if (params.isNotEmpty()) "map?${params.joinToString("&")}" else "map"
        }
    }
    object Directions : Screen("directions?toLocationId={toLocationId}", "Directions", Icons.AutoMirrored.Filled.ArrowForward) {
        fun createRoute(toLocationId: String) = "directions?toLocationId=$toLocationId"
    }
}
