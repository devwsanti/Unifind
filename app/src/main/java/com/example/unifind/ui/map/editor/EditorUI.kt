package com.example.unifind.ui.map.editor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unifind.ui.directions.RouteSummaryFormatter

@Composable
fun EditorOverlay(
    scale: Float,
    containerWidth: Float,
    containerHeight: Float,
    viewModel: GraphEditorViewModel = viewModel()
) {
    val updateTrigger by viewModel.updateTrigger.collectAsState()
    val selectedNodeId by viewModel.selectedNodeId.collectAsState()
    val testState by viewModel.testState.collectAsState()
    val allLocations = remember { viewModel.getAllLocations() }
    val locationNameMap = remember(allLocations) { allLocations.associate { it.id to it.name } }

    // Read state safely triggering on update
    val state = remember(updateTrigger) { viewModel.editorState }
    val nodes = state.nodes
    val edges = state.edges

    // Draw edges & test path (Canvas works in PIXELS, container sizes are in DP -> multiply by density)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeW = 3.dp.toPx() / scale
        val outlineW = strokeW + (2.dp.toPx() / scale)
        val widthPx = containerWidth * density
        val heightPx = containerHeight * density

        for (edge in edges) {
            val n1 = nodes.find { it.id == edge.first }
            val n2 = nodes.find { it.id == edge.second }
            if (n1 == null || n2 == null) continue

            val start = Offset(n1.mapX * widthPx, n1.mapY * heightPx)
            val end = Offset(n2.mapX * widthPx, n2.mapY * heightPx)

            // Thin white outline
            drawLine(color = Color.White, start = start, end = end, strokeWidth = outlineW)
            // Orange core
            drawLine(color = Color(0xFFFFA500), start = start, end = end, strokeWidth = strokeW)
        }

        // Draw test route path if active
        val testRouteNodes = testState.routeResult?.nodes
        if (!testRouteNodes.isNullOrEmpty()) {
            val pathW = 6.dp.toPx() / scale
            for (i in 0 until testRouteNodes.size - 1) {
                val n1 = testRouteNodes[i]
                val n2 = testRouteNodes[i + 1]
                val start = Offset(n1.mapX * widthPx, n1.mapY * heightPx)
                val end = Offset(n2.mapX * widthPx, n2.mapY * heightPx)
                drawLine(color = Color(0xFF00E676), start = start, end = end, strokeWidth = pathW)
            }
        }
    }

    // Draw nodes (32dp touch target, 8/14dp visible dot)
    val dens = LocalDensity.current.density
    for (node in nodes) {
        key(node.id) {
            val currentNode by rememberUpdatedState(node)
            val isStartNode = node.id == testState.startNodeId
            val isEndNode = node.id == testState.endNodeId
            val isSelected = node.id == selectedNodeId
            val dotSize = if (isSelected || isStartNode || isEndNode) 14.dp else 8.dp

            val dotColor = if (node.locationId != null) Color(0xFF4CAF50) else Color.DarkGray

            val borderModifier = when {
                isStartNode -> Modifier.border(3.dp, Color(0xFF4CAF50), CircleShape)
                isEndNode -> Modifier.border(3.dp, Color.Red, CircleShape)
                isSelected -> Modifier.border(2.dp, Color.Black, CircleShape)
                else -> Modifier
            }

            Box(
                modifier = Modifier
                    .offset(
                        x = (node.mapX * containerWidth).dp - 16.dp,
                        y = (node.mapY * containerHeight).dp - 16.dp
                    )
                    .size(32.dp)
                    .graphicsLayer(
                        scaleX = 1f / scale,
                        scaleY = 1f / scale,
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    )
                    .pointerInput(node.id, scale) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dxDp = dragAmount.x / (dens * scale)
                            val dyDp = dragAmount.y / (dens * scale)
                            val n = currentNode
                            val newX = (n.mapX + dxDp / containerWidth).coerceIn(0f, 1f)
                            val newY = (n.mapY + dyDp / containerHeight).coerceIn(0f, 1f)
                            viewModel.handleNodeDrag(n.id, newX, newY)
                        }
                    }
                    .clickable { viewModel.handleNodeTap(node.id) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(1.dp, Color.White, CircleShape)
                        .then(borderModifier)
                )
                val locId = node.locationId
                val showLabel = scale >= 2.5f || isSelected || isStartNode || isEndNode || locId != null
                if (showLabel) {
                    val labelText = when {
                        isStartNode -> "Start"
                        isEndNode -> "End"
                        locId != null -> locationNameMap[locId] ?: locId
                        else -> node.id
                    }
                    Text(
                        text = labelText,
                        color = Color.Black,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 28.dp)
                            .wrapContentWidth(unbounded = true)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorToolbar(viewModel: GraphEditorViewModel = viewModel()) {
    val currentTool by viewModel.currentTool.collectAsState()
    val selectedNodeId by viewModel.selectedNodeId.collectAsState()
    val testState by viewModel.testState.collectAsState()
    val updateTrigger by viewModel.updateTrigger.collectAsState()
    val state = remember(updateTrigger) { viewModel.editorState }

    val context = LocalContext.current
    var showValidate by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            // Test Route Card above toolbar when TEST mode has start or end node selected
            if (currentTool == GraphEditorViewModel.EditorTool.TEST && (testState.startNodeId != null || testState.endNodeId != null)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val text = when {
                            testState.noPathFound -> "No path between these nodes"
                            testState.routeResult != null -> RouteSummaryFormatter.formatSummary(testState.routeResult!!.distanceMeters)
                            testState.startNodeId != null -> "Tap end node to test route"
                            else -> ""
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { viewModel.clearTest() }) {
                            Text("Clear test")
                        }
                    }
                }
            }

            // Row 1: Mode chips (Horizontally scrollable with min touch target of 40dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tools = GraphEditorViewModel.EditorTool.values()
                tools.forEach { tool ->
                    FilterChip(
                        selected = currentTool == tool,
                        onClick = { viewModel.setTool(tool) },
                        label = {
                            Text(
                                tool.name,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .semantics { contentDescription = "${tool.name} tool" }
                    )
                }
            }

            // Row 2: Actions (Horizontally scrollable with min touch target of 40dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.undo() },
                    modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                ) {
                    Text("Undo", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
                TextButton(
                    onClick = { showValidate = true },
                    modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                ) {
                    Text("Validate", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
                TextButton(
                    onClick = {
                        val json = viewModel.getJson()
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Graph JSON", json))
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                ) {
                    Text("Copy", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
                TextButton(
                    onClick = {
                        val json = viewModel.getJson()
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share JSON"))
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                ) {
                    Text("Share", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
                TextButton(
                    onClick = { showClearConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                ) {
                    Text("Clear All", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
            }
        }
    }

    if (currentTool == GraphEditorViewModel.EditorTool.LINK && selectedNodeId != null) {
        val allLocations = remember { viewModel.getAllLocations() }
        val selectedNode = state.nodes.find { it.id == selectedNodeId }
        var searchQuery by remember { mutableStateOf("") }

        val locationToNodeMap = remember(updateTrigger) {
            state.nodes.mapNotNull { node ->
                node.locationId?.let { locId -> locId to node.id }
            }.toMap()
        }

        val filteredLocations = remember(searchQuery, allLocations, locationToNodeMap) {
            allLocations
                .filter { loc ->
                    searchQuery.isBlank() || loc.name.contains(searchQuery, ignoreCase = true) || loc.id.contains(searchQuery, ignoreCase = true)
                }
                .sortedWith(compareBy(
                    { locationToNodeMap[it.id] != null },
                    { it.name }
                ))
        }

        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedNode() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Link Location to $selectedNodeId",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (selectedNode?.locationId != null) {
                        TextButton(
                            onClick = {
                                viewModel.linkLocation(null)
                                viewModel.clearSelectedNode()
                            }
                        ) {
                            Text("Unlink", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search location...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(filteredLocations, key = { it.id }) { loc ->
                        val linkedNodeId = locationToNodeMap[loc.id]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.linkLocation(loc.id)
                                    viewModel.clearSelectedNode()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = loc.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (linkedNodeId == null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "not linked",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Linked to $linkedNodeId",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showValidate) {
        AlertDialog(
            onDismissRequest = { showValidate = false },
            title = { Text("Validation Report") },
            text = {
                val report = viewModel.getValidationReport()
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
                        viewModel.clearAll()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
