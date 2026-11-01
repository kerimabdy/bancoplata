package tech.bashga.plata.screens.invest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_lesson_understanding_chart
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.LessonCard
import tech.bashga.plata.designsystem.components.LiveChart
import tech.bashga.plata.designsystem.components.LiveChartAxis
import tech.bashga.plata.designsystem.components.LiveChartData
import tech.bashga.plata.designsystem.components.LiveChartHaptics
import tech.bashga.plata.designsystem.components.LiveChartMotion
import tech.bashga.plata.designsystem.components.LiveChartPoint
import tech.bashga.plata.designsystem.components.LiveChartStyle
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.SegmentedControl
import tech.bashga.plata.designsystem.components.SegmentedControlStyle
import tech.bashga.plata.designsystem.components.TrendDirection
import tech.bashga.plata.designsystem.components.TrendLabel
import tech.bashga.plata.designsystem.components.formatClockTime
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.time.Clock
import kotlin.time.Instant

enum class ChartRange(val label: String, val caption: String) {
    Hour("H", "Past hour"),
    Day("D", "Today"),
    Week("W", "Past week"),
    Month("M", "Past month"),
    Year("Y", "Past year"),
}

/** One range's chart: [points] are prices in USD cents oldest first, sampled at [times] epoch ms. */
@Immutable
data class ChartData(
    val points: List<Double>,
    val times: List<Long>,
    val high: Double,
    val low: Double,
    val change: Long,
    val changePercent: Int,
    val axis: LiveChartAxis,
)

/** The 1h simulated tick history, reproducible from [seed]. */
@Immutable
data class LiveFeed(val points: List<Double>, val times: List<Long>, val seed: Long)

/** The live feed's default tick interval. */
const val FeedTickMillis = 10_000L
private const val FeedWindowMillis = 3_600_000L
private const val FeedStep = 0.0005
private const val ClockStepMillis = 900_000L

/** A feed for [ticker] prefilled with the hour of ticks ending at [nowMillis]. */
fun liveFeed(ticker: String, quote: Quote, nowMillis: Long): LiveFeed {
    var seed = ticker.hashCode().toLong() and 0x7fffffff
    val count = (FeedWindowMillis / FeedTickMillis).toInt() + 1
    val walk = MutableList(count) { quote.price.toDouble() }
    for (i in count - 2 downTo 0) {
        seed = nextSeed(seed)
        walk[i] = walk[i + 1] * (1 - (seed / 2_147_483_647.0 - 0.5) * 2 * FeedStep)
    }
    val times = List(count) { nowMillis - (count - 1 - it) * FeedTickMillis }
    return LiveFeed(walk, times, seed)
}

private fun nextSeed(seed: Long) = (seed * 1_103_515_245 + 12_345) and 0x7fffffff

/** This feed with one tick at [nowMillis] appended and ticks older than the window dropped. */
fun LiveFeed.tick(nowMillis: Long): LiveFeed {
    val next = nextSeed(seed)
    val value = (points.last() * (1 + (next / 2_147_483_647.0 - 0.5) * 2 * FeedStep)).coerceAtLeast(1.0)
    val keep = times.indexOfFirst { it >= nowMillis - FeedWindowMillis }.let { if (it < 0) times.size else it }
    return copy(points = points.drop(keep) + value, times = times.drop(keep) + nowMillis, seed = next)
}

/** The 1h chart of this feed, change measured from its first point (the price an hour ago). */
fun LiveFeed.chart(): ChartData {
    val base = points.first().roundToLong().coerceAtLeast(1)
    val change = points.last().roundToLong() - base
    return ChartData(points, times, points.max(), points.min(), change, (change * 10_000 / base).toInt(), LiveChartAxis.Clock(ClockStepMillis))
}

/** This chart as the design-system chart's input; [live] scrolls a clock window. */
fun ChartData.toLiveChartData(live: Boolean): LiveChartData =
    LiveChartData(points.zip(times) { p, t -> LiveChartPoint(t, p.toFloat()) }, live, if (live) FeedWindowMillis else null, axis)

private val rangeAxes = mapOf(
    ChartRange.Week to listOf("Mon", "Tue", "Wed", "Thu"),
    ChartRange.Month to listOf("W1", "W2", "W3", "W4"),
    ChartRange.Year to listOf("Q1", "Q2", "Q3", "Q4"),
)

