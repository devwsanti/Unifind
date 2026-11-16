package com.example.unifind.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unifind.ui.detail.BuildingDetailScreen
import com.example.unifind.ui.directions.DirectionsScreen
import com.example.unifind.ui.home.HomeScreen
import com.example.unifind.ui.location.CurrentLocationHolder
import com.example.unifind.ui.map.MapScreen
import com.example.unifind.ui.search.SearchScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var isEditMode by remember { mutableStateOf(false) }
    val currentLocationHolder: CurrentLocationHolder = viewModel()

    val items = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Map,
        Screen.Directions
    )

    Scaffold(
        bottomBar = {
            if (!isEditMode) {
                val currentDensity = LocalDensity.current
                val cappedDensity = Density(currentDensity.density, fontScale = minOf(1.15f, currentDensity.fontScale))
                CompositionLocalProvider(LocalDensity provides cappedDensity) {
                    NavigationBar {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(imageVector = screen.icon, contentDescription = screen.label) },
                                label = { Text(screen.label, maxLines = 1, softWrap = false, fontSize = 11.sp) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    val route = when (screen) {
                                        Screen.Map -> "map?locationId="
                                        Screen.Directions -> "directions"
                                        else -> screen.route
                                    }
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onSearchClick = {
                        navController.navigate(Screen.Search.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onResultClick = { locationId, floorLabel, roomId ->
                        if (roomId != null) {
                            navController.navigate("building/$locationId?floor=$floorLabel&roomId=$roomId")
                        } else {
                            navController.navigate(Screen.Map.createRoute(locationId)) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
            composable(
                route = Screen.Map.route,
                arguments = listOf(
                    navArgument("locationId") { type = NavType.StringType; nullable = true },
                    navArgument("openImHere") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val locationId = backStackEntry.arguments?.getString("locationId")
                val openImHere = backStackEntry.arguments?.getBoolean("openImHere") ?: false
                MapScreen(
                    locationId = locationId,
                    openImHere = openImHere,
                    isEditMode = isEditMode,
                    onEditModeChange = { isEditMode = it },
                    navController = navController,
                    currentLocationHolder = currentLocationHolder
                )
            }
            composable(
                route = Screen.Directions.route,
                arguments = listOf(navArgument("toLocationId") {
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                val toLocationId = backStackEntry.arguments?.getString("toLocationId")
                DirectionsScreen(
                    toLocationId = toLocationId,
                    navController = navController,
                    currentLocationHolder = currentLocationHolder
                )
            }
            composable(
                route = "building/{id}?floor={floor}&roomId={roomId}",
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType },
                    navArgument("floor") { type = NavType.StringType; nullable = true },
                    navArgument("roomId") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                val floor = backStackEntry.arguments?.getString("floor")
                val roomId = backStackEntry.arguments?.getString("roomId")
                BuildingDetailScreen(
                    buildingId = id,
                    floorLabel = floor,
                    roomId = roomId,
                    onShowOnMap = { locId ->
                        navController.navigate(Screen.Map.createRoute(locId)) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    },
                    onDirections = { locId ->
                        navController.navigate(Screen.Directions.createRoute(locId)) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}