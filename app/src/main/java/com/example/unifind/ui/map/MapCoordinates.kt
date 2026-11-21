package com.example.unifind.ui.map

/**
 * OOP Concept: Utility / Helper (Pure Function)
 * A pure shared helper for converting relative map coordinate spaces to absolute boundaries and back.
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

    fun fromAbsolute(
        absX: Float,
        absY: Float,
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

        val mapX = ((absX - offsetX) / imageWidth).coerceIn(0f, 1f)
        val mapY = ((absY - offsetY) / imageHeight).coerceIn(0f, 1f)
        return Pair(mapX, mapY)
    }

    /**
     * Transforms screen tap coordinates (tapX, tapY) to normalized map coordinates (mapX, mapY) in [0..1].
     * Supports container dimensions, scale, pan offset, and rotation degrees around container center.
     */
    fun screenToMap(
        tapX: Float,
        tapY: Float,
        containerWidth: Float,
        containerHeight: Float,
        scale: Float = 1f,
        offset: androidx.compose.ui.geometry.Offset = androidx.compose.ui.geometry.Offset.Zero,
        rotationDeg: Float = 0f,
        imageAspectRatio: Float = 941f / 1171f
    ): Pair<Float, Float> {
        val containerRatio = containerWidth / containerHeight
        val unscaledImageWidth: Float
        val unscaledImageHeight: Float

        if (containerRatio > imageAspectRatio) {
            unscaledImageHeight = containerHeight
            unscaledImageWidth = containerHeight * imageAspectRatio
        } else {
            unscaledImageWidth = containerWidth
            unscaledImageHeight = containerWidth / imageAspectRatio
        }

        // Center of container
        val cx = containerWidth / 2f
        val cy = containerHeight / 2f

        // Step 1: Tap relative to container center
        val relX = tapX - cx
        val relY = tapY - cy

        // Step 2: Subtract pan offset
        val unshiftedX = relX - offset.x
        val unshiftedY = relY - offset.y

        // Step 3: Rotate by -rotationDeg (un-rotate)
        val rad = Math.toRadians(-rotationDeg.toDouble())
        val cosR = Math.cos(rad).toFloat()
        val sinR = Math.sin(rad).toFloat()

        val unrotatedX = unshiftedX * cosR - unshiftedY * sinR
        val unrotatedY = unshiftedX * sinR + unshiftedY * cosR

        // Step 4: Un-scale
        val unscaledRelX = unrotatedX / scale
        val unscaledRelY = unrotatedY / scale

        // Step 5: Map to [0, 1] relative to top-left of unscaled image
        val mapX = ((unscaledRelX + unscaledImageWidth / 2f) / unscaledImageWidth).coerceIn(0f, 1f)
        val mapY = ((unscaledRelY + unscaledImageHeight / 2f) / unscaledImageHeight).coerceIn(0f, 1f)

        return Pair(mapX, mapY)
    }
}
