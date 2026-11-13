package com.example.unifind.ui.map

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.unifind.model.Building
import com.example.unifind.model.Location
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    locationId: String? = null,
    navController: NavController = rememberNavController(),
    viewModel: MapViewModel = viewModel()
) {
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val isCalibrationMode by viewModel.isCalibrationMode.collectAsState()
    val placedLocations = viewModel.placedLocations
    val allLocationsCount = viewModel.allLocations.size

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var calibrationTap by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var legendExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(locationId) {
        if (locationId != null) {
            viewModel.selectLocationById(locationId)
            val loc = viewModel.selectedLocation.value
            if (loc != null && loc.mapX == 0f && loc.mapY == 0f) {
                snackbarHostState.showSnackbar("This location is not placed on the map yet.")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Campus Map")
                        Text(
                            text = "${placedLocations.size} of $allLocationsCount locations placed",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Calibrate", modifier = Modifier.padding(end = 8.dp))
                        Switch(
                            checked = isCalibrationMode,
                            onCheckedChange = { viewModel.toggleCalibrationMode() }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ZoomableMap(
                onMapTap = { x, y ->
                    if (isCalibrationMode) {
                        calibrationTap = Pair(x, y)
                    }
                },
                focusTarget = if (locationId != null && selectedLocation != null && selectedLocation!!.mapX > 0f)
                    Pair(selectedLocation!!.mapX, selectedLocation!!.mapY)
                else null
            ) { scale ->
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val w = maxWidth
                    val h = maxHeight
                    placedLocations.forEach { loc ->
                        MapMarker(
                            location = loc,
                            scale = scale,
                            isSelected = loc.id == selectedLocation?.id,
                            onClick = {
                                if (!isCalibrationMode) {
                                    viewModel.selectLocation(loc)
                                }
                            },
                            modifier = Modifier.offset(
                                x = w * loc.mapX - 5.dp,
                                y = h * loc.mapY - 5.dp
                            )
                        )
                    }

                    // Show temporary pin for calibration mode
                    if (isCalibrationMode && calibrationTap != null) {
                        val (x, y) = calibrationTap!!
                        MapMarker(
                            location = com.example.unifind.model.Gate("temp", "Tap", x, y),
                            scale = scale,
                            isSelected = true,
                            onClick = {},
                            modifier = Modifier.offset(
                                x = w * x - 5.dp,
                                y = h * y - 5.dp
                            )
                        )
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Card {
                    Column {
                        Row(
                            modifier = Modifier.clickable { legendExpanded = !legendExpanded }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Legend", style = MaterialTheme.typography.labelLarge)
                            Icon(
                                if (legendExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                                contentDescription = null
                            )
                        }
                        if (legendExpanded) {
                            Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
                                LegendItem("Gate", Color.Red)
                                LegendItem("Building", Color.Blue)
                                LegendItem("Facility", Color.Green)
                                LegendItem("Under construction", Color.Gray)
                            }
                        }
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
                            Text("Tapped Position:")
                            Text(textToCopy, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Campus Map Coordinate", textToCopy)
                                clipboard.setPrimaryClip(clip)
                            }) {
                                Text("Copy")
                            }
                        }
                    }
                }
            }

            selectedLocation?.let { loc ->
                ModalBottomSheet(onDismissRequest = { viewModel.selectLocation(null) }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(loc.name, style = MaterialTheme.typography.titleLarge)
                        Text(loc.describe(), style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        if (loc is Building && loc.floors.isNotEmpty()) {
                            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                                items(loc.floors) { floor ->
                                    Text(floor.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                                    floor.rooms.forEach { room ->
                                        Text("- ${room.name}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (loc is Building) {
                                Button(
                                    onClick = {
                                        navController.navigate("building/${loc.id}")
                                        viewModel.selectLocation(null)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("View details")
                                }
                            }
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Directions (Coming soon)")
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
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
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}