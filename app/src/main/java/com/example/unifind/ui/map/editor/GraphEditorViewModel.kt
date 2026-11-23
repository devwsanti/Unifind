package com.example.unifind.ui.map.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.unifind.UniFindApplication
import com.example.unifind.model.Location
import com.example.unifind.routing.RouteResult
import com.example.unifind.routing.editor.GraphEditorState
import com.example.unifind.routing.editor.ValidationReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

/**
 * OOP Concept: Facade / Adapter
 * Connects the pure Kotlin GraphEditorState to Android's ViewModel and UI.
 */
class GraphEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as UniFindApplication).repository

    enum class EditorTool { ADD, CONNECT, MOVE, DELETE, LINK, TEST }

    data class TestState(
        val startNodeId: String? = null,
        val endNodeId: String? = null,
        val routeResult: RouteResult? = null,
        val noPathFound: Boolean = false
    )

    private val _currentTool = MutableStateFlow(EditorTool.ADD)
    val currentTool: StateFlow<EditorTool> = _currentTool.asStateFlow()

    private val _selectedNodeId = MutableStateFlow<String?>(null)
    val selectedNodeId: StateFlow<String?> = _selectedNodeId.asStateFlow()

    private val _testState = MutableStateFlow(TestState())
    val testState: StateFlow<TestState> = _testState.asStateFlow()

    private val _updateTrigger = MutableStateFlow(0)
    val updateTrigger: StateFlow<Int> = _updateTrigger.asStateFlow()

    val editorState = GraphEditorState()

    private val draftFile = File(application.filesDir, "graph_draft.json")

    init {
        if (draftFile.exists()) {
            editorState.fromJson(draftFile.readText())
        } else {
            try {
                val jsonString = application.assets.open("graph.json").bufferedReader().use { it.readText() }
                editorState.fromJson(jsonString)
            } catch (e: Exception) {
                // Initial empty state
            }
        }
    }

    fun setTool(tool: EditorTool) {
        _currentTool.update { tool }
        if (tool != EditorTool.LINK) {
            _selectedNodeId.update { null }
        }
        if (tool != EditorTool.TEST) {
            _testState.update { TestState() }
        }
    }

    fun clearSelectedNode() {
        _selectedNodeId.update { null }
    }

    fun clearTest() {
        _testState.update { TestState() }
    }

    fun handleMapTap(mapX: Float, mapY: Float) {
        if (_currentTool.value == EditorTool.ADD) {
            editorState.addNode(mapX, mapY)
            notifyStateChanged()
        }
    }

    fun handleNodeTap(nodeId: String) {
        when (_currentTool.value) {
            EditorTool.CONNECT -> {
                val currentSelected = _selectedNodeId.value
                if (currentSelected == null) {
                    _selectedNodeId.update { nodeId }
                } else {
                    editorState.connect(currentSelected, nodeId)
                    _selectedNodeId.update { null }
                    notifyStateChanged()
                }
            }
            EditorTool.DELETE -> {
                editorState.deleteNode(nodeId)
                if (_selectedNodeId.value == nodeId) {
                    _selectedNodeId.update { null }
                }
                val currentTest = _testState.value
                if (currentTest.startNodeId == nodeId || currentTest.endNodeId == nodeId) {
                    _testState.update { TestState() }
                }
                notifyStateChanged()
            }
            EditorTool.LINK -> {
                _selectedNodeId.update { nodeId }
            }
            EditorTool.TEST -> {
                val current = _testState.value
                if (current.startNodeId == null || current.endNodeId != null || current.noPathFound) {
                    _testState.update { TestState(startNodeId = nodeId) }
                } else {
                    val startId = current.startNodeId
                    val route = editorState.findShortestPath(startId, nodeId)
                    _testState.update {
                        TestState(
                            startNodeId = startId,
                            endNodeId = nodeId,
                            routeResult = route,
                            noPathFound = (route == null)
                        )
                    }
                }
            }
            else -> {
                _selectedNodeId.update { nodeId }
            }
        }
    }

    fun handleNodeDrag(nodeId: String, newX: Float, newY: Float) {
        if (_currentTool.value == EditorTool.MOVE) {
            editorState.moveNode(nodeId, newX, newY)
            notifyStateChanged()
        }
    }

    fun linkLocation(locationId: String?) {
        val nodeId = _selectedNodeId.value
        if (nodeId != null) {
            editorState.setLocationId(nodeId, locationId)
            notifyStateChanged()
        }
    }

    fun undo() {
        editorState.undo()
        notifyStateChanged()
    }

    fun clearAll() {
        editorState.clearAll()
        _selectedNodeId.update { null }
        _testState.update { TestState() }
        notifyStateChanged()
    }

    fun getAllLocations(): List<Location> {
        return repository.getAllLocations()
    }

    private fun notifyStateChanged() {
        draftFile.writeText(editorState.toJson())
        _updateTrigger.update { it + 1 }
    }

    fun getValidationReport(): ValidationReport {
        val allLocationIds = repository.getAllLocations().map { it.id }
        return editorState.validate(allLocationIds)
    }

    fun getJson(): String {
        return editorState.toJson()
    }
}
