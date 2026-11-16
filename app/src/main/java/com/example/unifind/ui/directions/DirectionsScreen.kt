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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unifind.model.Building
import com.example.unifind.model.BuildingStatus
import com.example.unifind.model.Location
import com.example.unifind.ui.map.MapCoordinates
import com.example.unifind.ui.map.MapPinBase
import com.example.unifind.ui.map.ZoomableMap

/**
 * OOP Concept: View / Presenter
 * The UI layer representing the directions workflow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectionsScreen(
    toLocationId: String? = null,
    viewModel: DirectionsViewModel = viewModel()
) {
    LaunchedEffect(toLocationId) {
        if (toLocationId != null) {
            viewModel.setToLocationById(toLocationId)
        }
    }

    val fromLocation by viewModel.fromLocation.collectAsState()
    val toLocation by viewModel.toLocation.collectAsState()
    val routeResult by viewModel.routeResult.collectAsState()
    val allLocations = viewModel.allLocations

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    if (showFromPicker) {
        LocationPickerSheet(
            locations = allLocations,
            onLocationSelected = { loc ->
                viewModel.setFromLocation(loc)
                showFromPicker = false
            },
            onDismissRequest = { showFromPicker = false },
            showMyLocation = true
        )
    }

    if (showToPicker) {
        LocationPickerSheet(
            locations = allLocations,
            onLocationSelected = { loc ->
                viewModel.setToLocation(loc)
                showToPicker = false
            },
            onDismissRequest = { showToPicker = false },
            showMyLocation = false
        )
    }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.fillMaxWidth().clickable { showFromPicker = true }) {
                                OutlinedTextField(
                                    value = fromLocation?.name ?: "",
                                    onValueChange = {},
                                    enabled = false,
                                    label = { Text("From") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth().clickable { showToPicker = true }) {
                                OutlinedTextField(
                                    value = toLocation?.name ?: "",
                                    onValueChange = {},
                                    enabled = false,
                                    label = { Text("To") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                        TextButton(onClick = { viewModel.swapLocations() }) {
                            Text("Swap")
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                val midpoint = routeResult?.nodes?.let { nodes ->
                    if (nodes.isNotEmpty()) {
                        val minX = nodes.minOf { it.mapX }
                        val maxX = nodes.maxOf { it.mapX }
                        val minY = nodes.minOf { it.mapY }
                        val maxY = nodes.maxOf { it.mapY }
                        Pair((minX + maxX) / 2f, (minY + maxY) / 2f)
                    } else null
                }

                ZoomableMap(onMapTap = { _, _ -> }, focusTarget = midpoint) { scale ->
                    val route = routeResult
                    if (route != null && route.nodes.isNotEmpty()) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path()
                            route.nodes.forEachIndexed { index, node ->
                                val (x, y) = MapCoordinates.toAbsolute(node.mapX, node.mapY, maxWidth.value, maxHeight.value)
                                if (index == 0) path.moveTo(x.dp.toPx(), y.dp.toPx())
                                else path.lineTo(x.dp.toPx(), y.dp.toPx())
                            }
                            drawPath(path, color = Color.White, style = Stroke(width = 8.dp.toPx()))
                            drawPath(path, color = Color.Cyan, style = Stroke(width = 4.dp.toPx()))
                        }

                        val startNode = route.nodes.first()
                        val (sx, sy) = MapCoordinates.toAbsolute(startNode.mapX, startNode.mapY, maxWidth.value, maxHeight.value)
                        MapPinBase(
                            color = Color.Green,
                            label = "Start",
                            scale = scale,
                            isSelected = false,
                            onClick = {},
                            modifier = Modifier.offset(x = sx.dp, y = sy.dp)
                        )

                        val endNode = route.nodes.last()
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

            val route = routeResult
            if (route != null) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = RouteSummaryFormatter.formatSummary(route.distanceMeters),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Estimated",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Start at ${fromLocation?.name ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
                        Text("Walk about ${Math.round(route.distanceMeters)} m", style = MaterialTheme.typography.bodyMedium)
                        Text("Arrive at ${toLocation?.name ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (fromLocation != null && toLocation != null) {
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
    onLocationSelected: (Location?) -> Unit,
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column {
                                Text("My location", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text("Coming soon", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                            }
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