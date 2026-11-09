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

        // 1. buildings.size == 9
        val buildings = repository.getBuildings()
        assertEquals(9, buildings.size)

        // 2. Sto. Tomas building has status UNDER_CONSTRUCTION
        val stoTomas = buildings.find { it.id == "sto_tomas" }
        assertTrue("Sto. Tomas building should exist", stoTomas != null)
        assertEquals(BuildingStatus.UNDER_CONSTRUCTION, stoTomas?.status)

        // 3. findById("admin") contains the room "Cashier"
        val admin = repository.findById("admin") as? Building
        assertTrue("Admin building should exist", admin != null)
        val hasCashier = admin?.floors?.any { floor ->
            floor.rooms.any { room -> room.name == "Cashier" }
        } ?: false
        assertTrue("Admin building should contain Cashier room", hasCashier)
    }
}
