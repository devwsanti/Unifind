package com.example.unifind.ui.map

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.unifind.BuildConfig
import com.example.unifind.UniFindApplication
import com.example.unifind.model.Building
import com.example.unifind.model.BuildingStatus
import com.example.unifind.model.Facility
import com.example.unifind.model.Gate
import com.example.unifind.routing.DestinationRanker
import com.example.unifind.routing.NearestLocationFinder
import com.example.unifind.routing.NearestNodeFinder
import com.example.unifind.search.LocationType
import com.example.unifind.ui.directions.RouteSummaryFormatter
import com.example.unifind.ui.location.CurrentLocationHolder
import com.example.unifind.ui.map.editor.EditorOverlay
import com.example.unifind.ui.map.editor.EditorToolbar
import com.example.unifind.ui.map.editor.GraphEditorViewModel
import com.example.unifind.ui.navigation.Screen
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    locationId: String? = null,
    openImHere: Boolean = false,
    startPlacing: Boolean = false,
    isEditMode: Boolean = false,
    onEditModeChange: (Boolean) -> Unit = {},
    navController: NavController = rememberNavController(),
    viewModel: MapViewModel = viewModel(),
    editorViewModel: GraphEditorViewModel = viewModel(),
    currentLocationHolder: CurrentLocationHolder = viewModel()
) {
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val isCalibrationMode by viewModel.isCalibrationMode.collectAsState()
    val placedLocations = viewModel.placedLocations
    val allLocationsCount = viewModel.allLocations.size

    val myLocation by currentLocationHolder.myLocation.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var calibrationTap by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var legendExpanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    var showImHereSheet by remember { mutableStateOf(false) }
    var isPlacingLocationOnMap by remember { mutableStateOf(false) }
    var placedDotPos by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var showWhereToGoSheet by remember { mutableStateOf(false) }
    var customFocusTarget by remember { mutableStateOf<Pair<Float, Float>?>(null) }

    val editorUpdateTrigger by editorViewModel.updateTrigger.collectAsState()
    var showValidate by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(openImHere) {
        if (openImHere) {
            showImHereSheet = true
        }
    }

    LaunchedEffect(startPlacing) {
        if (startPlacing) {
            isPlacingLocationOnMap = true
            placedDotPos = myLocation?.let { Pair(it.mapX, it.mapY) }
        }
    }

    LaunchedEffect(locationId) {
        if (locationId != null) {
            customFocusTarget = null
            viewModel.selectLocationById(locationId)
            val loc = viewModel.selectedLocation.value
            if (loc != null && loc.mapX == 0f && loc.mapY == 0f) {
                snackbarHostState.showSnackbar("This location is not placed on the map yet.")
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isEditMode) {
                val trigger = editorUpdateTrigger
                var editorMenuExpanded by remember { mutableStateOf(false) }
                TopAppBar(
                    title = {
                        val nodeCount = editorViewModel.editorState.nodes.size
                        val edgeCount = editorViewModel.editorState.edges.size
                        Column {
                            Text("Map Editor", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("$nodeCount nodes, $edgeCount edges", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    actions = {
                        Button(onClick = { onEditModeChange(false) }) {
                            Text("Exit editor", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box {
                            IconButton(onClick = { editorMenuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Editor Menu")
                            }
                            DropdownMenu(
                                expanded = editorMenuExpanded,
                                onDismissRequest = { editorMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Undo", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        editorViewModel.undo()
                                        editorMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Validate", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        showValidate = true
                                        editorMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Copy JSON", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        val json = editorViewModel.getJson()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Graph JSON", json))
                                        editorMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share JSON", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        val json = editorViewModel.getJson()
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, json)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Share JSON"))
                                        editorMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear all", color = Color.Red, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        showClearConfirm = true
                                        editorMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text("Campus Map", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = "${placedLocations.size} of $allLocationsCount locations placed",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Mode Selector")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (isCalibrationMode) "View mode" else "Calibrate mode",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    onClick = {
                                        viewModel.toggleCalibrationMode()
                                        menuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edit paths", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        if (isCalibrationMode) viewModel.toggleCalibrationMode()
                                        onEditModeChange(true)
                                        menuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Validate graph", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        showValidate = true
                                        menuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ZoomableMap(
                    onMapTap = { x, y ->
                        if (isEditMode) {
                            editorViewModel.handleMapTap(x, y)
                        } else if (isCalibrationMode) {
                            calibrationTap = Pair(x, y)
                        } else if (isPlacingLocationOnMap) {
                            if (x < 0f || x > 1f || y < 0f || y > 1f) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Tap inside the campus map")
                                }
                            } else {
                                placedDotPos = Pair(x, y)
                            }
                        }
                    },
                    focusTarget = customFocusTarget ?: (if (selectedLocation != null && selectedLocation!!.mapX > 0f)
                        Pair(selectedLocation!!.mapX, selectedLocation!!.mapY)
                    else null),
                    hasSheet = (selectedLocation != null)
                ) { scale, offset, rotation ->
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val w = maxWidth.value
                        val h = maxHeight.value
                        val density = LocalDensity.current.density

                        placedLocations.forEach { loc ->
                            val (absX, absY) = MapCoordinates.toAbsolute(loc.mapX, loc.mapY, w, h)
                            MapPin(
                                location = loc,
                                scale = scale,
                                isSelected = loc.id == selectedLocation?.id,
                                onClick = {
                                    if (!isCalibrationMode && !isEditMode && !isPlacingLocationOnMap) {
                                        viewModel.selectLocation(loc)
                                    }
                                },
                                modifier = Modifier.offset(
                                    x = absX.dp,
                                    y = absY.dp
                                ),
                                isEditorMode = isEditMode,
                                rotation = rotation
                            )
                        }

                        if (!isEditMode && (scale >= 2.5f || selectedLocation != null)) {
                            val offsetXDp = offset.x / density
                            val offsetYDp = offset.y / density
                            val visibleMinX = (w / 2f - (w / 2f + offsetXDp) / scale).coerceAtLeast(0f)
                            val visibleMaxX = (w / 2f + (w / 2f - offsetXDp) / scale).coerceAtMost(w)
                            val visibleMinY = (h / 2f - (h / 2f + offsetYDp) / scale).coerceAtLeast(0f)
                            val visibleMaxY = (h / 2f + (h / 2f - offsetYDp) / scale).coerceAtMost(h)

                            val sortedCandidates = placedLocations.sortedByDescending { it.id == selectedLocation?.id }
                            val drawnLabelRects = mutableListOf<Rect>()

                            sortedCandidates.forEach { loc ->
                                val isSel = (loc.id == selectedLocation?.id)
                                if (scale >= 2.5f || isSel) {
                                    val (absX, absY) = MapCoordinates.toAbsolute(loc.mapX, loc.mapY, w, h)
                                    val pinHeightDp = if (isSel) 31f else 24f
                                    val pinWidthDp = pinHeightDp * 0.7f

                                    val isVisibleOnScreen = absX >= visibleMinX && absX <= visibleMaxX && absY >= visibleMinY && absY <= visibleMaxY

                                    if (isVisibleOnScreen) {
                                        val approxTextWidthDp = (loc.name.length * 6f).coerceAtLeast(40f)
                                        val approxTextHeightDp = 18f
                                        val labelLeftDp = absX - (approxTextWidthDp / 2f)
                                        val labelTopDp = absY + (pinHeightDp / 2f) + 2f

                                        val candidateRect = Rect(
                                            left = labelLeftDp,
                                            top = labelTopDp,
                                            right = labelLeftDp + approxTextWidthDp,
                                            bottom = labelTopDp + approxTextHeightDp
                                        )

                                        val overlaps = drawnLabelRects.any { existing ->
                                            candidateRect.overlaps(existing)
                                        }

                                        if (isSel || !overlaps) {
                                            drawnLabelRects.add(candidateRect)
                                            MapPinLabel(
                                                label = loc.name,
                                                scale = scale,
                                                modifier = Modifier.offset(
                                                    x = absX.dp,
                                                    y = absY.dp
                                                ),
                                                rotation = rotation
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (myLocation != null) {
                            val loc = myLocation!!
                            val (absX, absY) = MapCoordinates.toAbsolute(loc.mapX, loc.mapY, w, h)
                            Box(
                                modifier = Modifier.offset(
                                    x = absX.dp,
                                    y = absY.dp
                                )
                            ) {
                                MyLocationDot(
                                    scale = scale,
                                    label = loc.label,
                                    nearPlace = NearestLocationFinder().findNearestLocation(viewModel.allLocations, loc.mapX, loc.mapY)?.name,
                                    rotation = rotation
                                )
                            }
                        }

                        if (placedDotPos != null) {
                            val (px, py) = placedDotPos!!
                            val (absX, absY) = MapCoordinates.toAbsolute(px, py, w, h)
                            Box(
                                modifier = Modifier.offset(
                                    x = absX.dp,
                                    y = absY.dp
                                )
                            ) {
                                MyLocationDot(
                                    scale = scale,
                                    label = "Set location",
                                    nearPlace = null,
                                    rotation = rotation
                                )
                            }
                        }

                        if (isEditMode) {
                            EditorOverlay(
                                scale = scale,
                                containerWidth = w,
                                containerHeight = h,
                                viewModel = editorViewModel
                            )
                        }

                        if (isCalibrationMode && calibrationTap != null) {
                            val (x, y) = calibrationTap!!
                            val (absX, absY) = MapCoordinates.toAbsolute(x, y, w, h)
                            MapPinBase(
                                color = Color.Black,
                                scale = scale,
                                isSelected = true,
                                onClick = {},
                                modifier = Modifier.offset(
                                    x = absX.dp,
                                    y = absY.dp
                                ),
                                isEditorMode = false,
                                rotation = rotation
                            )
                        }
                    }
                }

                if (isPlacingLocationOnMap) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tap where you are", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                isPlacingLocationOnMap = false
                                placedDotPos = null
                            }) {
                                Text("Cancel")
                            }
                        }
                    }

                    if (placedDotPos != null) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val (px, py) = placedDotPos!!
                                        val nearestLoc = NearestLocationFinder().findNearestLocation(viewModel.allLocations, px, py)
                                        currentLocationHolder.setLocation(px, py, nearestLoc?.name ?: "")
                                        isPlacingLocationOnMap = false
                                        placedDotPos = null
                                        showWhereToGoSheet = true
                                    },
                                    modifier = Modifier.semantics { contentDescription = "Set here" }
                                ) {
                                    Text("Set here")
                                }
                                TextButton(
                                    onClick = {
                                        isPlacingLocationOnMap = false
                                        placedDotPos = null
                                    }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }

                if (!isEditMode) {
                    Box(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                        Card {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .clickable { legendExpanded = !legendExpanded }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Legend", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Icon(
                                        if (legendExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                                        contentDescription = null
                                    )
                                }
                                if (legendExpanded) {
                                    Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
                                        LegendItem("Gate", Color.Red)
                                        LegendItem("Building", MaterialTheme.colorScheme.primary)
                                        LegendItem("Facility", Color.Green)
                                        LegendItem("Under construction", Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    val isSheetOrPlacingOpen = showImHereSheet || isPlacingLocationOnMap || showWhereToGoSheet

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (myLocation != null) {
                            if (isSheetOrPlacingOpen) {
                                SmallFloatingActionButton(
                                    onClick = {
                                        val loc = myLocation
                                        if (loc != null) {
                                            customFocusTarget = Pair(loc.mapX, loc.mapY)
                                        }
                                    },
                                    modifier = Modifier.semantics { contentDescription = "Center on me" }
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Center on me")
                                }
                            } else {
                                ExtendedFloatingActionButton(
                                    onClick = {
                                        val loc = myLocation
                                        if (loc != null) {
                                            customFocusTarget = Pair(loc.mapX, loc.mapY)
                                        }
                                    },
                                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                    text = { Text("Center on me") },
                                    modifier = Modifier.semantics { contentDescription = "Center on me" }
                                )
                            }
                        }

                        if (isSheetOrPlacingOpen) {
                            SmallFloatingActionButton(
                                onClick = { showImHereSheet = true },
                                modifier = Modifier.semantics { contentDescription = "I'm here" }
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = "I'm here")
                            }
                        } else {
                            ExtendedFloatingActionButton(
                                onClick = { showImHereSheet = true },
                                icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                text = { Text("I'm here") },
                                modifier = Modifier.semantics { contentDescription = "I'm here" }
                            )
                        }
                    }
                }

                if (isCalibrationMode) {
                    calibrationTap?.let { (x, y) ->
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val strX = String.format(Locale.US, "%.4f", x)
                                val strY = String.format(Locale.US, "%.4f", y)
                                val textToCopy = "\"mapX\": $strX, \"mapY\": $strY"
                                Text("Tapped Position:", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(textToCopy, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Campus Map Coordinate", textToCopy)
                                    clipboard.setPrimaryClip(clip)
                                }) {
                                    Text("Copy", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }

            if (isEditMode) {
                Box(modifier = Modifier.fillMaxWidth().weight(0.25f, fill = false)) {
                    EditorToolbar(viewModel = editorViewModel)
                }
            }
        }

        if (showValidate) {
            AlertDialog(
                onDismissRequest = { showValidate = false },
                title = { Text("Validation Report") },
                text = {
                    val report = editorViewModel.getValidationReport()
                    Column {
                        Text("Nodes: ${report.nodeCount}, Edges: ${report.edgeCount}")
                        Text("Connected Groups: ${report.connectedGroupsCount}" + if (report.connectedGroupsCount > 1) " (WARNING!)" else "")
                        Text("Nodes without edges: ${report.nodesWithNoEdges.size}")
                        Text("Locations missing entry nodes: ${report.missingLocationIds.size}")
                    }
                },
                confirmButton = { Button(onClick = { showValidate = false }) { Text("OK") } }
            )
        }

        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = { Text("Clear all nodes?") },
                text = { Text("This will delete all nodes and edges from the draft. It will not delete assets/graph.json.") },
                confirmButton = {
                    Button(
                        onClick = {
                            editorViewModel.clearAll()
                            showClearConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Clear All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
                }
            )
        }

        if (showImHereSheet) {
            ModalBottomSheet(onDismissRequest = { showImHereSheet = false }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Set my location", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (myLocation == null) {
                        Button(
                            onClick = {
                                showImHereSheet = false
                                isPlacingLocationOnMap = true
                                placedDotPos = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Tap on the map" }
                        ) {
                            Text("Tap on the map")
                        }
                    } else {
                        Button(
                            onClick = {
                                showImHereSheet = false
                                isPlacingLocationOnMap = true
                                placedDotPos = Pair(myLocation!!.mapX, myLocation!!.mapY)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Move my location" }
                        ) {
                            Text("Move my location")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                currentLocationHolder.clearLocation()
                                showImHereSheet = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Clear my location" },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Clear my location")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showWhereToGoSheet) {
            val currentMyLoc = myLocation
            val closestLoc = currentMyLoc?.let {
                NearestLocationFinder().findNearestLocation(viewModel.allLocations, it.mapX, it.mapY)
            }

            var selectedFilter by remember { mutableStateOf("All") }
            var searchQuery by remember { mutableStateOf("") }
            val keyboardController = LocalSoftwareKeyboardController.current

            val searchEngine = (LocalContext.current.applicationContext as UniFindApplication).searchEngine

            val results = if (searchQuery.isBlank()) {
                when (selectedFilter) {
                    "Gates" -> searchEngine.filterByType(LocationType.GATE)
                    "Facilities" -> searchEngine.filterByType(LocationType.FACILITY)
                    "Buildings" -> searchEngine.filterByType(LocationType.BUILDING)
                    "Rooms" -> searchEngine.filterByType(LocationType.ROOM)
                    else -> searchEngine.getAllGrouped().values.flatten()
                }
            } else {
                val searchResults = searchEngine.search(searchQuery)
                when (selectedFilter) {
                    "Gates" -> searchResults.filter { it.location is Gate && it.room == null }
                    "Facilities" -> searchResults.filter { it.location is Facility && it.room == null }
                    "Buildings" -> searchResults.filter { it.location is Building && it.room == null }
                    "Rooms" -> searchResults.filter { it.room != null }
                    else -> searchResults
                }
            }

            val rankedDestinations = DestinationRanker().rankSearchResults(
                results = results,
                getDistanceMeters = { loc ->
                    if (currentMyLoc != null) viewModel.routePlanner.fromPoint(currentMyLoc.mapX, currentMyLoc.mapY, loc.id)?.distanceMeters else null
                },
                query = searchQuery
            )

            ModalBottomSheet(onDismissRequest = { showWhereToGoSheet = false }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val titleText = if (closestLoc != null) {
                        "You are near ${closestLoc.name}. Where do you want to go?"
                    } else {
                        "Where do you want to go?"
                    }
                    Text(titleText, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search destination") },
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
                                selected = selectedFilter == category,
                                onClick = { selectedFilter = category },
                                label = { Text(category) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (rankedDestinations.isEmpty() && searchQuery.isNotBlank()) {
                        Text(
                            text = "No place or room matches '$searchQuery'",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(1f, fill = false)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                            items(rankedDestinations) { item ->
                                val res = item.result
                                val loc = res.location
                                val room = res.room
                                val dist = item.distanceMeters
                                val isUnderConstruction = loc is Building && loc.status == BuildingStatus.UNDER_CONSTRUCTION

                                val headline = if (room != null) {
                                    "${room.name} - ${loc.name}, ${res.floorLabel}"
                                } else {
                                    loc.name
                                }

                                val distanceText = if (isUnderConstruction) {
                                    "Coming soon"
                                } else if (dist != null) {
                                    "about ${RouteSummaryFormatter.formatDistance(dist)} m"
                                } else {
                                    "No path"
                                }

                                val isEnabled = !isUnderConstruction && dist != null

                                ListItem(
                                    headlineContent = { Text(headline) },
                                    supportingContent = {
                                        Text(
                                            distanceText,
                                            color = if (isUnderConstruction) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    modifier = Modifier
                                        .clickable(enabled = isEnabled) {
                                            showWhereToGoSheet = false
                                            val route = if (room != null) {
                                                Screen.Directions.createRoute(toLocationId = loc.id, fromMyLocation = true, toRoomId = room.id)
                                            } else {
                                                Screen.Directions.createRoute(toLocationId = loc.id, fromMyLocation = true)
                                            }
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id)
                                                launchSingleTop = true
                                            }
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showWhereToGoSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Not now")
                    }
                }
            }
        }

        selectedLocation?.let { loc ->
            val isUnderConstruction = loc is Building && loc.status == BuildingStatus.UNDER_CONSTRUCTION
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            ModalBottomSheet(
                onDismissRequest = { viewModel.selectLocation(null) },
                modifier = Modifier.heightIn(max = screenHeight * 0.35f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = loc.name,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        val typeLabel = when {
                            isUnderConstruction -> "Under Construction"
                            loc is Building -> "Building"
                            loc is Gate -> "Gate"
                            loc is Facility -> "Facility"
                            else -> "Location"
                        }
                        SuggestionChip(
                            onClick = {},
                            label = { Text(typeLabel, style = MaterialTheme.typography.labelSmall) }
                        )
                    }

                    if (loc is Building) {
                        if (loc.college.isNotBlank()) {
                            Text(
                                text = loc.college,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        val floorCount = loc.floors.size
                        val roomCount = loc.floors.sumOf { it.rooms.size }
                        Text(
                            text = "$floorCount floors - $roomCount rooms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (loc is Building) {
                            Button(
                                onClick = {
                                    navController.navigate("building/${loc.id}")
                                    viewModel.selectLocation(null)
                                },
                                enabled = !isUnderConstruction,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isUnderConstruction) "Coming soon" else "View details", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        Button(
                            onClick = {
                                navController.navigate(Screen.Directions.createRoute(toLocationId = loc.id)) {
                                    popUpTo(navController.graph.findStartDestination().id)
                                    launchSingleTop = true
                                }
                                viewModel.selectLocation(null)
                            },
                            enabled = !isUnderConstruction,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isUnderConstruction) "Coming soon" else "Directions", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    if (myLocation != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = {
                                navController.navigate(Screen.Directions.createRoute(toLocationId = loc.id, fromMyLocation = true)) {
                                    popUpTo(navController.graph.findStartDestination().id)
                                    launchSingleTop = true
                                }
                                viewModel.selectLocation(null)
                            },
                            enabled = !isUnderConstruction,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Directions from my location", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
