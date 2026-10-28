package tech.bashga.plata.screens.invest

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.components.LiveChartHaptics
import tech.bashga.plata.designsystem.components.LiveChartMotion
import tech.bashga.plata.designsystem.components.LiveChartStyle
import tech.bashga.plata.designsystem.theme.PlataColors
import tech.bashga.plata.dev.dials.DialOption
import tech.bashga.plata.dev.dials.DialPanel

/** Theme tokens selectable as the scrub crosshair color. */
enum class ChartInk { NavSeparator, BorderSubtle, TextTertiary, TextSecondary, Accent }

/** Line cap choices for the chart stroke. */
enum class ChartCap { Round, Square, Butt }

/** Easing curve choices for the range morph. */
enum class ChartEasing { FastOutSlowIn, Linear, EaseOut }

/** Haptic choices for scrub start, tick and release; [Off] plays none. */
enum class ChartHaptic {
    Off, SegmentTick, SegmentFrequentTick, TextHandleMove, GestureThresholdActivate, GestureEnd,
    ContextClick, LongPress, VirtualKey, KeyboardTap, ToggleOn, ToggleOff, Confirm, Reject,
}

/** Debug state overrides for the stock chart; [reduceMotion] null follows the system setting. */
@Immutable
data class LiveChartDebug(
    val forceLoading: Boolean = false,
    val empty: Boolean = false,
    val liveFeed: Boolean = true,
    val reduceMotion: Boolean? = null,
)

/** The theme color this ink names. */
fun ChartInk.color(colors: PlataColors): Color = when (this) {
    ChartInk.NavSeparator -> colors.navSeparator
    ChartInk.BorderSubtle -> colors.borderSubtle
    ChartInk.TextTertiary -> colors.textTertiary
    ChartInk.TextSecondary -> colors.textSecondary
    ChartInk.Accent -> colors.accent
}

/** The Compose cap this choice names. */
val ChartCap.strokeCap: StrokeCap get() = when (this) {
    ChartCap.Round -> StrokeCap.Round
    ChartCap.Square -> StrokeCap.Square
    ChartCap.Butt -> StrokeCap.Butt
}

/** The Compose easing this choice names. */
val ChartEasing.easing: Easing get() = when (this) {
    ChartEasing.FastOutSlowIn -> FastOutSlowInEasing
    ChartEasing.Linear -> LinearEasing
    ChartEasing.EaseOut -> EaseOut
}

/** The Compose haptic this choice names; null for [ChartHaptic.Off]. */
val ChartHaptic.type: HapticFeedbackType? get() = when (this) {
    ChartHaptic.Off -> null
    ChartHaptic.SegmentTick -> HapticFeedbackType.SegmentTick
    ChartHaptic.SegmentFrequentTick -> HapticFeedbackType.SegmentFrequentTick
    ChartHaptic.TextHandleMove -> HapticFeedbackType.TextHandleMove
    ChartHaptic.GestureThresholdActivate -> HapticFeedbackType.GestureThresholdActivate
    ChartHaptic.GestureEnd -> HapticFeedbackType.GestureEnd
    ChartHaptic.ContextClick -> HapticFeedbackType.ContextClick
    ChartHaptic.LongPress -> HapticFeedbackType.LongPress
    ChartHaptic.VirtualKey -> HapticFeedbackType.VirtualKey
    ChartHaptic.KeyboardTap -> HapticFeedbackType.KeyboardTap
    ChartHaptic.ToggleOn -> HapticFeedbackType.ToggleOn
    ChartHaptic.ToggleOff -> HapticFeedbackType.ToggleOff
    ChartHaptic.Confirm -> HapticFeedbackType.Confirm
    ChartHaptic.Reject -> HapticFeedbackType.Reject
}

/** The "Live chart" dials panel: debug state overrides and style tuning for the stock chart. */
object LiveChartDials {
    private val panel = DialPanel("Live chart")

    private val state = panel.folder("state")
    private val forceLoading = state.toggle("forceLoading", false)
    private val empty = state.toggle("empty", false)
    private val liveFeed = state.toggle("liveFeed", true)
    private val feedTickSeconds = state.slider("feedTickSeconds", (FeedTickMillis / 1_000).toFloat(), 1f..60f, 1f, "s")
    private val reduceMotion = state.select(
        "reduceMotion",
        listOf(DialOption("System"), DialOption("On"), DialOption("Off")),
    )

    /** Called when the "Simulate range load" action fires. */
    var onSimulateRangeLoad: () -> Unit = {}
    private val simulateRangeLoad = state.action("simulateRangeLoad") { onSimulateRangeLoad() }

