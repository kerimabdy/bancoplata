package tech.bashga.plata.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.navigation.rememberReduceMotion
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Instant

/** One chart sample at [timeMillis] epoch ms. */
@Immutable
data class LiveChartPoint(val timeMillis: Long, val value: Float)

/** How the chart's time axis is labelled. */
sealed interface LiveChartAxis {
    /** Static labels spread evenly under the chart. */
    @Immutable
    data class Fixed(val labels: List<String>) : LiveChartAxis

    /** Sliding HH:mm labels every [stepMillis] that move with a live window. */
    @Immutable
    data class Clock(val stepMillis: Long) : LiveChartAxis
}

/** What the chart draws: [windowMillis] null fits all [points], otherwise a scrolling window of that length. */
@Immutable
data class LiveChartData(
    val points: List<LiveChartPoint>,
    val live: Boolean,
    val windowMillis: Long?,
    val axis: LiveChartAxis,
)

/** Stroke, crosshair, point, fill and dimming values; [crosshairColor] Unspecified means the theme's borderSubtle; the fill fades from [fillAlpha] at the line's peak to [fillEndAlpha] over [fillFade] of the depth below it. */
@Immutable
data class LiveChartStyle(
    val lineWidth: Dp = 2.dp,
    val lineCap: StrokeCap = StrokeCap.Round,
    val crosshairColor: Color = Color.Unspecified,
    val crosshairWidth: Dp = 1.5.dp,
    val crosshairDash: Dp = 3.dp,
    val crosshairGap: Dp = 3.dp,
    val crosshairCap: StrokeCap = StrokeCap.Round,
    val pointRadius: Dp = 4.dp,
    val haloRadius: Dp = 10.dp,
    val haloAlpha: Float = 0.1f,
    val fillAlpha: Float = 0.12f,
    val fillEndAlpha: Float = 0f,
    val fillFade: Float = 1f,
    val scrubDimAlpha: Float = 0.3f,
)

/** Durations, easing and speeds for every chart animation. */
@Immutable
data class LiveChartMotion(
    val morphMillis: Int = 350,
    val morphEasing: Easing = FastOutSlowInEasing,
    val loadingFadeMillis: Int = 160,
    val catchUpMillis: Int = 400,
    val releaseMillis: Int = 250,
    val lerpSpeed: Float = 0.08f,
    val pulseMillis: Int = 1_500,
)

/** Scrub haptics for press start, each new point and release; null plays none. */
@Immutable
data class LiveChartHaptics(
    val start: HapticFeedbackType? = HapticFeedbackType.LongPress,
    val tick: HapticFeedbackType? = HapticFeedbackType.GestureThresholdActivate,
    val release: HapticFeedbackType? = HapticFeedbackType.TextHandleMove,
)

/** The point under the finger while scrubbing. */
@Immutable
data class LiveChartScrub(val index: Int, val point: LiveChartPoint)

private const val LongPressMillis = 150L
private const val BreathAlpha = 0.3f
private const val SettleFraction = 0.0005f
private val LabelFadeWidth = 24.dp

