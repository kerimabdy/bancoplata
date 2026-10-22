package tech.bashga.plata.screens.invest

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_chart_krispy_kreme
import plata.shared.generated.resources.raster_lesson_understanding_chart
import tech.bashga.plata.data.AppRepository
import tech.bashga.plata.data.LocalAppRepository
import tech.bashga.plata.data.Month
import tech.bashga.plata.designsystem.components.BrandLogoSize
import tech.bashga.plata.designsystem.components.Button
import tech.bashga.plata.designsystem.components.HeadlineBlock
import tech.bashga.plata.designsystem.components.LessonCard
import tech.bashga.plata.designsystem.components.NavigationBar
import tech.bashga.plata.designsystem.components.SegmentedControl
import tech.bashga.plata.designsystem.components.SegmentedControlStyle
import tech.bashga.plata.designsystem.components.TrendDirection
import tech.bashga.plata.designsystem.components.TrendLabel
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme
import kotlin.math.abs
import kotlin.math.roundToLong

enum class ChartRange(val label: String, val caption: String) {
    Day("1d", "Today"),
    Week("1w", "Past week"),
    Month("1m", "Past month"),
    Year("1y", "Past year"),
    All("All time", "All time"),
}

/** One range's chart: [points] are prices in USD cents oldest first; null draws the captured DNUT day raster. */
@Immutable
data class ChartData(
    val points: List<Long>?,
    val high: Long,
    val low: Long,
    val change: Long,
    val changePercent: Int,
    val axis: List<String>,
)

private val rangeAxes = mapOf(
    ChartRange.Day to listOf("12:30", "15:55", "03:40", "08:40"),
    ChartRange.Week to listOf("Mon", "Tue", "Wed", "Thu"),
    ChartRange.Month to listOf("W1", "W2", "W3", "W4"),
    ChartRange.Year to listOf("Q1", "Q2", "Q3", "Q4"),
    ChartRange.All to listOf("2021", "2022", "2023", "2024"),
)

/** The range's chart for [ticker] ending at [price]: DNUT's day is the captured one (341fcc04), the rest a seeded walk. */
fun chartData(ticker: String, price: Long, quoteChange: Int, range: ChartRange): ChartData {
    val axis = rangeAxes.getValue(range)
    if (ticker == "DNUT" && range == ChartRange.Day && price == 3_27L) {
        return ChartData(null, high = 3_51, low = 3_26, change = -18, changePercent = quoteChange, axis = axis)
    }
    var seed = (ticker.hashCode() * 31 + range.ordinal).toLong() and 0x7fffffff
    fun next(): Double {
        seed = (seed * 1_103_515_245 + 12_345) and 0x7fffffff
        return seed / 2_147_483_647.0
    }
    val swing = when (range) {
        ChartRange.Day -> 0.01
        ChartRange.Week -> 0.02
        ChartRange.Month -> 0.04
        ChartRange.Year -> 0.08
        ChartRange.All -> 0.15
    }
    val walk = MutableList(60) { 0.0 }
    for (i in 58 downTo 0) walk[i] = walk[i + 1] + (next() - 0.48) * swing
    val points = walk.map { (price * (1 - it)).roundToLong().coerceAtLeast(1) }
    val first = points.first()
    val change = price - first
    return ChartData(points, points.max(), points.min(), change, (change * 10_000 / first).toInt(), axis)
}

@Immutable
data class StockDetailState(
    val asset: Asset,
    val quote: Quote,
    val range: ChartRange = ChartRange.Day,
    val lessonDismissed: Boolean = false,
) {
    val chart: ChartData get() = chartData(asset.ticker, quote.price, quote.change, range)

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
}

private data class StockDetailLocal(val range: ChartRange = ChartRange.Day, val lessonDismissed: Boolean = false)

class StockDetailViewModel(repository: AppRepository, private val ticker: String) : ViewModel() {
    private val local = MutableStateFlow(StockDetailLocal())

    init {
        repository.addRecent(ticker)
    }

    val state: StateFlow<StockDetailState> = combine(repository.state, local) { snapshot, l ->
        StockDetailState.of(ticker, snapshot.month).copy(range = l.range, lessonDismissed = l.lessonDismissed)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, StockDetailState.of(ticker, repository.snapshot.month))

