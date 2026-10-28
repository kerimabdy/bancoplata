package tech.bashga.plata.screens.invest

import kotlinx.datetime.TimeZone
import tech.bashga.plata.data.Month
import tech.bashga.plata.designsystem.components.LiveChartAxis
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StockDetailChartTest {
    private val now = 1_717_000_000_000L
    private val hourMillis = 3_600_000L
    private val clock = Regex("""\d\d:\d\d""")

    private fun dnut() = StockDetailState.of("DNUT", Month.June).let { it.copy(feed = liveFeed("DNUT", it.quote, now), nowMillis = now) }

    @Test
    fun theHourFeedHoldsOneHourOfTicksEndingAtNow() {
        val quote = dnut().quote
        val feed = liveFeed("DNUT", quote, now)
        assertEquals(361, feed.points.size)
        assertEquals(now, feed.times.last())
        assertEquals(now - hourMillis, feed.times.first())
        assertTrue(feed.times.zipWithNext().all { (a, b) -> b - a == FeedTickMillis })
        assertEquals(quote.price.toDouble(), feed.points.last())

        val later = (1..720).fold(feed) { f, i -> f.tick(now + i * FeedTickMillis) }
        val end = now + 720 * FeedTickMillis
        assertEquals(end, later.times.last())
        assertTrue(later.times.first() >= end - hourMillis, "ticks older than the window are dropped")
        assertEquals(later.points.size, later.times.size)
    }

    @Test
    fun scrubbingDrivesTheHeaderFromAFrozenChartAndReleaseRestoresIt() {
        val live = dnut()
        assertEquals(ChartRange.Hour, live.range)
        assertEquals(live.quote.price, live.displayPrice)
        assertEquals(live.quote.price - live.feed!!.points.first().roundToLong(), live.displayChange)
        assertEquals(ChartRange.Hour.caption, live.displayCaption)

        val atStart = live.reduce(StockDetailAction.ScrubAt(0))
        assertEquals(0L, atStart.displayChange)
        assertEquals(0, atStart.displayChangePercent)
        assertTrue(clock.matches(atStart.displayCaption), atStart.displayCaption)

        val last = live.feed!!.points.lastIndex
        val ticked = live.reduce(StockDetailAction.ScrubAt(last)).let { it.copy(feed = it.feed!!.tick(now + FeedTickMillis)) }
        assertEquals(live.quote.price, ticked.displayPrice, "a tick while scrubbing must not move the scrubbed point")

        val released = ticked.reduce(StockDetailAction.EndScrub)
        assertEquals(ChartRange.Hour.caption, released.displayCaption)
        assertEquals(released.feed!!.points.last().roundToLong(), released.displayPrice)
    }

    @Test
    fun selectingARangeHoldsTheShownRangeUntilItsLoadCompletes() {
        val scrubbing = dnut().reduce(StockDetailAction.ScrubAt(3))
        val selected = scrubbing.reduce(StockDetailAction.SelectRange(ChartRange.Day))
        assertEquals(ChartRange.Day, selected.range)
        assertEquals(ChartRange.Hour, selected.shownRange)
        assertTrue(selected.loading)
        assertEquals(ChartRange.Hour.caption, selected.displayCaption)
        assertEquals(selected.quote.price - selected.feed!!.points.first().roundToLong(), selected.displayChange)
    }

    @Test
    fun theDayChartWalksTwentyFourHoursFromTheDayOpenToThePrice() {
        val quote = dnut().quote
        val day = chartData("DNUT", quote, ChartRange.Day, now)
        assertEquals(289, day.points.size)
        assertEquals(now, day.times.last())
        assertTrue(day.times.zipWithNext().all { (a, b) -> b - a == 300_000L })
        assertEquals(quote.price.toDouble(), day.points.last())
        val open = quote.price * 10_000.0 / (10_000 + quote.change)
        assertTrue(abs(day.points.first() - open) <= 1.0, "day open ${day.points.first()} vs ${open} from the quote's change")
        val labels = assertIs<LiveChartAxis.Fixed>(day.axis).labels
        assertEquals(4, labels.size)
        assertTrue(labels.all(clock::matches), labels.toString())
    }

    @Test
    fun everyRangeChartsWithoutCrashingOnTheRightAxis() {
        for (range in ChartRange.entries) {
            val chart = StockDetailState.of("DNUT", Month.June).copy(range = range, shownRange = range, nowMillis = now).chart
            assertEquals(now, chart.times.last(), "$range")
            assertTrue(chart.times.zipWithNext().all { (a, b) -> b > a }, "$range times ascend")
            when (range) {
                ChartRange.Hour -> assertIs<LiveChartAxis.Clock>(chart.axis)
                else -> assertEquals(4, assertIs<LiveChartAxis.Fixed>(chart.axis, "$range").labels.size)
            }
        }
    }

    @Test
    fun scrubLabelsFollowTheRangeInTheGivenTimeZone() {
        val at = 1_709_647_620_000L
        val cases = listOf(
            ChartRange.Hour to "14:07",
            ChartRange.Day to "14:07",
            ChartRange.Week to "Tue 14:07",
            ChartRange.Month to "5 Mar",
            ChartRange.Year to "5 Mar 2024",
            ChartRange.All to "5 Mar 2024",
        )
        for ((range, label) in cases) assertEquals(label, range.scrubLabel(at, TimeZone.UTC), "$range")
        assertNotEquals(ChartRange.Hour.scrubLabel(at, TimeZone.UTC), ChartRange.Hour.scrubLabel(at, TimeZone.of("Asia/Tokyo")))
    }
}