/** The range's chart for [ticker]: a seeded walk ending at [quote]'s price and [nowMillis]; Day starts at the day-open price. */
fun chartData(ticker: String, quote: Quote, range: ChartRange, nowMillis: Long): ChartData {
    val price = quote.price
    val span = when (range) {
        ChartRange.Hour -> FeedWindowMillis
        ChartRange.Day -> DayMillis
        ChartRange.Week -> 7 * DayMillis
        ChartRange.Month -> 30 * DayMillis
        ChartRange.Year -> 365 * DayMillis
    }
    val axis = when (range) {
        ChartRange.Hour -> LiveChartAxis.Clock(ClockStepMillis)
        ChartRange.Day -> LiveChartAxis.Fixed(listOf(1, 3, 5, 7).map { formatClockTime(nowMillis - span + it * span / 8) })
        else -> LiveChartAxis.Fixed(rangeAxes.getValue(range))
    }
    var seed = (ticker.hashCode() * 31 + range.ordinal).toLong() and 0x7fffffff
    fun next(): Double {
        seed = (seed * 1_103_515_245 + 12_345) and 0x7fffffff
        return seed / 2_147_483_647.0
    }
    // Day's smaller per-step swing keeps its 289-step walk about as rough as the 60-step ranges.
    val swing = when (range) {
        ChartRange.Hour -> 0.005
        ChartRange.Day -> 0.005
        ChartRange.Week -> 0.02
        ChartRange.Month -> 0.04
        ChartRange.Year -> 0.08
    }
    val count = if (range == ChartRange.Day) (DayMillis / DaySampleMillis).toInt() + 1 else 60
    val walk = MutableList(count) { 0.0 }
    for (i in count - 2 downTo 0) walk[i] = walk[i + 1] + (next() - 0.48) * swing
    if (range == ChartRange.Day) {
        val openOffset = 1 - 10_000.0 / (10_000 + quote.change)
        val drift = openOffset - walk[0]
        for (i in walk.indices) walk[i] += drift * (count - 1 - i) / (count - 1)
    }
    val points = walk.map { (price * (1 - it)).roundToLong().coerceAtLeast(1).toDouble() }
    val first = points.first()
    val change = price - first.toLong()
    val times = points.indices.map { nowMillis - span + it * span / (points.size - 1) }
    return ChartData(points, times, points.max(), points.min(), change, (change * 10_000 / first.toLong()).toInt(), axis)
}

private const val DayMillis = 86_400_000L
private const val DaySampleMillis = 300_000L

/** How long a range change's simulated load lasts. */
const val RangeLoadMillis = 500L

/** [epochMillis] labelled for this range: 1h/1d HH:mm, 1w EEE HH:mm, 1m d MMM, 1y d MMM yyyy. */
fun ChartRange.scrubLabel(epochMillis: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val time = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone)
    val date = "${time.day} ${MonthNames[time.month.ordinal]}"
    return when (this) {
        ChartRange.Hour, ChartRange.Day -> formatClockTime(epochMillis, timeZone)
        ChartRange.Week -> "${WeekdayNames[time.dayOfWeek.ordinal]} ${formatClockTime(epochMillis, timeZone)}"
        ChartRange.Month -> date
        ChartRange.Year -> "$date ${time.year}"
    }
}

private val MonthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
private val WeekdayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Immutable
data class StockDetailState(
    val asset: Asset,
    val quote: Quote,
    val range: ChartRange = ChartRange.Hour,
    val shownRange: ChartRange = ChartRange.Hour,
    val lessonDismissed: Boolean = false,
    val feed: LiveFeed? = null,
    val scrub: Int? = null,
    val frozen: ChartData? = null,
    val loading: Boolean = false,
    val nowMillis: Long = 0L,
) {
    val chart: ChartData
        get() = frozen ?: feed?.takeIf { shownRange == ChartRange.Hour }?.chart() ?: chartData(asset.ticker, quote, shownRange, nowMillis)

    /** The price in cents of the scrubbed point, or the latest. */
    val displayPrice: Long get() = chart.points.let { p -> scrub?.let { p.getOrNull(it) } ?: p.last() }.roundToLong()

    /** The change in cents up to the scrubbed point, or across the range. */
    val displayChange: Long get() = displayPrice - changeBase

    private val changeBase: Long get() = chart.points.first().roundToLong().coerceAtLeast(1)

    /** [displayChange] in basis points. */
    val displayChangePercent: Int get() = (displayChange * 10_000 / changeBase).toInt()

    /** The scrubbed point's time, or the range caption. */
    val displayCaption: String get() = scrub?.let { chart.times.getOrNull(it) }?.let { shownRange.scrubLabel(it) } ?: shownRange.caption

    companion object {
        fun of(ticker: String, month: Month) = StockDetailState(InvestCatalog.asset(ticker), InvestCatalog.quote(ticker, month))
    }
}

