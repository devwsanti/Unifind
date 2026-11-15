package com.example.unifind.ui.map

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * OOP Concept: Testing
 * Pure offline unit test validating aspect-ratio math mappings.
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
}
