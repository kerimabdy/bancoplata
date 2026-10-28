package tech.bashga.plata.designsystem.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiveChartGeometryTest {
    @Test
    fun aFlatSeriesStillHasAValueSpan() {
        val flat = List(5) { LiveChartPoint(it * 1_000L, 327f) }
        val bounds = valueBounds(flat)
        assertTrue(bounds.endInclusive > bounds.start, "$bounds")
        assertTrue(327f in bounds)
    }

    @Test
    fun scrubbingPastEitherEdgeClampsToTheEndPoints() {
        assertEquals(0, scrubIndex(-40f, 300f, 61))
        assertEquals(60, scrubIndex(340f, 300f, 61))
        assertEquals(30, scrubIndex(150f, 300f, 61))
        assertEquals(0, scrubIndex(10f, 300f, 1))
    }
}
