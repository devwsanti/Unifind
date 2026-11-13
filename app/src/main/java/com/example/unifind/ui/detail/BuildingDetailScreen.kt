package com.example.unifind.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unifind.model.BuildingStatus
import com.example.unifind.model.Room

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingDetailScreen(
    buildingId: String,
    floorLabel: String?,
    roomId: String?,
    onShowOnMap: (String) -> Unit,
    viewModel: BuildingDetailViewModel = viewModel()
) {
    LaunchedEffect(buildingId, floorLabel, roomId) {
        viewModel.loadBuilding(buildingId, floorLabel, roomId)
    }

    val building by viewModel.building.collectAsState()
    val selectedFloor by viewModel.selectedFloor.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val highlightedRoomId by viewModel.highlightedRoomId.collectAsState()

    val b = building
    if (b == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Building not found.")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(b.name) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(b.describe(), style = MaterialTheme.typography.bodyLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onShowOnMap(b.id) }, modifier = Modifier.weight(1f)) {
                    Text("Show on map")
                }
                Button(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) {
                    Text("Directions")
                }
            }

            if (b.status == BuildingStatus.UNDER_CONSTRUCTION) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Coming soon", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                }
            } else {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search rooms in ${b.name}...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )

                if (searchQuery.isEmpty() && b.floors.isNotEmpty()) {
                    val floorIndex = b.floors.indexOfFirst { it.label == selectedFloor }.coerceAtLeast(0)
                    ScrollableTabRow(
                        selectedTabIndex = floorIndex,
                        edgePadding = 0.dp
                    ) {
                        b.floors.forEach { f ->
                            Tab(
                                selected = f.label == selectedFloor,
                                onClick = { viewModel.updateSelectedFloor(f.label) },
                                text = { Text(f.label) }
                            )
                        }
                    }
                }

                val filtered = viewModel.getFilteredRooms()

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { (floor, room) ->
                        RoomCard(
                            room = room,
                            floorLabel = floor,
                            isHighlighted = room.id == highlightedRoomId,
                            showFloorLabel = searchQuery.isNotEmpty()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoomCard(room: Room, floorLabel: String, isHighlighted: Boolean, showFloorLabel: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(room.name, style = MaterialTheme.typography.titleMedium)
            if (showFloorLabel) {
                Text(floorLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (room.ownerCollege != null || room.sharedWith.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                val ownerStr = room.ownerCollege ?: "Unassigned"
                val sharedStr = if (room.sharedWith.isNotEmpty()) " - shared with ${room.sharedWith.joinToString(", ")}" else ""
                Text("Owned by $ownerStr$sharedStr", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