/** A live line chart that eases toward new points and reports scrubbing through [onScrub], null on release; [loading] grays a drawn line, or breathes a flat loading line when none was drawn. */
@Composable
fun LiveChart(
    data: LiveChartData,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    paused: Boolean = false,
    emptyText: String = "No data to display",
    formatValue: (Float) -> String = { it.toString() },
    onScrub: (LiveChartScrub?) -> Unit = {},
    style: LiveChartStyle = LiveChartStyle(),
    plotHeight: Dp? = null,
    reduceMotion: Boolean? = null,
    motion: LiveChartMotion = LiveChartMotion(),
    haptics: LiveChartHaptics = LiveChartHaptics(),
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val spacing = PlataTheme.spacing
    val systemReduceMotion = rememberReduceMotion()
    val still = reduceMotion ?: systemReduceMotion
    val feedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val points = data.points
    val latest by rememberUpdatedState(data)
    val isPaused by rememberUpdatedState(paused)
    val timing by rememberUpdatedState(motion)
    val lastTime = points.lastOrNull()?.timeMillis ?: 0L

    var end by remember { mutableLongStateOf(lastTime) }
    var tip by remember { mutableFloatStateOf(points.lastOrNull()?.value ?: 0f) }
    var low by remember { mutableFloatStateOf(0f) }
    var high by remember { mutableFloatStateOf(0f) }
    var seeded by remember { mutableStateOf(false) }
    var catchUpFrom by remember { mutableStateOf<Long?>(null) }
    var catchUpStart by remember { mutableLongStateOf(0L) }
    var wasPaused by remember { mutableStateOf(false) }

    val drawn = remember { mutableListOf<Offset>() }
    // captured during composition so the frame that flips loading still sees the line as drawn
    val held = remember(loading) { drawn.toList() }
    val heldLow = remember(loading) { low }
    val heldHigh = remember(loading) { high }
    val morph = remember(loading) { Animatable(if (!loading && held.isNotEmpty() && !still) 0f else 1f) }
    val currentMorph by rememberUpdatedState(morph)

    var scrub by remember { mutableStateOf<LiveChartScrub?>(null) }
    // restarted by anything that can unsettle it; ends once settled so a still chart lets Compose idle
    LaunchedEffect(still, data, paused, loading, scrub != null) {
        var settled = false
        while (!settled) {
            withFrameNanos { frame ->
                val d = latest
                val speed = timing.lerpSpeed
                val target = d.points.lastOrNull()
                if (target == null) { settled = true; return@withFrameNanos }
                // snap during a range morph so the line morphs straight to its final shape instead of stretching after
                if (!seeded || still || currentMorph.value < 1f) {
                    end = target.timeMillis; tip = target.value; catchUpFrom = null
                    val bounds = valueBounds(visibleRange(d.points, d.windowMillis, end).ifEmpty { d.points })
                    low = bounds.start; high = bounds.endInclusive; seeded = true
                    settled = currentMorph.value >= 1f
                    return@withFrameNanos
                }
                val visible = visibleRange(d.points, d.windowMillis, end.coerceAtMost(target.timeMillis))
                val bounds = valueBounds(visible.ifEmpty { d.points })
                if (isPaused) { wasPaused = true; settled = true; return@withFrameNanos }
                if (wasPaused) { wasPaused = false; catchUpFrom = end; catchUpStart = frame }
                val from = catchUpFrom
                if (from != null) {
                    val t = ((frame - catchUpStart) / 1_000_000f / timing.catchUpMillis.coerceAtLeast(1)).coerceIn(0f, 1f)
                    end = from + ((target.timeMillis - from) * FastOutSlowInEasing.transform(t)).toLong()
                    if (t >= 1f) catchUpFrom = null
                } else {
                    end += ((target.timeMillis - end) * speed).toLong().coerceAtLeast(if (end < target.timeMillis) 1L else 0L)
                }
                val epsilon = (bounds.endInclusive - bounds.start) * SettleFraction
                fun approach(current: Float, goal: Float) = if (abs(goal - current) <= epsilon) goal else current + (goal - current) * speed
                tip = approach(tip, target.value)
                low = approach(low, bounds.start)
                high = approach(high, bounds.endInclusive)
                settled = catchUpFrom == null && end == target.timeMillis &&
                    tip == target.value && low == bounds.start && high == bounds.endInclusive
            }
        }
    }

    val gray = remember { Animatable(if (loading) 1f else 0f) }
    val breath = remember { Animatable(1f) }
    LaunchedEffect(loading, still) {
        val target = if (loading) 1f else 0f
        if (still) {
            gray.snapTo(target)
            breath.snapTo(1f)
            morph.snapTo(1f)
            return@LaunchedEffect
        }
        val m = timing
        launch { gray.animateTo(target, if (loading) tween(m.loadingFadeMillis) else tween(m.morphMillis, easing = m.morphEasing)) }
        // animates from the current value, so the breath starts at full opacity and the return eases with the morph
        launch {
            if (loading) breath.animateTo(BreathAlpha, infiniteRepeatable(tween(m.pulseMillis / 2, easing = LinearEasing), RepeatMode.Reverse))
            else breath.animateTo(1f, tween(m.morphMillis, easing = m.morphEasing))
        }
        morph.animateTo(1f, tween(m.morphMillis, easing = m.morphEasing))
    }

    var released by remember { mutableStateOf<LiveChartScrub?>(null) }
    val release = remember { Animatable(0f) }
    // frame callbacks run before recomposition, so the loop's snap lands a frame after new data; derive it here instead
    val snap = still || !seeded
    val windowEnd = if (data.windowMillis == null || snap) lastTime else end
    val visible = visibleRange(points, data.windowMillis, windowEnd)
    val firstVisible = if (visible.isEmpty()) 0 else points.indexOf(visible.first())
    val snapBounds = if (snap) valueBounds(visible.ifEmpty { points }) else null
    val shownLow = snapBounds?.start ?: low
    val shownHigh = snapBounds?.endInclusive ?: high
    val scrubPoints by rememberUpdatedState(visible)
    val scrubOffset by rememberUpdatedState(firstVisible)
    val scrubbable by rememberUpdatedState(!loading)
    val instant by rememberUpdatedState(still)
    val emit by rememberUpdatedState(onScrub)
    val buzz by rememberUpdatedState(haptics)

    val labelT = morph.value
    val highLabel = if (labelT < 1f) heldHigh + (shownHigh - heldHigh) * labelT else shownHigh
    val lowLabel = if (labelT < 1f) heldLow + (shownLow - heldLow) * labelT else shownLow
    // the gray LaunchedEffect lands a frame late, which would show the first frame of new data still gray
    val grayLevel = if (still) (if (loading) 1f else 0f) else gray.value
    val breathLevel = if (still) 1f else breath.value
    // labels keep their space when hidden so the layout doesn't jump once points arrive
    val labelModifier = Modifier.fillMaxWidth().padding(horizontal = spacing.screenMargin)
        .alpha(if ((loading && held.isEmpty()) || (!loading && points.isEmpty())) 0f else 1f)
    Column(modifier) {
        PlataText(formatValue(highLabel), typography.subhead, colors.textSecondary, labelModifier, textAlign = TextAlign.End)
        Box(Modifier.fillMaxWidth().then(if (plotHeight != null) Modifier.height(plotHeight) else Modifier.weight(1f))) {
            when {
                loading && held.isEmpty() -> LoadingLine(breathLevel, style)
                !loading && points.isEmpty() -> PlataText(emptyText, typography.subhead, colors.textSecondary, Modifier.align(Alignment.Center))
                else -> {
                    val ink = lerp(colors.accent, colors.textTertiary, grayLevel)
                    val crosshair = style.crosshairColor.takeOrElse { colors.borderSubtle }
                    Canvas(
                        Modifier.fillMaxSize().pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                if (!scrubbable) return@awaitEachGesture
                                val start = withTimeoutOrNull(LongPressMillis) {
                                    var total = Offset.Zero
                                    while (true) {
                                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: return@withTimeoutOrNull false
                                        if (!change.pressed) return@withTimeoutOrNull false
                                        total += change.positionChange()
                                        if (total.getDistance() > viewConfiguration.touchSlop) return@withTimeoutOrNull abs(total.x) > abs(total.y)
                                    }
                                    @Suppress("UNREACHABLE_CODE") false
                                } ?: true
                                if (!start) return@awaitEachGesture
                                fun report(x: Float, tick: Boolean) {
                                    val vis = scrubPoints
                                    if (vis.isEmpty()) return
                                    val i = scrubIndex(x, size.width.toFloat(), vis.size)
                                    val s = LiveChartScrub(scrubOffset + i, vis[i])
                                    if (tick && s.index != scrub?.index) buzz.tick?.let(feedback::performHapticFeedback)
                                    if (s != scrub) { scrub = s; emit(s) }
                                }
                                scope.launch { release.snapTo(1f) }
                                buzz.start?.let(feedback::performHapticFeedback)
                                report(down.position.x, tick = false)
                                while (true) {
                                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break
                                    change.consume()
                                    report(change.position.x, tick = true)
                                }
                                released = scrub
                                scrub = null
                                emit(null)
                                buzz.release?.let(feedback::performHapticFeedback)
                                scope.launch {
                                    if (instant) release.snapTo(0f) else release.animateTo(0f, tween(timing.releaseMillis))
                                }
                            }
                        },
                    ) {
                        val plotWidth = size.width.coerceAtLeast(1f)
                        val lo = snapBounds?.start ?: low
                        val span = ((snapBounds?.endInclusive ?: high) - lo).takeIf { it > 0f } ?: 1f
                        fun yOf(v: Float) = size.height * (1 - (v - lo) / span)
                        val window = data.windowMillis
                        val steady = if (visible.isEmpty()) emptyList() else {
                            val startTime = if (window == null) visible.first().timeMillis else windowEnd - window
                            val duration = ((if (window == null) visible.last().timeMillis else windowEnd) - startTime).coerceAtLeast(1L).toFloat()
                            visible.mapIndexed { i, p ->
                                val v = if (data.live && !snap && i == visible.lastIndex) tip else p.value
                                Offset((p.timeMillis - startTime) / duration * plotWidth, yOf(v))
                            }
                        }
                        val heldCoords = held.map { Offset(it.x * size.width, it.y * size.height) }
                        val t = morph.value
                        val coords = when {
                            loading -> heldCoords
                            t < 1f && heldCoords.isNotEmpty() -> morphLine(heldCoords, steady, t)
                            else -> steady
                        }
                        if (coords.isEmpty()) return@Canvas
                        drawn.clear()
                        coords.mapTo(drawn) { Offset(it.x / size.width, it.y / size.height) }
                        val line = Path().apply { coords.forEachIndexed { i, o -> if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y) } }
                        val fill = Path().apply {
                            addPath(line)
                            lineTo(coords.last().x, size.height)
                            lineTo(coords.first().x, size.height)
                            close()
                        }
                        val stroke = Stroke(style.lineWidth.toPx(), cap = style.lineCap, join = StrokeJoin.Round)
                        val fillTop = coords.minOf { it.y }
                        val fillBottom = fillTop + (size.height - fillTop) * style.fillFade.coerceAtLeast(0.01f)
                        fun drawSeries(dim: Float) {
                            val alpha = ink.alpha * dim * breathLevel
                            val fillBrush = Brush.verticalGradient(
                                listOf(ink.copy(alpha = style.fillAlpha * alpha), ink.copy(alpha = style.fillEndAlpha * alpha)),
                                startY = fillTop,
                                endY = fillBottom,
                            )
                            drawPath(fill, fillBrush)
                            drawPath(line, ink.copy(alpha = alpha), style = stroke)
                        }
                        val shown = scrub ?: released
                        val fade = if (scrub != null) 1f else release.value
                        // located by time: after release the frozen list is replaced by the live one, whose indices have shifted
                        val at = shown?.takeIf { fade > 0f && coords.size == visible.size }
                            ?.let { s -> visible.indexOfFirst { it.timeMillis == s.point.timeMillis } }
                            ?.let { coords.getOrNull(it) }
                        if (at == null) {
                            drawSeries(1f)
                        } else {
                            // bleed past the canvas so the clip only splits at the point and never trims stroke caps at the edges
                            clipRect(left = -size.width, top = -size.height, right = at.x, bottom = size.height * 2) { drawSeries(1f) }
                            clipRect(left = at.x, top = -size.height, right = size.width * 2, bottom = size.height * 2) {
                                drawSeries(1f + (style.scrubDimAlpha - 1f) * fade)
                            }
                            val crosshairWidth = style.crosshairWidth.toPx()
                            // round/square caps extend each dash by the stroke width, so add it to the off interval to keep the visible gap at crosshairGap
                            val capExtent = if (style.crosshairCap == StrokeCap.Butt) 0f else crosshairWidth
                            val crosshairEffect = if (style.crosshairGap > 0.dp) {
                                PathEffect.dashPathEffect(floatArrayOf(style.crosshairDash.toPx(), style.crosshairGap.toPx() + capExtent))
                            } else {
                                null
                            }
                            drawLine(
                                crosshair.copy(alpha = crosshair.alpha * fade),
                                Offset(at.x, 0f),
                                Offset(at.x, size.height),
                                strokeWidth = crosshairWidth,
                                cap = style.crosshairCap,
                                pathEffect = crosshairEffect,
                            )
                            drawCircle(ink.copy(alpha = ink.alpha * style.haloAlpha * fade), style.haloRadius.toPx() * fade, at)
                            drawCircle(ink.copy(alpha = ink.alpha * fade), style.pointRadius.toPx() * fade, at)
                        }
                    }
                }
            }
        }
        PlataText(formatValue(lowLabel), typography.subhead, colors.textSecondary, labelModifier, textAlign = TextAlign.End)
        Spacer(Modifier.height(spacing.s12))
        when (val axis = data.axis) {
            is LiveChartAxis.Fixed -> Row(Modifier.fillMaxWidth().padding(horizontal = spacing.screenMargin)) {
                axis.labels.forEach { PlataText(it, typography.subhead, colors.textSecondary, Modifier.weight(1f), textAlign = TextAlign.Center) }
            }
            is LiveChartAxis.Clock -> ClockAxis(axis, data.windowMillis, windowEnd)
        }
    }
}

