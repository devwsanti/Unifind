package com.example.unifind.ui.directions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.unifind.model.Building
import com.example.unifind.model.BuildingStatus
import com.example.unifind.model.Location
import com.example.unifind.model.MyLocation
import com.example.unifind.ui.location.CurrentLocationHolder
import com.example.unifind.ui.map.MapCoordinates
import com.example.unifind.ui.map.MapPinBase
import com.example.unifind.ui.map.MyLocationDot
import com.example.unifind.ui.map.ZoomableMap
import com.example.unifind.ui.navigation.Screen

/**
 * OOP Concept: View / Presenter
 * The UI layer representing the directions workflow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectionsScreen(
    toLocationId: String? = null,
    navController: NavController = rememberNavController(),
    viewModel: DirectionsViewModel = viewModel(),
    currentLocationHolder: CurrentLocationHolder = viewModel()
) {
    val myLocation by currentLocationHolder.myLocation.collectAsState()

    LaunchedEffect(toLocationId) {
        if (toLocationId != null) {
            viewModel.setToLocationById(toLocationId)
        }
    }

    LaunchedEffect(myLocation) {
        viewModel.updateMyLocation(myLocation)
    }

    val fromLocation by viewModel.fromLocation.collectAsState()
    val toLocation by viewModel.toLocation.collectAsState()
    val isNearestGateAuto by viewModel.isNearestGateAuto.collectAsState()
    val isFromMyLocation by viewModel.isFromMyLocation.collectAsState()
    val routeResult by viewModel.routeResult.collectAsState()
    val altRouteResult by viewModel.altRouteResult.collectAsState()
    val nearestGateResult by viewModel.nearestGateResult.collectAsState()
    val isFastestSelected by viewModel.isFastestSelected.collectAsState()
    val allLocations = viewModel.allLocations

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    if (showFromPicker) {
        LocationPickerSheet(
            locations = allLocations,
            myLocation = myLocation,
            onLocationSelected = { loc ->
                viewModel.setFromLocation(loc)
                showFromPicker = false
            },
            onNearestGateSelected = {
                viewModel.setFromNearestGateAuto()
                showFromPicker = false
            },
            onMyLocationSelected = {
                viewModel.setFromMyLocation(myLocation)
                showFromPicker = false
            },
            onDismissRequest = { showFromPicker = false },
            showMyLocation = true
        )
    }

    if (showToPicker) {
        LocationPickerSheet(
            locations = allLocations,
            myLocation = myLocation,
            onLocationSelected = { loc ->
                viewModel.setToLocation(loc)
                showToPicker = false
            },
            onNearestGateSelected = {},
            onMyLocationSelected = {},
            onDismissRequest = { showToPicker = false },
            showMyLocation = false
        )
    }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().clickable { showFromPicker = true }) {
                            val fromText = if (isFromMyLocation) {
                                if (myLocation != null) "My location (${myLocation?.label})" else "My location"
                            } else if (isNearestGateAuto) {
                                "Nearest gate (auto)"
                            } else {
                                fromLocation?.name ?: ""
                            }
                            OutlinedTextField(
                                value = fromText,
                                onValueChange = {},
                                enabled = false,
                                label = { Text("From") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().clickable { showToPicker = true }) {
                            OutlinedTextField(
                                value = toLocation?.name ?: "",
                                onValueChange = {},
                                enabled = false,
                                label = { Text("To") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.swapLocations() }) {
                        Text("Swap")
                    }
                }
            }

            if (isFromMyLocation && myLocation == null) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Set your location first",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                navController.navigate(Screen.Map.createRoute(openImHere = true)) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        ) {
                            Text("Open Map")
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
            ) {
                val currentSelectedRoute = if (isFastestSelected) routeResult else (altRouteResult ?: routeResult)
                val currentOtherRoute = if (isFastestSelected) altRouteResult else routeResult

                val midpoint = currentSelectedRoute?.nodes?.let { nodes ->
                    if (nodes.isNotEmpty()) {
                        val minX = nodes.minOf { it.mapX }
                        val maxX = nodes.maxOf { it.mapX }
                        val minY = nodes.minOf { it.mapY }
                        val maxY = nodes.maxOf { it.mapY }
                        Pair((minX + maxX) / 2f, (minY + maxY) / 2f)
                    } else null
                }

                ZoomableMap(onMapTap = { _, _ -> }, focusTarget = midpoint) { scale ->
                    if (currentOtherRoute != null && currentOtherRoute != currentSelectedRoute && currentOtherRoute.nodes.isNotEmpty()) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                            val path = Path()
                            currentOtherRoute.nodes.forEachIndexed { index, node ->
                                val (x, y) = MapCoordinates.toAbsolute(node.mapX, node.mapY, maxWidth.value, maxHeight.value)
                                if (index == 0) path.moveTo(x.dp.toPx(), y.dp.toPx())
                                else path.lineTo(x.dp.toPx(), y.dp.toPx())
                            }
                            drawPath(
                                path = path,
                                color = Color.Gray,
                                style = Stroke(width = 4.dp.toPx(), pathEffect = dashedEffect)
                            )
                        }
                    }

                    if (currentSelectedRoute != null && currentSelectedRoute.nodes.isNotEmpty()) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path()
                            currentSelectedRoute.nodes.forEachIndexed { index, node ->
                                val (x, y) = MapCoordinates.toAbsolute(node.mapX, node.mapY, maxWidth.value, maxHeight.value)
                                if (index == 0) path.moveTo(x.dp.toPx(), y.dp.toPx())
                                else path.lineTo(x.dp.toPx(), y.dp.toPx())
                            }
                            drawPath(path, color = Color.White, style = Stroke(width = 8.dp.toPx()))
                            drawPath(path, color = Color.Cyan, style = Stroke(width = 4.dp.toPx()))
                        }

                        if (isFromMyLocation && myLocation != null) {
                            val loc = myLocation!!
                            val (mx, my) = MapCoordinates.toAbsolute(loc.mapX, loc.mapY, maxWidth.value, maxHeight.value)
                            MyLocationDot(
                                scale = scale,
                                label = "You are here",
                                modifier = Modifier.offset(x = mx.dp, y = my.dp)
                            )
                        } else {
                            val startNode = currentSelectedRoute.nodes.first()
                            val (sx, sy) = MapCoordinates.toAbsolute(startNode.mapX, startNode.mapY, maxWidth.value, maxHeight.value)
                            MapPinBase(
                                color = Color.Green,
                                label = "Start",
                                scale = scale,
                                isSelected = false,
                                onClick = {},
                                modifier = Modifier.offset(x = sx.dp, y = sy.dp)
                            )
                        }

                        val endNode = currentSelectedRoute.nodes.last()
                        val (ex, ey) = MapCoordinates.toAbsolute(endNode.mapX, endNode.mapY, maxWidth.value, maxHeight.value)
                        MapPinBase(
                            color = Color.Red,
                            label = "End",
                            scale = scale,
                            isSelected = false,
                            onClick = {},
                            modifier = Modifier.offset(x = ex.dp, y = ey.dp)
                        )
                    }
                }
            }

            val fastest = routeResult
            val alt = altRouteResult

            if (fastest != null && alt != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isFastestSelected,
                        onClick = { viewModel.selectFastestRoute() },
                        label = {
                            Text("Fastest (${RouteSummaryFormatter.formatDistance(fastest.distanceMeters)} m, ${fastest.walkingMinutes} min)")
                        }
                    )
                    FilterChip(
                        selected = !isFastestSelected,
                        onClick = { viewModel.selectAltRoute() },
                        label = {
                            Text("Alternative (${RouteSummaryFormatter.formatDistance(alt.distanceMeters)} m, ${alt.walkingMinutes} min)")
                        }
                    )
                }
            }

            val activeRoute = if (isFastestSelected) fastest else (alt ?: fastest)
            if (activeRoute != null) {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = RouteSummaryFormatter.formatSummary(activeRoute.distanceMeters),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Estimated",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))

                        if (isNearestGateAuto && nearestGateResult != null) {
                            val bestG = nearestGateResult!!.bestGate
                            val bestR = nearestGateResult!!.bestRoute
                            Text(
                                text = "Best entrance: ${bestG.name} (about ${RouteSummaryFormatter.formatDistance(bestR.distanceMeters)} m)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                        }

                        val startName = if (isFromMyLocation) {
                            "My location (${myLocation?.label ?: ""})"
                        } else if (isNearestGateAuto) {
                            fromLocation?.name
                        } else {
                            fromLocation?.name ?: "Unknown"
                        }
                        Text("Start at ${startName ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Walk about ${RouteSummaryFormatter.formatDistance(activeRoute.distanceMeters)} m",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text("Arrive at ${toLocation?.name ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)

                        if (isNearestGateAuto && nearestGateResult != null) {
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Gate distances:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            nearestGateResult!!.allOptions.forEach { option ->
                                val distStr = if (option.route != null) {
                                    "about ${RouteSummaryFormatter.formatDistance(option.route.distanceMeters)} m"
                                } else {
                                    "No path"
                                }
                                Text("• ${option.gate.name}: $distStr", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            } else if ((fromLocation != null || isNearestGateAuto || (isFromMyLocation && myLocation != null)) && toLocation != null) {
                val toLoc = toLocation
                if (toLoc is Building && toLoc.status == BuildingStatus.UNDER_CONSTRUCTION) {
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = "${toLoc.name}: Coming soon",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = "No walkable path found",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    locations: List<Location>,
    myLocation: MyLocation?,
    onLocationSelected: (Location?) -> Unit,
    onNearestGateSelected: () -> Unit,
    onMyLocationSelected: () -> Unit,
    onDismissRequest: () -> Unit,
    showMyLocation: Boolean
) {
    var query by remember { mutableStateOf("") }
    val filtered = locations.filter { it.name.contains(query, ignoreCase = true) }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search location") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                if (showMyLocation && query.isEmpty()) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMyLocationSelected() }
                                    .padding(vertical = 12.dp)
                            ) {
                                Column {
                                    Text("My location", color = MaterialTheme.colorScheme.primary)
                                    if (myLocation != null) {
                                        Text(myLocation.label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        Text("Not set", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNearestGateSelected() }
                                    .padding(vertical = 12.dp)
                            ) {
                                Text("Nearest gate (auto)", color = MaterialTheme.colorScheme.primary)
                            }
                            HorizontalDivider()
                        }
                    }
                }
                items(filtered) { loc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLocationSelected(loc) }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(loc.name)
                    }
                }
            }
        }
    }
}