    fun onAction(action: StockDetailAction) {
        local.value = when (action) {
            is StockDetailAction.SelectRange -> local.value.copy(range = action.range)
            StockDetailAction.DismissLesson -> local.value.copy(lessonDismissed = true)
            else -> local.value
        }
    }
}

/** invest/stock/{ticker}: opening it pushes [ticker] to the recents. */
@Composable
fun StockDetailScreen(ticker: String, onLesson: () -> Unit, onBack: () -> Unit) {
    val repository = LocalAppRepository.current
    val viewModel = viewModel(key = "stock-$ticker") { StockDetailViewModel(repository, ticker) }
    val state by viewModel.state.collectAsState()
    StockDetailContent(state, onAction = { action ->
        when (action) {
            StockDetailAction.OpenLesson -> onLesson()
            StockDetailAction.Back -> onBack()
            else -> viewModel.onAction(action)
        }
    })
}

// 341fcc04 measurements: chart band 144 high, time axis 4 equal cells 18 in from the edges, off-grid gaps
private val ChartHeight = 144.dp
private val AxisInset = 18.dp
private val TrendGap = 2.dp
private val ChartTopGap = 22.dp
private val LessonGap = 36.dp
private val ChartLine = 2.dp

@Composable
fun StockDetailContent(state: StockDetailState, onAction: (StockDetailAction) -> Unit, modifier: Modifier = Modifier) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val typography = PlataTheme.typography
    val chart = state.chart
    val direction = if (chart.change < 0) TrendDirection.Down else TrendDirection.Up
    Box(modifier.fillMaxSize().background(colors.surfaceBackground)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            NavigationBar(onBack = { onAction(StockDetailAction.Back) }, includeStatusBar = true)
            Column(Modifier.padding(horizontal = spacing.screenMargin)) {
                Spacer(Modifier.height(spacing.s20))
                AssetLogo(state.asset, BrandLogoSize.S56)
                Spacer(Modifier.height(spacing.s20))
                HeadlineBlock("${state.asset.name}\n${usd(state.quote.price)}")
                Spacer(Modifier.height(TrendGap))
                TrendLabel(
                    "${usd(abs(chart.change))} · ${formatPercent(chart.changePercent)}",
                    direction,
                    caption = "· ${state.range.caption}",
                    captionColor = colors.textPrimary,
                )
                Spacer(Modifier.height(ChartTopGap))
                PlataText(usd(chart.high), typography.subhead, colors.textSecondary, Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            }
            val points = chart.points
            if (points == null) {
                Image(
                    painterResource(Res.drawable.raster_chart_krispy_kreme),
                    "Price chart",
                    Modifier.fillMaxWidth().height(ChartHeight),
                    contentScale = ContentScale.FillBounds,
                )
            } else {
                val ink = colors.accent
                Canvas(Modifier.fillMaxWidth().height(ChartHeight)) {
                    val span = (chart.high - chart.low).coerceAtLeast(1).toFloat()
                    val step = size.width / (points.size - 1)
                    val path = Path()
                    points.forEachIndexed { i, p ->
                        val at = Offset(i * step, size.height * (1 - (p - chart.low) / span))
                        if (i == 0) path.moveTo(at.x, at.y) else path.lineTo(at.x, at.y)
                    }
                    drawPath(path, ink, style = Stroke(ChartLine.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
            Column(Modifier.padding(horizontal = spacing.screenMargin)) {
                PlataText(usd(chart.low), typography.subhead, colors.textSecondary, Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            }
            Spacer(Modifier.height(spacing.s12))
            Row(Modifier.fillMaxWidth().padding(horizontal = AxisInset)) {
                chart.axis.forEach {
                    PlataText(it, typography.subhead, colors.textSecondary, Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(spacing.s16))
            SegmentedControl(
                ChartRange.entries.map { it.label },
                state.range.ordinal,
                { onAction(StockDetailAction.SelectRange(ChartRange.entries[it])) },
                Modifier.align(Alignment.CenterHorizontally),
                style = SegmentedControlStyle.Plain,
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