@Composable
private fun LoadingLine(breath: Float, style: LiveChartStyle) {
    val ink = PlataTheme.colors.textTertiary
    Canvas(Modifier.fillMaxSize()) {
        val y = size.height / 2
        drawLine(ink.copy(alpha = ink.alpha * breath), Offset(0f, y), Offset(size.width, y), strokeWidth = style.lineWidth.toPx(), cap = style.lineCap)
    }
}

@Composable
private fun ClockAxis(axis: LiveChartAxis.Clock, windowMillis: Long?, endMillis: Long) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val window = windowMillis ?: return
    val start = endMillis - window
    val ticks = clockTicks(start, endMillis, axis.stepMillis)
    val fade = with(LocalDensity.current) { LabelFadeWidth.toPx() }
    Layout(
        content = { ticks.forEach { PlataText(formatClockTime(it), typography.subhead, colors.textSecondary) } },
        modifier = Modifier.fillMaxWidth(),
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val width = constraints.maxWidth
        layout(width, placeables.maxOfOrNull { it.height } ?: 0) {
            placeables.forEachIndexed { i, p ->
                val center = (ticks[i] - start).toFloat() / window * width
                val x = (center - p.width / 2f).roundToInt()
                val edge = minOf(x.toFloat(), width - (x + p.width).toFloat())
                p.placeWithLayer(x, 0) { alpha = (edge / fade).coerceIn(0f, 1f) }
            }
        }
    }
}

