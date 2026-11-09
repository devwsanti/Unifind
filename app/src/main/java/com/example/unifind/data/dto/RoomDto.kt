package com.example.unifind.data.dto

import kotlinx.serialization.Serializable

/**
 * OOP Concept: Data Transfer Object (DTO)
 * Maps the Room object from JSON.
 */
@Serializable
data class RoomDto(
    val id: String,
    val name: String,
    val floorLabel: String,
    val ownerCollege: String?,
    val sharedWith: List<String>
)
