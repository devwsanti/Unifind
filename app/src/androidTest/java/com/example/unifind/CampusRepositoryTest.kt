package com.example.unifind

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.unifind.data.CampusRepository
import com.example.unifind.model.Building
import com.example.unifind.model.BuildingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * OOP Concept: Testing
 * Validates the CampusRepository.
 */
@RunWith(AndroidJUnit4::class)
class CampusRepositoryTest {

    @Test
    fun testCampusDataLoading() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = CampusRepository(context)

        // 1. buildings.size == 12
        val buildings = repository.getBuildings()
        assertEquals(12, buildings.size)

        // Gates.size == 4
        val gates = repository.getGates()
        assertEquals(4, gates.size)

        // Facilities.size == 4
        val facilities = repository.getFacilities()
        assertEquals(4, facilities.size)

        // 2. Sto. Tomas building has status UNDER_CONSTRUCTION
        val stoTomas = buildings.find { it.id == "sto_tomas" }
        assertTrue("Sto. Tomas building should exist", stoTomas != null)
        assertEquals(BuildingStatus.UNDER_CONSTRUCTION, stoTomas?.status)

        // 3. findById("ermita") contains the room "Cashier"
        val ermita = repository.findById("ermita") as? Building
        assertTrue("Ermita building should exist", ermita != null)
        val hasCashier = ermita?.floors?.any { floor ->
            floor.rooms.any { room -> room.name == "Cashier" }
        } ?: false
        assertTrue("Ermita building should contain Cashier room", hasCashier)

        // Colleges size == 7
        val colleges = repository.getColleges()
        assertEquals(7, colleges.size)
    }
}
