package com.example.unifind.data.dto

import kotlinx.serialization.Serializable

/**
 * OOP Concept: Data Transfer Object (DTO) / Composition
 * Maps the Floor object from JSON.
 */
@Serializable
data class FloorDto(
    val label: String,
    val rooms: List<RoomDto> = emptyList()
)