    private val styleFolder = panel.folder("style")
    private val crosshairColor = styleFolder.select("crosshairColor", ChartInk.entries.map { DialOption(it.name) }, default = ChartInk.BorderSubtle.name)
    private val crosshairWidth = styleFolder.slider("crosshairWidth", 1.5f, 0.5f..3f, 0.1f, "dp")
    private val crosshairDash = styleFolder.slider("crosshairDash", 3f, 0f..12f, 0.5f, "dp")
    private val crosshairGap = styleFolder.slider("crosshairGap", 3f, 0f..12f, 0.5f, "dp")
    private val crosshairCap = styleFolder.select("crosshairCap", ChartCap.entries.map { DialOption(it.name) }, default = ChartCap.Round.name)
    private val lineWidth = styleFolder.slider("lineWidth", 2f, 1f..4f, 0.1f, "dp")
    private val pointRadius = styleFolder.slider("pointRadius", 4f, 2f..8f, 0.5f, "dp")
    private val haloRadius = styleFolder.slider("haloRadius", 10f, 0f..20f, 0.5f, "dp")
    private val haloAlpha = styleFolder.slider("haloAlpha", 0.1f, 0f..1f, 0.01f)
    private val fillAlpha = styleFolder.slider("fillAlpha", 0.12f, 0f..0.5f, 0.01f)
    private val fillEndAlpha = styleFolder.slider("fillEndAlpha", 0f, 0f..0.5f, 0.01f)
    private val fillFade = styleFolder.slider("fillFade", 1f, 0.1f..1f, 0.05f)
    private val scrubDimAlpha = styleFolder.slider("scrubDimAlpha", 0.3f, 0f..1f, 0.01f)
    private val lineCap = styleFolder.select("lineCap", ChartCap.entries.map { DialOption(it.name) })

    private val motionFolder = panel.folder("motion")
    private val morphMillis = motionFolder.slider("morphMillis", 350f, 100f..1000f, 10f, "ms")
    private val morphEasing = motionFolder.select("morphEasing", ChartEasing.entries.map { DialOption(it.name) })
    private val loadingFadeMillis = motionFolder.slider("loadingFadeMillis", 160f, 0f..500f, 10f, "ms")
    private val rangeLoadMillisDial = motionFolder.slider("rangeLoadMillis", RangeLoadMillis.toFloat(), 0f..2000f, 50f, "ms")
    private val catchUpMillis = motionFolder.slider("catchUpMillis", 400f, 0f..1000f, 10f, "ms")
    private val releaseMillis = motionFolder.slider("releaseMillis", 250f, 0f..600f, 10f, "ms")
    private val lerpSpeed = motionFolder.slider("lerpSpeed", 0.08f, 0.02f..0.3f, 0.01f)
    private val pulseMillis = motionFolder.slider("pulseMillis", 1500f, 500f..3000f, 50f, "ms")

    private val hapticsFolder = panel.folder("haptics")
    private val hapticOptions = ChartHaptic.entries.map { DialOption(it.name) }
    private val startHaptic = hapticsFolder.select("start", hapticOptions, default = ChartHaptic.LongPress.name)
    private val tickHaptic = hapticsFolder.select("tick", hapticOptions, default = ChartHaptic.GestureThresholdActivate.name)
    private val releaseHaptic = hapticsFolder.select("release", hapticOptions, default = ChartHaptic.TextHandleMove.name)

    /** The current state overrides. */
    val debug: LiveChartDebug get() = LiveChartDebug(
        forceLoading = forceLoading.value,
        empty = empty.value,
        liveFeed = liveFeed.value,
        reduceMotion = when (reduceMotion.value) {
            "On" -> true
            "Off" -> false
            else -> null
        },
    )

    /** The current style in [colors]. */
    fun style(colors: PlataColors): LiveChartStyle = LiveChartStyle(
        lineWidth = lineWidth.value.dp,
        lineCap = ChartCap.valueOf(lineCap.value).strokeCap,
        crosshairColor = ChartInk.valueOf(crosshairColor.value).color(colors),
        crosshairWidth = crosshairWidth.value.dp,
        crosshairDash = crosshairDash.value.dp,
        crosshairGap = crosshairGap.value.dp,
        crosshairCap = ChartCap.valueOf(crosshairCap.value).strokeCap,
        pointRadius = pointRadius.value.dp,
        haloRadius = haloRadius.value.dp,
        haloAlpha = haloAlpha.value,
        fillAlpha = fillAlpha.value,
        fillEndAlpha = fillEndAlpha.value,
        fillFade = fillFade.value,
        scrubDimAlpha = scrubDimAlpha.value,
    )

    /** The current motion tuning. */
    val motion: LiveChartMotion get() = LiveChartMotion(
        morphMillis = morphMillis.value.toInt(),
        morphEasing = ChartEasing.valueOf(morphEasing.value).easing,
        loadingFadeMillis = loadingFadeMillis.value.toInt(),
        catchUpMillis = catchUpMillis.value.toInt(),
        releaseMillis = releaseMillis.value.toInt(),
        lerpSpeed = lerpSpeed.value,
        pulseMillis = pulseMillis.value.toInt(),
    )

    /** The current simulated range load time. */
    val rangeLoadMillis: Long get() = rangeLoadMillisDial.value.toLong()

    /** The current live feed tick interval. */
    val feedTickMillis: Long get() = feedTickSeconds.value.toLong() * 1_000

    /** The current scrub haptics. */
    val haptics: LiveChartHaptics get() = LiveChartHaptics(
        start = ChartHaptic.valueOf(startHaptic.value).type,
        tick = ChartHaptic.valueOf(tickHaptic.value).type,
        release = ChartHaptic.valueOf(releaseHaptic.value).type,
    )
}