sealed interface StockDetailAction {
    data class SelectRange(val range: ChartRange) : StockDetailAction
    data object DismissLesson : StockDetailAction
    data object OpenLesson : StockDetailAction
    data object Buy : StockDetailAction
    data object Back : StockDetailAction
    data class ScrubAt(val index: Int) : StockDetailAction
    data object EndScrub : StockDetailAction
}

/** The state after [action]; navigation actions leave it unchanged. */
fun StockDetailState.reduce(action: StockDetailAction): StockDetailState = when (action) {
    is StockDetailAction.SelectRange -> copy(range = action.range, scrub = null, frozen = null, loading = true)
    StockDetailAction.DismissLesson -> copy(lessonDismissed = true)
    is StockDetailAction.ScrubAt -> copy(scrub = action.index, frozen = frozen ?: chart)
    StockDetailAction.EndScrub -> copy(scrub = null, frozen = null)
    StockDetailAction.OpenLesson, StockDetailAction.Buy, StockDetailAction.Back -> this
}


class StockDetailViewModel(
    repository: AppRepository,
    private val ticker: String,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {
    private val local = MutableStateFlow(StockDetailState.of(ticker, repository.snapshot.month))
    private var load: Job? = null
    private var ticking = true
    private var rangeLoadMillis = RangeLoadMillis
    private var feedTickMillis = FeedTickMillis

    init {
        startFeed()
        repository.addRecent(ticker)
    }

    val state: StateFlow<StockDetailState> = combine(repository.state, local) { snapshot, l ->
        StockDetailState.of(ticker, snapshot.month).let { l.copy(asset = it.asset, quote = it.quote) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, StockDetailState.of(ticker, repository.snapshot.month))

    fun onAction(action: StockDetailAction) {
        val current = state.value
        local.update { it.copy(asset = current.asset, quote = current.quote).reduce(action) }
        if (action is StockDetailAction.SelectRange) loadRange(action.range, rangeLoadMillis)
    }

    private fun startFeed() {
        val start = now()
        local.update { it.copy(feed = liveFeed(ticker, it.quote, start), nowMillis = start) }
        viewModelScope.launch {
            while (true) {
                delay(feedTickMillis)
                if (!ticking) continue
                val at = now()
                local.update { it.copy(feed = it.feed?.tick(at), nowMillis = at) }
            }
        }
    }

    /** Debug inputs from the Live chart dials: whether the feed ticks, how often, and how long a range load lasts. */
    fun tune(liveFeed: Boolean, rangeLoadMillis: Long, feedTickMillis: Long) {
        ticking = liveFeed
        this.rangeLoadMillis = rangeLoadMillis
        this.feedTickMillis = feedTickMillis
    }

    private fun loadRange(range: ChartRange, delayMillis: Long) {
        load?.cancel()
        load = viewModelScope.launch {
            delay(delayMillis)
            local.update { if (it.range == range) it.copy(loading = false, shownRange = range) else it }
        }
    }
}

/** invest/stock/{ticker}: opening it pushes [ticker] to the recents. */
@Composable
fun StockDetailScreen(ticker: String, onLesson: () -> Unit, onBack: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel(key = "stock-$ticker") { StockDetailViewModel(repository, ticker) }
    val state by viewModel.state.collectAsState()
    val debug = LiveChartDials.debug
    val motion = LiveChartDials.motion
    val style = LiveChartDials.style(PlataTheme.colors)
    val rangeLoadMillis = LiveChartDials.rangeLoadMillis
    val feedTickMillis = LiveChartDials.feedTickMillis
    LaunchedEffect(debug.liveFeed, rangeLoadMillis, feedTickMillis) { viewModel.tune(debug.liveFeed, rangeLoadMillis, feedTickMillis) }
    DisposableEffect(viewModel) {
        LiveChartDials.onSimulateRangeLoad = { viewModel.onAction(StockDetailAction.SelectRange(viewModel.state.value.range)) }
        onDispose { LiveChartDials.onSimulateRangeLoad = {} }
    }
    StockDetailContent(state, style = style, debug = debug, motion = motion, haptics = LiveChartDials.haptics, onAction = { action ->
        when (action) {
            StockDetailAction.OpenLesson -> onLesson()
            StockDetailAction.Back -> onBack()
            else -> viewModel.onAction(action)
        }
    })
}

// 341fcc04 measurements: off-grid gaps
private val ChartPlotHeight = 180.dp
private val TrendGap = 2.dp
private val ChartTopGap = 22.dp
private val LessonGap = 36.dp

@Composable
fun StockDetailContent(
    state: StockDetailState,
    onAction: (StockDetailAction) -> Unit,
    modifier: Modifier = Modifier,
    style: LiveChartStyle = LiveChartStyle(),
    debug: LiveChartDebug = LiveChartDebug(),
    motion: LiveChartMotion = LiveChartMotion(),
    haptics: LiveChartHaptics = LiveChartHaptics(),
) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val chart = state.chart
    val direction = if (state.displayChange < 0) TrendDirection.Down else TrendDirection.Up
    Box(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            NavigationBar(onBack = { onAction(StockDetailAction.Back) }, includeStatusBar = true)
            Column(Modifier.padding(horizontal = spacing.screenMargin)) {
                Spacer(Modifier.height(spacing.s20))
                AssetLogo(state.asset, BrandLogoSize.S56)
                Spacer(Modifier.height(spacing.s20))
                HeadlineBlock("${state.asset.name}\n${usd(state.displayPrice)}")
                Spacer(Modifier.height(TrendGap))
                TrendLabel(
                    "${usd(abs(state.displayChange))} · ${formatPercent(state.displayChangePercent)}",
                    direction,
                    caption = "· ${state.displayCaption}",
                    captionColor = colors.textPrimary,
                )
                Spacer(Modifier.height(ChartTopGap))
            }
            LiveChart(
                chart.toLiveChartData(state.shownRange == ChartRange.Hour).let { if (debug.empty) it.copy(points = emptyList()) else it },
                Modifier.fillMaxWidth(),
                loading = state.loading || debug.forceLoading,
                paused = state.scrub != null,
                formatValue = { usd(it.roundToLong()) },
                onScrub = { onAction(it?.let { s -> StockDetailAction.ScrubAt(s.index) } ?: StockDetailAction.EndScrub) },
                style = style,
                plotHeight = ChartPlotHeight,
                reduceMotion = debug.reduceMotion,
                motion = motion,
                haptics = haptics,
            )
            Spacer(Modifier.height(spacing.s16))
            SegmentedControl(
                ChartRange.entries.map { it.label },
                state.range.ordinal,
                { onAction(StockDetailAction.SelectRange(ChartRange.entries[it])) },
                Modifier.fillMaxWidth().padding(horizontal = spacing.screenMargin),
                equalWidths = true,
            )
            if (!state.lessonDismissed) {
                Spacer(Modifier.height(LessonGap))
                LessonCard(
                    "Understanding\nthe chart",
                    "2-minute read",
                    Modifier.fillMaxWidth().padding(horizontal = spacing.screenMargin),
                    onClose = { onAction(StockDetailAction.DismissLesson) },
                    onClick = { onAction(StockDetailAction.OpenLesson) },
                    thumbnail = {
                        Image(painterResource(Res.drawable.raster_lesson_understanding_chart), null, Modifier.size(64.dp, 80.dp))
                    },
                )
            }
            Spacer(Modifier.height(PlataTheme.sizes.buttonLarge + spacing.s40 + PlataSafeArea.current.bottom))
        }
        val hairline = colors.borderSubtle
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(colors.surfaceTabbar)
                .drawBehind { drawLine(hairline, Offset(0f, 0.5f), Offset(size.width, 0.5f), strokeWidth = 1f) }
                .padding(start = spacing.screenMargin, end = spacing.screenMargin, top = spacing.s20)
                .padding(bottom = spacing.s20 + PlataSafeArea.current.bottom),
            verticalArrangement = Arrangement.Top,
        ) {
            Button("Buy from USD 1", { onAction(StockDetailAction.Buy) }, Modifier.fillMaxWidth())
        }
    }
}
