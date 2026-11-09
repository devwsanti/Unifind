package com.example.unifind.data.dto

import kotlinx.serialization.Serializable

/**
 * OOP Concept: Data Transfer Object (DTO)
 * The root object mapping the entire campus.json file.
 */
@Serializable
data class CampusDto(
    val gates: List<GateDto> = emptyList(),
    val facilities: List<FacilityDto> = emptyList(),
    val buildings: List<BuildingDto> = emptyList()
)
