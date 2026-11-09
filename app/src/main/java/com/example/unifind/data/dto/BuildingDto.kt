package com.example.unifind.data.dto

import kotlinx.serialization.Serializable

/**
 * OOP Concept: Data Transfer Object (DTO)
 * Maps the Building object from JSON.
 */
@Serializable
data class BuildingDto(
    val id: String,
    val name: String,
    val college: String,
    val status: String,
    val mapX: Float,
    val mapY: Float,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val floors: List<FloorDto> = emptyList()
)
