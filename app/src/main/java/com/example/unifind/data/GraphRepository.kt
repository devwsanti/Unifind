package com.example.unifind.data

import android.content.Context
import com.example.unifind.data.dto.GraphDto
import com.example.unifind.routing.CampusGraph
import com.example.unifind.routing.WaypointNode
import kotlinx.serialization.json.Json

/**
 * OOP Concept: Repository Pattern
 * Loads graph data from JSON and provides a parsed CampusGraph.
 */
class GraphRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val graph: CampusGraph by lazy {
        val jsonString = context.assets.open("graph.json").bufferedReader().use { it.readText() }
        parseGraphJson(jsonString)
    }

    fun parseGraphJson(jsonString: String): CampusGraph {
        val dto = json.decodeFromString<GraphDto>(jsonString)
        val nodes = dto.nodes.map { WaypointNode(it.id, it.mapX, it.mapY, it.locationId) }
        val edges = dto.edges.map { Pair(it[0], it[1]) }
        return CampusGraph(
            nodes = nodes,
            edges = edges,
            imageWidth = dto.imageWidth,
            imageHeight = dto.imageHeight,
            metersPerPixel = dto.metersPerPixel
        )
    }
}