private fun List<Offset>.pointAt(u: Float): Offset {
    if (size == 1) return first()
    val at = u * (size - 1)
    val lo = at.toInt().coerceIn(0, size - 2)
    return lerp(this[lo], this[lo + 1], at - lo)
}

internal fun visibleRange(points: List<LiveChartPoint>, windowMillis: Long?, endMillis: Long): List<LiveChartPoint> =
    if (windowMillis == null) points else points.filter { it.timeMillis in (endMillis - windowMillis)..endMillis }

internal fun valueBounds(points: List<LiveChartPoint>): ClosedFloatingPointRange<Float> {
    if (points.isEmpty()) return 0f..1f
    val min = points.minOf { it.value }
    val max = points.maxOf { it.value }
    val pad = if (max > min) 0f else maxOf(abs(max) * 0.001f, 1f)
    return (min - pad)..(max + pad)
}

internal fun scrubIndex(x: Float, width: Float, count: Int): Int =
    if (count <= 1 || width <= 0f) 0 else (x / width * (count - 1)).roundToInt().coerceIn(0, count - 1)

internal fun clockTicks(startMillis: Long, endMillis: Long, stepMillis: Long): List<Long> {
    if (stepMillis <= 0 || endMillis < startMillis) return emptyList()
    val first = (startMillis + stepMillis - 1).floorDiv(stepMillis) * stepMillis
    return generateSequence(first) { it + stepMillis }.takeWhile { it <= endMillis }.toList()
}

