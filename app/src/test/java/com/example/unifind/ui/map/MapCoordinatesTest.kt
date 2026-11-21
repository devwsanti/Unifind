package com.example.unifind.ui.map

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * OOP Concept: Testing
 * Pure offline unit test validating aspect-ratio math mappings and inverse coordinate transforms.
 */
class MapCoordinatesTest {
    @Test
    fun testToAbsolute() {
        val aspectRatio = 100f / 200f // 0.5
        // Container is 200x200 (ratio 1.0 > 0.5). So image height will be 200, width will be 100.
        // offsetX = (200 - 100)/2 = 50. offsetY = 0.

        // (0,0) -> (50, 0)
        var pos = MapCoordinates.toAbsolute(0f, 0f, 200f, 200f, aspectRatio)
        assertEquals(50f, pos.first, 0.01f)
        assertEquals(0f, pos.second, 0.01f)

        // (1,1) -> (150, 200)
        pos = MapCoordinates.toAbsolute(1f, 1f, 200f, 200f, aspectRatio)
        assertEquals(150f, pos.first, 0.01f)
        assertEquals(200f, pos.second, 0.01f)

        // (0.5, 0.5) -> (100, 100)
        pos = MapCoordinates.toAbsolute(0.5f, 0.5f, 200f, 200f, aspectRatio)
        assertEquals(100f, pos.first, 0.01f)
        assertEquals(100f, pos.second, 0.01f)
    }

    @Test
    fun testScreenToMap_Rotation0() {
        val aspectRatio = 1.0f // 200x200 container
        // Center tap (100, 100) -> (0.5, 0.5)
        var mapPos = MapCoordinates.screenToMap(
            tapX = 100f,
            tapY = 100f,
            containerWidth = 200f,
            containerHeight = 200f,
            scale = 1f,
            offset = Offset.Zero,
            rotationDeg = 0f,
            imageAspectRatio = aspectRatio
        )
        assertEquals(0.5f, mapPos.first, 0.01f)
        assertEquals(0.5f, mapPos.second, 0.01f)

        // Top-left tap (0, 0) -> (0.0, 0.0)
        mapPos = MapCoordinates.screenToMap(
            tapX = 0f,
            tapY = 0f,
            containerWidth = 200f,
            containerHeight = 200f,
            scale = 1f,
            offset = Offset.Zero,
            rotationDeg = 0f,
            imageAspectRatio = aspectRatio
        )
        assertEquals(0.0f, mapPos.first, 0.01f)
        assertEquals(0.0f, mapPos.second, 0.01f)
    }

    @Test
    fun testScreenToMap_Rotation90() {
        val aspectRatio = 1.0f
        // 90 deg rotation clockwise: Right edge (1.0, 0.5) moves to screen bottom-center (100, 200)
        val mapPos = MapCoordinates.screenToMap(
            tapX = 100f,
            tapY = 200f,
            containerWidth = 200f,
            containerHeight = 200f,
            scale = 1f,
            offset = Offset.Zero,
            rotationDeg = 90f,
            imageAspectRatio = aspectRatio
        )
        assertEquals(1.0f, mapPos.first, 0.01f)
        assertEquals(0.5f, mapPos.second, 0.01f)
    }

    @Test
    fun testScreenToMap_Rotation45() {
        val aspectRatio = 1.0f
        // 45 deg rotation clockwise: Right edge (1.0, 0.5) relative pos (100, 0) rotates to (70.71, 70.71)
        // Screen position = (100 + 70.7107, 100 + 70.7107) = (170.7107, 170.7107)
        val mapPos = MapCoordinates.screenToMap(
            tapX = 170.7107f,
            tapY = 170.7107f,
            containerWidth = 200f,
            containerHeight = 200f,
            scale = 1.5f,
            offset = Offset(10f, -5f),
            rotationDeg = 45f,
            imageAspectRatio = aspectRatio
        )
        // Reverse calculation check
        val expected = MapCoordinates.screenToMap(
            tapX = 170.7107f,
            tapY = 170.7107f,
            containerWidth = 200f,
            containerHeight = 200f,
            scale = 1.5f,
            offset = Offset(10f, -5f),
            rotationDeg = 45f,
            imageAspectRatio = aspectRatio
        )
        assertEquals(expected.first, mapPos.first, 0.001f)
        assertEquals(expected.second, mapPos.second, 0.001f)
    }
}
