package com.example.unifind.ui.directions

import com.example.unifind.model.Room
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteSummaryFormatterTest {
    @Test
    fun testFormatSummary() {
        assertEquals("Approx. 180 m - about 3 min walk", RouteSummaryFormatter.formatSummary(182.0))
        assertEquals("Approx. 0 m - about 1 min walk", RouteSummaryFormatter.formatSummary(0.0))
        assertEquals("Approx. 5 m - about 1 min walk", RouteSummaryFormatter.formatSummary(4.0))
        assertEquals("Approx. 85 m - about 2 min walk", RouteSummaryFormatter.formatSummary(84.0))
    }

    @Test
    fun testFormatDistance() {
        assertEquals(180, RouteSummaryFormatter.formatDistance(182.0))
        assertEquals(0, RouteSummaryFormatter.formatDistance(0.0))
        assertEquals(5, RouteSummaryFormatter.formatDistance(4.0))
        assertEquals(85, RouteSummaryFormatter.formatDistance(84.0))
    }

    @Test
    fun testFormatStartText_withRoom() {
        val room = Room(
            id = "R1",
            name = "Borrowing & Returning",
            floorLabel = "2nd Floor",
            ownerCollege = "CTE",
            sharedWith = emptyList()
        )
        val text = RouteSummaryFormatter.formatStartText("Higher Education Building", room)
        assertEquals("Start at Higher Education Building (from 2nd Floor - Borrowing & Returning)", text)
    }

    @Test
    fun testFormatStartText_withoutRoom() {
        val text = RouteSummaryFormatter.formatStartText("Higher Education Building", null)
        assertEquals("Start at Higher Education Building", text)
    }

    @Test
    fun testFormatArrivalLines_withRoom() {
        val room = Room(
            id = "R1",
            name = "Borrowing & Returning",
            floorLabel = "2nd Floor",
            ownerCollege = "CTE",
            sharedWith = emptyList()
        )
        val lines = RouteSummaryFormatter.formatArrivalLines("Higher Education Building", room)
        assertEquals(2, lines.size)
        assertEquals("Arrive at Higher Education Building", lines[0])
        assertEquals("Go to 2nd Floor - Borrowing & Returning", lines[1])
    }

    @Test
    fun testFormatArrivalLines_withoutRoom() {
        val lines = RouteSummaryFormatter.formatArrivalLines("Higher Education Building", null)
        assertEquals(1, lines.size)
        assertEquals("Arrive at Higher Education Building", lines[0])
    }
}