internal fun resample(points: List<Float>, count: Int): List<Float> {
    if (points.isEmpty() || count <= 0) return emptyList()
    if (points.size == 1 || count == 1) return List(count) { points.first() }
    return List(count) { i ->
        val at = i * (points.size - 1f) / (count - 1)
        val lo = at.toInt().coerceAtMost(points.size - 2)
        points[lo] + (points[lo + 1] - points[lo]) * (at - lo)
    }
}

internal fun morphLine(from: List<Offset>, to: List<Offset>, t: Float): List<Offset> {
    if (from.isEmpty() || t >= 1f) return to
    if (to.isEmpty()) return from
    // sample both at every vertex of either so t = 0 and t = 1 reproduce them exactly
    val stops = (from.indices.map { it / (from.size - 1f).coerceAtLeast(1f) } + to.indices.map { it / (to.size - 1f).coerceAtLeast(1f) })
        .distinct().sorted()
    return stops.map { u -> lerp(from.pointAt(u), to.pointAt(u), t) }
}

/** [epochMillis] as HH:mm in [timeZone]. */
fun formatClockTime(epochMillis: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val time = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone)
    return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

private val PreviewPoints = List(60) { i -> LiveChartPoint(1_700_000_000_000L + i * 5_000L, 100f + (i % 7) * 1.5f - (i % 3)) }
private val PreviewModifier = Modifier.width(375.dp).height(260.dp)
private val PreviewEmpty = LiveChartData(emptyList(), live = false, windowMillis = null, axis = LiveChartAxis.Fixed(emptyList()))

@Preview
@Composable
internal fun LiveChartLivePreview() = PlataTheme {
    LiveChart(LiveChartData(PreviewPoints, live = true, windowMillis = 300_000L, axis = LiveChartAxis.Clock(60_000L)), PreviewModifier)
}

@Preview
@Composable
internal fun LiveChartFixedPreview() = PlataTheme {
    LiveChart(LiveChartData(PreviewPoints, live = false, windowMillis = null, axis = LiveChartAxis.Fixed(listOf("Mon", "Tue", "Wed", "Thu"))), PreviewModifier)
}

@Preview
@Composable
internal fun LiveChartLoadingPreview() = PlataTheme {
    LiveChart(PreviewEmpty, PreviewModifier, loading = true)
}

@Preview
@Composable
internal fun LiveChartEmptyPreview() = PlataTheme {
    LiveChart(PreviewEmpty, PreviewModifier)
}
