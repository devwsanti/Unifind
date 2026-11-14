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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unifind.UniFindApplication
import com.example.unifind.routing.CampusGraph
import com.example.unifind.routing.DijkstraRouteFinder
import com.example.unifind.routing.RoutePlanner
import com.example.unifind.routing.RouteResult

@Composable
fun EditorOverlay(
    scale: Float,
    containerWidth: Float,
    containerHeight: Float,
    viewModel: GraphEditorViewModel = viewModel()
) {
    val updateTrigger by viewModel.updateTrigger.collectAsState()
    val selectedNodeId by viewModel.selectedNodeId.collectAsState()

    // Read state safely triggering on update
    val state = remember(updateTrigger) { viewModel.editorState }
    val nodes = state.nodes
    val edges = state.edges

    // Draw edges
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeW = 3.dp.toPx() / scale
        for (edge in edges) {
            val n1 = nodes.find { it.id == edge.first }
            val n2 = nodes.find { it.id == edge.second }
            if (n1 == null || n2 == null) continue
            
            // Draw thin white outline
            drawLine(
                color = Color.White,
                start = Offset(n1.mapX * containerWidth, n1.mapY * containerHeight),
                end = Offset(n2.mapX * containerWidth, n2.mapY * containerHeight),
                strokeWidth = strokeW + (2.dp.toPx() / scale)
            )
            // Draw orange core
            drawLine(
                color = Color(0xFFFFA500),
                start = Offset(n1.mapX * containerWidth, n1.mapY * containerHeight),
                end = Offset(n2.mapX * containerWidth, n2.mapY * containerHeight),
                strokeWidth = strokeW
            )
        }
    }

    // Draw nodes
    for (node in nodes) {
        val isSelected = node.id == selectedNodeId
        val baseSize = if (isSelected) 14.dp else 8.dp

        Box(
            modifier = Modifier
                .offset(
                    x = (node.mapX * containerWidth).dp - (baseSize / 2),
                    y = (node.mapY * containerHeight).dp - (baseSize / 2)
                )
                .graphicsLayer(
                    scaleX = 1f / scale,
                    scaleY = 1f / scale,
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(baseSize)
                        .clip(CircleShape)
                        .background(if (node.locationId != null) Color.Green else Color.DarkGray)
                        .border(1.dp, Color.White, CircleShape)
                        .then(if (isSelected) Modifier.border(2.dp, Color.Black, CircleShape) else Modifier)
                        .pointerInput(node.id) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                // Convert dragAmount back to 0..1 relative to container
                                // But dragAmount is in unscaled pixels? Yes, because we consume it here before ZoomableMap
                                val newX = (node.mapX * containerWidth + dragAmount.x) / containerWidth
                                val newY = (node.mapY * containerHeight + dragAmount.y) / containerHeight
                                viewModel.handleNodeDrag(node.id, newX, newY)
                            }
                        }
                        .clickable { viewModel.handleNodeTap(node.id) }
                )
                if (scale >= 2.5f || isSelected) {
                    Text(
                        text = node.locationId ?: node.id,
                        color = Color.Black,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditorToolbar(viewModel: GraphEditorViewModel = viewModel()) {
    val currentTool by viewModel.currentTool.collectAsState()
    val context = LocalContext.current
    var showValidate by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.95f))
    ) {
        // Row 1: Mode chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val tools = GraphEditorViewModel.EditorTool.values()
            tools.forEach { tool ->
                FilterChip(
                    selected = currentTool == tool,
                    onClick = { viewModel.setTool(tool) },
                    label = { Text(tool.name, fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
        
        // Row 2: Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { viewModel.undo() }) { Text("Undo", fontSize = 10.sp, maxLines = 1) }
            TextButton(onClick = { /* TODO Test */ }) { Text("Test", fontSize = 10.sp, maxLines = 1) }
            TextButton(onClick = { showValidate = true }) { Text("Validate", fontSize = 10.sp, maxLines = 1) }
            TextButton(onClick = {
                val json = viewModel.getJson()
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Graph JSON", json))
            }) { Text("Copy", fontSize = 10.sp, maxLines = 1) }
            TextButton(onClick = {
                val json = viewModel.getJson()
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, json)
                }
                context.startActivity(Intent.createChooser(intent, "Share JSON"))
            }) { Text("Share", fontSize = 10.sp, maxLines = 1) }
            TextButton(
                onClick = { showClearConfirm = true },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) { Text("Clear All", fontSize = 10.sp, maxLines = 1) }
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
}
