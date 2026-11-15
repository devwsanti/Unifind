package com.example.unifind.ui.map

/**
 * OOP Concept: Utility / Helper (Pure Function)
 * A pure shared helper for converting relative map coordinate spaces to absolute boundaries.
 */
object MapCoordinates {
    fun toAbsolute(
        mapX: Float,
        mapY: Float,
        containerWidth: Float,
        containerHeight: Float,
        imageAspectRatio: Float = 941f / 1171f
    ): Pair<Float, Float> {
        val containerRatio = containerWidth / containerHeight
        val imageWidth: Float
        val imageHeight: Float
        val offsetX: Float
        val offsetY: Float

        if (containerRatio > imageAspectRatio) {
            imageHeight = containerHeight
            imageWidth = containerHeight * imageAspectRatio
            offsetX = (containerWidth - imageWidth) / 2f
            offsetY = 0f
        } else {
            imageWidth = containerWidth
            imageHeight = containerWidth / imageAspectRatio
            offsetX = 0f
            offsetY = (containerHeight - imageHeight) / 2f
        }

        return Pair(offsetX + mapX * imageWidth, offsetY + mapY * imageHeight)
    }
}
