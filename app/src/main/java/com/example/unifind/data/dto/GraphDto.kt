package com.example.unifind.data.dto

import kotlinx.serialization.Serializable

/**
 * OOP Concept: Data Transfer Object (DTO)
 * Maps the raw graph JSON structure.
 */
@Serializable
data class GraphDto(
    val imageWidth: Int,
    val imageHeight: Int,
    val metersPerPixel: Double,
    val nodes: List<GraphNodeDto>,
    val edges: List<List<String>>
)

@Serializable
data class GraphNodeDto(
    val id: String,
    val mapX: Float,
    val mapY: Float,
    val locationId: String? = null
)
