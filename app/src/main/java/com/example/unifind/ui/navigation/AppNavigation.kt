package com.example.unifind.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import com.example.unifind.ui.detail.BuildingDetailScreen
import com.example.unifind.ui.directions.DirectionsScreen
import com.example.unifind.ui.home.HomeScreen
import com.example.unifind.ui.map.MapScreen
import com.example.unifind.ui.search.SearchScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var isEditMode by remember { mutableStateOf(false) }
    
    val items = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Map,
        Screen.Directions
    )

    Scaffold(
        bottomBar = {
            if (!isEditMode) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                val route = if (screen == Screen.Map) "map?locationId=" else screen.route
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
                arguments = listOf(navArgument("locationId") { 
                    type = NavType.StringType 
                    nullable = true 
                })
            ) { backStackEntry ->
                val locationId = backStackEntry.arguments?.getString("locationId")
                MapScreen(
                    locationId = locationId,
                    isEditMode = isEditMode,
                    onEditModeChange = { isEditMode = it },
                    navController = navController
                )
            }
            composable(Screen.Directions.route) { DirectionsScreen() }
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
                    }
                )
            }
        }
    }
}
