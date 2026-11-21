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
    object Search : Screen("search", "Search", Icons.Filled.Search)
    object Map : Screen("map?locationId={locationId}&openImHere={openImHere}&startPlacing={startPlacing}", "Map", Icons.Filled.LocationOn) {
        fun createRoute(
            locationId: String? = null,
            openImHere: Boolean = false,
            startPlacing: Boolean = false
        ): String {
            val params = mutableListOf<String>()
            if (locationId != null) params.add("locationId=$locationId")
            if (openImHere) params.add("openImHere=true")
            if (startPlacing) params.add("startPlacing=true")
            return if (params.isNotEmpty()) "map?${params.joinToString("&")}" else "map"
        }
    }
    object Directions : Screen("directions?toLocationId={toLocationId}&fromMyLocation={fromMyLocation}&toRoomId={toRoomId}", "Directions", Icons.AutoMirrored.Filled.ArrowForward) {
        fun createRoute(
            toLocationId: String? = null,
            fromMyLocation: Boolean = false,
            toRoomId: String? = null
        ): String {
            val params = mutableListOf<String>()
            if (toLocationId != null) params.add("toLocationId=$toLocationId")
            if (fromMyLocation) params.add("fromMyLocation=true")
            if (toRoomId != null) params.add("toRoomId=$toRoomId")
            return if (params.isNotEmpty()) "directions?${params.joinToString("&")}" else "directions"
        }
    }
}
