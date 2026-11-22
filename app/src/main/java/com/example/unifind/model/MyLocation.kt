package com.example.unifind.model

/**
 * OOP Concept: Data Transfer Object (DTO) / Encapsulation
 * Represents the user's manually specified campus location coordinates and label.
 */
data class MyLocation(
    val mapX: Float,
    val mapY: Float,
    val label: String
)
