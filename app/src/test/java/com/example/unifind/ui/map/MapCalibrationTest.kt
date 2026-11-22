package com.example.unifind.ui.map

import com.example.unifind.data.CampusRepository
import com.example.unifind.model.Gate
import com.example.unifind.model.Location
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * OOP Concept: Testing / Encapsulation
 * Unit test validating map pin calibration logic: move with clamping, reset one, reset all, and JSON export.
 */
class MapCalibrationTest {

    private lateinit var gate1: Location
    private lateinit var gate2: Location
    private lateinit var repository: CampusRepository

    @Before
    fun setUp() {
        gate1 = Gate("gate_1", "Gate 1", 0.1458f, 0.8138f)
        gate2 = Gate("gate_2", "Gate 2", 0.5647f, 0.8497f)
        repository = CampusRepository {
            """
            {
              "colleges": [],
              "gates": [
                { "id": "gate_1", "name": "Gate 1", "mapX": 0.1458, "mapY": 0.8138 },
                { "id": "gate_2", "name": "Gate 2", "mapX": 0.5647, "mapY": 0.8497 }
              ],
              "facilities": [],
              "buildings": []
            }
            """.trimIndent()
        }
    }

    @Test
    fun testMoveWithClamping() {
        val loc = repository.findById("gate_1")!!

        // Move to valid position
        loc.updateCoordinates(0.2500f, 0.5000f)
        assertEquals(0.2500f, loc.mapX, 0.0001f)
        assertEquals(0.5000f, loc.mapY, 0.0001f)

        // Move beyond [0, 1] bounds should clamp
        loc.updateCoordinates(-0.1f, 1.5f)
        assertEquals(0.0f, loc.mapX, 0.0001f)
        assertEquals(1.0f, loc.mapY, 0.0001f)
    }

    @Test
    fun testResetOnePin() {
        val loc = repository.findById("gate_1")!!
        val origX = loc.originalMapX
        val origY = loc.originalMapY

        loc.updateCoordinates(0.7500f, 0.3500f)
        assertEquals(0.7500f, loc.mapX, 0.0001f)

        loc.resetCoordinates()
        assertEquals(origX, loc.mapX, 0.0001f)
        assertEquals(origY, loc.mapY, 0.0001f)
    }

    @Test
    fun testResetAllPins() {
        val loc1 = repository.findById("gate_1")!!
        val loc2 = repository.findById("gate_2")!!

        loc1.updateCoordinates(0.99f, 0.99f)
        loc2.updateCoordinates(0.01f, 0.01f)

        repository.getAllLocations().forEach { it.resetCoordinates() }

        assertEquals(loc1.originalMapX, loc1.mapX, 0.0001f)
        assertEquals(loc1.originalMapY, loc1.mapY, 0.0001f)
        assertEquals(loc2.originalMapX, loc2.mapX, 0.0001f)
        assertEquals(loc2.originalMapY, loc2.mapY, 0.0001f)
    }

    @Test
    fun testJsonExport() {
        val loc1 = repository.findById("gate_1")!!
        loc1.updateCoordinates(0.12345f, 0.67891f)

        val items = repository.getAllLocations().joinToString(",\n  ") { loc ->
            val xStr = String.format(Locale.US, "%.4f", loc.mapX)
            val yStr = String.format(Locale.US, "%.4f", loc.mapY)
            "{\n    \"id\": \"${loc.id}\",\n    \"mapX\": $xStr,\n    \"mapY\": $yStr\n  }"
        }
        val json = "[\n  $items\n]"

        assertTrue(json.contains("\"id\": \"gate_1\""))
        assertTrue(json.contains("\"mapX\": 0.1235"))
        assertTrue(json.contains("\"mapY\": 0.6789"))
    }
}
