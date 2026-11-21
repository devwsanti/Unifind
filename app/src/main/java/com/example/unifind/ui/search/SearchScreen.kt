package com.example.unifind.ui.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unifind.search.LocationType
import com.example.unifind.search.SearchResult

/**
 * OOP Concept: View / Presenter / Interface Interoperability
 * Renders the search UI and maps user actions to state updates.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onResultClick: (String, String?, String?) -> Unit = { _, _, _ -> }
) {
    val query by viewModel.query.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val results by viewModel.results.collectAsState()
    val groupedResults by viewModel.groupedResults.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(query, filter) {
        if (query.isBlank() && filter == null) {
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search gates, buildings, rooms...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ElevatedFilterChip(
                        selected = filter == null,
                        onClick = { viewModel.updateFilter(null) },
                        label = { Text("All") }
                    )
                }
                items(LocationType.values()) { type ->
                    ElevatedFilterChip(
                        selected = filter == type,
                        onClick = { viewModel.updateFilter(type) },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            if (results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found.", color = MaterialTheme.colorScheme.error)
                }
            } else if (query.isBlank() && filter == null) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedResults.forEach { (type, typeResults) ->
                        if (typeResults.isNotEmpty()) {
                            stickyHeader(key = "header_${type.name}") {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = when (type) {
                                            LocationType.GATE -> "Gates"
                                            LocationType.FACILITY -> "Facilities"
                                            LocationType.BUILDING -> "Buildings"
                                            LocationType.ROOM -> "Rooms"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            items(
                                items = typeResults,
                                key = { result ->
                                    if (result.room != null) "room_${result.room.id}" else "loc_${result.location.id}"
                                }
                            ) { result ->
                                SearchResultItem(result = result, onClick = {
                                    viewModel.resetSearch()
                                    if (result.room != null) {
                                        onResultClick(result.location.id, result.room.floorLabel, result.room.id)
                                    } else {
                                        onResultClick(result.location.id, null, null)
                                    }
                                })
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = results,
                        key = { result ->
                            if (result.room != null) "room_${result.room.id}" else "loc_${result.location.id}"
                        }
                    ) { result ->
                        SearchResultItem(result = result, onClick = {
                            viewModel.resetSearch()
                            if (result.room != null) {
                                onResultClick(result.location.id, result.room.floorLabel, result.room.id)
                            } else {
                                onResultClick(result.location.id, null, null)
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(result: SearchResult, onClick: () -> Unit) {
    val isComingSoon = result.subtitle == "Coming soon"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isComingSoon) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = result.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isComingSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = if (isComingSoon) FontStyle.Italic else FontStyle.Normal
            )
        }
    }
}
