package com.example.unifind.data.dto

import kotlinx.serialization.Serializable

/**
 * OOP Concept: Data Transfer Object (DTO)
 * Maps the Facility object from JSON.
 */
@Serializable
data class FacilityDto(
    val id: String,
    val name: String,
    val category: String,
    val mapX: Float,
    val mapY: Float,
    val latitude: Double? = null,
    val longitude: Double? = null
)
