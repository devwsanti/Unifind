package com.example.unifind.ui.directions

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
}