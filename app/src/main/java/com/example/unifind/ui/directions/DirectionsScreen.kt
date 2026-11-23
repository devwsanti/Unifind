package com.example.unifind.ui.directions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.unifind.model.Building
import com.example.unifind.model.BuildingStatus
import com.example.unifind.model.Facility
import com.example.unifind.model.Gate
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
    fromMyLocation: Boolean = false,
    toRoomId: String? = null,
    navController: NavController = rememberNavController(),
    viewModel: DirectionsViewModel = viewModel(),
    currentLocationHolder: CurrentLocationHolder = viewModel()
) {
    val myLocation by currentLocationHolder.myLocation.collectAsState()

    LaunchedEffect(toLocationId, fromMyLocation, toRoomId) {
        if (toLocationId != null) {
            viewModel.setToLocationById(toLocationId, toRoomId)
        }
        if (fromMyLocation) {
            viewModel.setFromMyLocation(myLocation)
        }
    }

    LaunchedEffect(myLocation) {
        viewModel.updateMyLocation(myLocation)
    }

    val fromLocation by viewModel.fromLocation.collectAsState()
    val fromRoom by viewModel.fromRoom.collectAsState()
    val toLocation by viewModel.toLocation.collectAsState()
    val targetRoom by viewModel.targetRoom.collectAsState()
    val isNearestGateAuto by viewModel.isNearestGateAuto.collectAsState()
    val isFromMyLocation by viewModel.isFromMyLocation.collectAsState()
    val routeResult by viewModel.routeResult.collectAsState()
    val altRouteResult by viewModel.altRouteResult.collectAsState()
    val nearestGateResult by viewModel.nearestGateResult.collectAsState()
    val isFastestSelected by viewModel.isFastestSelected.collectAsState()

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    if (showFromPicker) {
        LocationPickerSheet(
            searchEngine = viewModel.searchEngine,
            routePlanner = viewModel.routePlannerInstance,
            myLocation = myLocation,
            onLocationSelected = { loc, room ->
                viewModel.setFromLocation(loc, room)
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
            onSetOnMapSelected = {
                showFromPicker = false
                navController.navigate(Screen.Map.createRoute(startPlacing = true)) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onDismissRequest = { showFromPicker = false },
            showMyLocation = true
        )
    }

    if (showToPicker) {
        LocationPickerSheet(
            searchEngine = viewModel.searchEngine,
            routePlanner = viewModel.routePlannerInstance,
            myLocation = myLocation,
            onLocationSelected = { loc, room ->
                viewModel.setToLocation(loc, room)
                showToPicker = false
            },
            onNearestGateSelected = {},
            onMyLocationSelected = {},
            onSetOnMapSelected = {},
            onDismissRequest = { showToPicker = false },
            showMyLocation = false
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val (fromPrimary, fromSecondary) = when {
                            isFromMyLocation -> {
                                val sec = if (myLocation != null) {
                                    if (myLocation?.label.isNullOrBlank()) "" else "Near ${myLocation?.label}"
                                } else {
                                    "Not set"
                                }
                                "My location" to sec
                            }
                            isNearestGateAuto -> {
                                "Nearest gate (auto)" to (fromLocation?.name ?: "")
                            }
                            fromLocation != null -> {
                                if (fromRoom != null) {
                                    fromRoom!!.name to "${fromLocation!!.name} - ${fromRoom!!.floorLabel}"
                                } else {
                                    fromLocation!!.name to fromLocation!!.describe()
                                }
                            }
                            else -> "" to ""
                        }
                        DirectionField(
                            label = "From",
                            primaryText = fromPrimary,
                            secondaryText = fromSecondary,
                            onClick = { showFromPicker = true }
                        )

                        Spacer(Modifier.height(8.dp))

                        val (toPrimary, toSecondary) = when {
                            toLocation != null -> {
                                if (targetRoom != null) {
                                    targetRoom!!.name to "${toLocation!!.name} - ${targetRoom!!.floorLabel}"
                                } else {
                                    toLocation!!.name to toLocation!!.describe()
                                }
                            }
                            else -> "" to ""
                        }
                        DirectionField(
                            label = "To",
                            primaryText = toPrimary,
                            secondaryText = toSecondary,
                            onClick = { showToPicker = true }
                        )
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
                                navController.navigate(Screen.Map.createRoute(startPlacing = true)) {
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

                ZoomableMap(onMapTap = { _, _ -> }, focusTarget = midpoint, enableRotation = false) { scale, _, _ ->
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
                                nearPlace = loc.label.ifBlank { null },
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

                        val context = androidx.compose.ui.platform.LocalContext.current
                        val graphRepo = remember(context) { com.example.unifind.data.GraphRepository(context) }
                        val gapMeters = if (isFromMyLocation && activeRoute.nodes.size >= 2 && activeRoute.nodes[0].id == "MY_LOCATION") {
                            graphRepo.graph.distanceMeters(activeRoute.nodes[0].mapX, activeRoute.nodes[0].mapY, activeRoute.nodes[1].mapX, activeRoute.nodes[1].mapY)
                        } else 0.0

                        val startText = if (isFromMyLocation) {
                            val label = myLocation?.label
                            val locName = if (!label.isNullOrBlank()) "My location - near $label" else "My location"
                            "Start at $locName"
                        } else if (isNearestGateAuto) {
                            "Start at ${fromLocation?.name ?: "Nearest gate"}"
                        } else {
                            RouteSummaryFormatter.formatStartText(fromLocation?.name ?: "Unknown", fromRoom)
                        }

                        Text(startText, style = MaterialTheme.typography.bodyMedium)
                        if (gapMeters > 5.0) {
                            Text(
                                "Walk about ${RouteSummaryFormatter.formatDistance(gapMeters)} m to the nearest path",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            "Walk about ${RouteSummaryFormatter.formatDistance(activeRoute.distanceMeters)} m",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        val arrivalLines = RouteSummaryFormatter.formatArrivalLines(toLocation?.name ?: "Unknown", targetRoom)
                        arrivalLines.forEach { line ->
                            Text(line, style = MaterialTheme.typography.bodyMedium)
                        }

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
                            text = "Coming soon",
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

@Composable
fun DirectionField(
    label: String,
    primaryText: String,
    secondaryText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (primaryText.isNotBlank()) {
                Text(
                    text = primaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = "Select location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (secondaryText.isNotBlank()) {
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    searchEngine: com.example.unifind.search.SearchEngine,
    routePlanner: com.example.unifind.routing.RoutePlanner,
    myLocation: MyLocation?,
    onLocationSelected: (Location, com.example.unifind.model.Room?) -> Unit,
    onNearestGateSelected: () -> Unit,
    onMyLocationSelected: () -> Unit,
    onSetOnMapSelected: () -> Unit,
    onDismissRequest: () -> Unit,
    showMyLocation: Boolean
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val results = if (query.isBlank()) {
        when (selectedCategory) {
            "Gates" -> searchEngine.filterByType(com.example.unifind.search.LocationType.GATE)
            "Facilities" -> searchEngine.filterByType(com.example.unifind.search.LocationType.FACILITY)
            "Buildings" -> searchEngine.filterByType(com.example.unifind.search.LocationType.BUILDING)
            "Rooms" -> searchEngine.filterByType(com.example.unifind.search.LocationType.ROOM)
            else -> searchEngine.getAllGrouped().values.flatten()
        }
    } else {
        val searchResults = searchEngine.search(query)
        when (selectedCategory) {
            "Gates" -> searchResults.filter { it.location is Gate && it.room == null }
            "Facilities" -> searchResults.filter { it.location is Facility && it.room == null }
            "Buildings" -> searchResults.filter { it.location is Building && it.room == null }
            "Rooms" -> searchResults.filter { it.room != null }
            else -> searchResults
        }
    }

    val rankedResults = com.example.unifind.routing.DestinationRanker().rankSearchResults(
        results = results,
        getDistanceMeters = { loc ->
            if (myLocation != null) routePlanner.fromPoint(myLocation.mapX, myLocation.mapY, loc.id)?.distanceMeters else null
        },
        query = query
    )

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("All", "Gates", "Facilities", "Buildings", "Rooms").forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                if (showMyLocation && query.isEmpty()) {
                    item {
                        Column {
                            if (myLocation != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onMyLocationSelected() }
                                        .padding(vertical = 12.dp)
                                ) {
                                    Column {
                                        val labelText = if (myLocation.label.isNotBlank()) "My location - near ${myLocation.label}" else "My location"
                                        Text(labelText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSetOnMapSelected() }
                                        .padding(vertical = 12.dp)
                                ) {
                                    Column {
                                        Text("Set on map", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        Text("Tap where you are on the map", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

                if (rankedResults.isEmpty() && query.isNotBlank()) {
                    item {
                        Text(
                            text = "No place or room matches '$query'",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(rankedResults) { item ->
                        val res = item.result
                        val loc = res.location
                        val room = res.room
                        val dist = item.distanceMeters
                        val isUnderConstruction = loc is Building && loc.status == BuildingStatus.UNDER_CONSTRUCTION

                        val distanceText = if (isUnderConstruction) {
                            "Coming soon"
                        } else if (dist != null) {
                            "about ${RouteSummaryFormatter.formatDistance(dist)} m"
                        } else {
                            ""
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLocationSelected(loc, room) }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(res.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(res.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (distanceText.isNotBlank()) {
                                    Text(
                                        distanceText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isUnderConstruction) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
