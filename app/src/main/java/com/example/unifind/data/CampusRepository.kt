package com.example.unifind.data

import android.content.Context
import com.example.unifind.data.dto.BuildingDto
import com.example.unifind.data.dto.CampusDto
import com.example.unifind.data.dto.FacilityDto
import com.example.unifind.data.dto.FloorDto
import com.example.unifind.data.dto.GateDto
import com.example.unifind.data.dto.RoomDto
import com.example.unifind.model.Building
import com.example.unifind.model.BuildingStatus
import com.example.unifind.model.Facility
import com.example.unifind.model.Floor
import com.example.unifind.model.Gate
import com.example.unifind.model.Location
import com.example.unifind.model.Room
import kotlinx.serialization.json.Json

/**
 * OOP Concept: Repository Pattern / Singleton / Dependency Injection
 * Encapsulates the logic for loading and mapping data from JSON.
 */
class CampusRepository(private val jsonSupplier: () -> String) {

    constructor(context: Context) : this({
        context.assets.open("campus.json").bufferedReader().use { it.readText() }
    })

    private val json = Json { ignoreUnknownKeys = true }

    private val campusData: CampusDto by lazy {
        json.decodeFromString<CampusDto>(jsonSupplier())
    }

    private val locationsList: List<Location> by lazy {
        val gates = campusData.gates.map { it.toModel() }
        val facilities = campusData.facilities.map { it.toModel() }
        val buildings = campusData.buildings.map { it.toModel() }
        gates + facilities + buildings
    }

    fun getAllLocations(): List<Location> = locationsList

    fun getBuildings(): List<Building> = locationsList.filterIsInstance<Building>()

    fun getGates(): List<Gate> = locationsList.filterIsInstance<Gate>()

    fun getFacilities(): List<Facility> = locationsList.filterIsInstance<Facility>()

    fun getColleges(): List<String> = campusData.colleges

    fun getBuildingsForCollege(collegeName: String): List<Building> {
        val norm = collegeName.replace(Regex("[ .-]"), "").lowercase()
        return getBuildings().filter { b ->
            val bCollegeNorm = b.college.replace(Regex("[ .-]"), "").lowercase()
            if (bCollegeNorm == norm) return@filter true
            b.floors.any { f ->
                f.rooms.any { r ->
                    r.ownerCollege?.replace(Regex("[ .-]"), "")?.lowercase() == norm ||
                    r.sharedWith.any { it.replace(Regex("[ .-]"), "").lowercase() == norm }
                }
            }
        }
    }

    fun findById(id: String): Location? = locationsList.find { it.id == id }

    // Mapper extension functions

    private fun GateDto.toModel(): Gate = Gate(
        id = id,
        name = name,
        mapX = mapX,
        mapY = mapY,
        latitude = latitude,
        longitude = longitude
    )

    private fun FacilityDto.toModel(): Facility = Facility(
        id = id,
        name = name,
        mapX = mapX,
        mapY = mapY,
        category = category,
        latitude = latitude,
        longitude = longitude
    )

    private fun BuildingDto.toModel(): Building = Building(
        id = id,
        name = name,
        mapX = mapX,
        mapY = mapY,
        college = college,
        status = runCatching { BuildingStatus.valueOf(status.uppercase()) }.getOrDefault(BuildingStatus.ACTIVE),
        floors = floors.map { it.toModel() },
        latitude = latitude,
        longitude = longitude
    )

    private fun FloorDto.toModel(): Floor = Floor(
        label = label,
        rooms = rooms.map { it.toModel() }
    )

    private fun RoomDto.toModel(): Room = Room(
        id = id,
        name = name,
        floorLabel = floorLabel,
        ownerCollege = ownerCollege,
        sharedWith = sharedWith
    )
}
