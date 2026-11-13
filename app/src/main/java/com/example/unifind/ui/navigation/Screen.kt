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
    object Map : Screen("map?locationId={locationId}", "Map", Icons.Filled.LocationOn) {
        fun createRoute(locationId: String) = "map?locationId=$locationId"
    }
    object Directions : Screen("directions", "Directions",    Icons.AutoMirrored.Filled.ArrowForward)
}
