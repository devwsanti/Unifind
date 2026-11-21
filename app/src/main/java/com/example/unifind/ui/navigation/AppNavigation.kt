package com.example.unifind.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unifind.UniFindApplication
import com.example.unifind.ui.colleges.CollegeBuildingsScreen
import com.example.unifind.ui.colleges.CollegesScreen
import com.example.unifind.ui.detail.BuildingDetailScreen
import com.example.unifind.ui.directions.DirectionsScreen
import com.example.unifind.ui.home.HomeScreen
import com.example.unifind.ui.location.CurrentLocationHolder
import com.example.unifind.ui.map.MapScreen
import com.example.unifind.ui.search.SearchScreen
import com.example.unifind.ui.search.SearchViewModel
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var isEditMode by remember { mutableStateOf(false) }
    val currentLocationHolder: CurrentLocationHolder = viewModel()
    val searchViewModel: SearchViewModel = viewModel()

    val context = LocalContext.current
    val campusRepository: com.example.unifind.data.CampusRepository = remember(context) {
        (context.applicationContext as UniFindApplication).repository
    }

    val items = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Map,
        Screen.Directions
    )

    val navigateToTab: (Screen, Boolean) -> Unit = { screen, resetSearch ->
        val route = when (screen) {
            Screen.Search -> Screen.Search.route
            Screen.Map -> "map?locationId="
            Screen.Directions -> "directions"
            else -> screen.route
        }
        if (screen == Screen.Search && resetSearch) {
            searchViewModel.resetSearch()
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                                onClick = { navigateToTab(screen, true) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary
                                )
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
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(120)) },
            exitTransition = { fadeOut(animationSpec = tween(120)) },
            popEnterTransition = { fadeIn(animationSpec = tween(120)) },
            popExitTransition = { fadeOut(animationSpec = tween(120)) }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onSearchClick = { filter, query ->
                        searchViewModel.applyInitialState(filter?.name, query)
                        navigateToTab(Screen.Search, false)
                    },
                    onCollegesClick = {
                        navController.navigate("colleges")
                    },
                    onDirectionsClick = {
                        navigateToTab(Screen.Directions, true)
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onResultClick = { locationId, floorLabel, roomId ->
                        if (roomId != null) {
                            navController.navigate("building/$locationId?floor=$floorLabel&roomId=$roomId")
                        } else {
                            navController.navigate(Screen.Map.createRoute(locationId)) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    }
                )
            }
            composable("colleges") {
                CollegesScreen(
                    campusRepository = campusRepository,
                    onCollegeClick = { college ->
                        val encoded = URLEncoder.encode(college, "UTF-8")
                        navController.navigate("colleges/$encoded")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = "colleges/{collegeName}",
                arguments = listOf(navArgument("collegeName") { type = NavType.StringType })
            ) { backStackEntry ->
                val encoded = backStackEntry.arguments?.getString("collegeName") ?: ""
                val collegeName = URLDecoder.decode(encoded, "UTF-8")
                CollegeBuildingsScreen(
                    collegeName = collegeName,
                    campusRepository = campusRepository,
                    onBuildingClick = { buildingId ->
                        navController.navigate("building/$buildingId")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Map.route,
                arguments = listOf(
                    navArgument("locationId") { type = NavType.StringType; nullable = true },
                    navArgument("openImHere") { type = NavType.BoolType; defaultValue = false },
                    navArgument("startPlacing") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val locationId = backStackEntry.arguments?.getString("locationId")
                val openImHere = backStackEntry.arguments?.getBoolean("openImHere") ?: false
                val startPlacing = backStackEntry.arguments?.getBoolean("startPlacing") ?: false
                MapScreen(
                    locationId = locationId,
                    openImHere = openImHere,
                    startPlacing = startPlacing,
                    isEditMode = isEditMode,
                    onEditModeChange = { isEditMode = it },
                    navController = navController,
                    currentLocationHolder = currentLocationHolder
                )
            }
            composable(
                route = Screen.Directions.route,
                arguments = listOf(
                    navArgument("toLocationId") { type = NavType.StringType; nullable = true },
                    navArgument("fromMyLocation") { type = NavType.BoolType; defaultValue = false },
                    navArgument("toRoomId") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val toLocationId = backStackEntry.arguments?.getString("toLocationId")
                val fromMyLocation = backStackEntry.arguments?.getBoolean("fromMyLocation") ?: false
                val toRoomId = backStackEntry.arguments?.getString("toRoomId")
                DirectionsScreen(
                    toLocationId = toLocationId,
                    fromMyLocation = fromMyLocation,
                    toRoomId = toRoomId,
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
                    onBack = { navController.popBackStack() },
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
