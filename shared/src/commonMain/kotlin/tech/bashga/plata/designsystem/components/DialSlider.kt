package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.plataShadow
import tech.bashga.plata.designsystem.theme.PlataColors
import tech.bashga.plata.designsystem.theme.PlataTheme
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val Diameter = 259.dp
private val Track = 30.dp
private val Knob = 26.dp
private val TickDot = 10.dp
private val GripBar = 1.5.dp
private val GripHeight = 7.dp
private val GripGap = 2.dp
private val Ticks = listOf(0.25f, 0.5f, 0.75f)
private const val GlossStart = 0.36f
private const val GlossFull = 0.6f

private class DialStop(val at: Float, val color: Color)

private fun PlataColors.dialStops() = listOf(
    DialStop(0f, dialStart),
    DialStop(0.25f, dialQuarter),
    DialStop(0.42f, dialMid),
    DialStop(0.65f, dialEnd),
    DialStop(1f, dialEnd),
)

private fun List<DialStop>.colorAt(fraction: Float): Color {
    val upper = indexOfFirst { it.at >= fraction }.coerceAtLeast(1)
    val a = this[upper - 1]
    val b = this[upper]
    return lerp(a.color, b.color, ((fraction - a.at) / (b.at - a.at)).coerceIn(0f, 1f))
}

/**
 * Circular slider (b3c28fcb, f4915e4c): a 259 ring with a 30 track, the fill sweeping clockwise from 12 o'clock to
 * [value] (0–1) with rounded caps, the knob (a white 26 disc with a grip, ringed by the fill) at the sweep's end, dots
 * at 25/50/75% and [label] (28 bold) in the centre. The fill colour follows the angle, blue at the top through violet
 * to pink. Dragging or tapping the ring calls [onValueChange] when it's set.
 */
@Composable
fun DialSlider(
    value: Float,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: ((Float) -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val stops = colors.dialStops()
    val fraction = value.coerceIn(0f, 1f)
    val angle = fraction * 2 * PI.toFloat()
    val knobColor = stops.colorAt(fraction)
    val radius = (Diameter - Track) / 2
    val onChange = rememberUpdatedState(onValueChange)
    val input = if (onValueChange == null) Modifier else Modifier
        .pointerInput(Unit) { detectTapGestures { onChange.value?.invoke(fractionAt(it, size.width / 2f)) } }
        .pointerInput(Unit) { detectDragGestures { change, _ -> onChange.value?.invoke(fractionAt(change.position, size.width / 2f)) } }
    Box(modifier.size(Diameter).then(input), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Track.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawCircle(colors.surfaceControl, radius = (size.width - stroke) / 2, style = Stroke(stroke))
            val r = (size.width - stroke) / 2
            fun tick(at: Float, color: Color) {
                val a = at * 2 * PI.toFloat()
                drawCircle(color, radius = TickDot.toPx() / 2, center = center + Offset(r * sin(a), -r * cos(a)))
            }
            Ticks.filter { it > fraction }.forEach { tick(it, colors.dialTick) }
            if (fraction > 0f) {
                // sweep gradients start at 3 o'clock; rotate so 0 is 12 o'clock
                rotate(-90f) {
                    val brush = Brush.sweepGradient(*stops.map { it.at to it.color }.toTypedArray())
                    drawArc(brush, 0f, fraction * 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Butt))
                    if (fraction > GlossStart) drawGloss(fraction, stroke, inset, arcSize, colors.surfaceCard, colors.textPrimary)
                }
                // round caps drawn apart: a gradient cap left of 12 o'clock would take the sweep's last colour
                drawCircle(colors.dialStart, radius = stroke / 2, center = center + Offset(0f, -r))
                drawCircle(knobColor, radius = stroke / 2, center = center + Offset(r * sin(angle), -r * cos(angle)))
                Ticks.filter { it <= fraction }.forEach { tick(it, colors.surfaceCard) }
            }
        }
        PlataText(label, PlataTheme.typography.title1, colors.textPrimary, maxLines = 1)
        DialKnob(knobColor, Modifier.offset(radius * sin(angle), -radius * cos(angle)))
    }
}

// f4915e4c shades the pink half like a tube (light centre line, darker edges), fading in from ~130° to ~215°
private fun DrawScope.drawGloss(fraction: Float, stroke: Float, inset: Float, arcSize: Size, light: Color, dark: Color) {
    val outer = size.width / 2
    val inner = (outer - stroke) / outer
    fun across(t: Float) = inner + (1f - inner) * t
    val tube = Brush.radialGradient(
        inner to dark.copy(alpha = 0.07f),
        across(0.15f) to Color.Transparent,
        across(0.35f) to light.copy(alpha = 0.3f),
        across(0.6f) to light.copy(alpha = 0.3f),
        across(0.85f) to Color.Transparent,
        1f to dark.copy(alpha = 0.07f),
        center = center,
        radius = outer,
    )
    val fade = Brush.sweepGradient(0f to Color.Transparent, GlossStart to Color.Transparent, GlossFull to light, 1f to light, center = center)
    val bounds = Rect(Offset.Zero, size)
    drawIntoCanvas { canvas ->
        canvas.saveLayer(bounds, Paint())
        drawArc(tube, 0f, fraction * 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Butt))
        drawArc(fade, 0f, fraction * 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Butt), blendMode = BlendMode.DstIn)
        canvas.restore()
    }
}

private fun fractionAt(position: Offset, center: Float): Float {
    val a = atan2(position.x - center, center - position.y)
    return ((if (a < 0) a + 2 * PI.toFloat() else a) / (2 * PI.toFloat())).coerceIn(0f, 1f)
}

@Composable
private fun DialKnob(ring: Color, modifier: Modifier) {
    Box(
        modifier
            .size(Track)
            .background(ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(Knob)
                .plataShadow(PlataTheme.shadows.knob, CircleShape)
                .background(PlataTheme.colors.surfaceCard, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(GripGap)) {
                repeat(3) { Box(Modifier.size(GripBar, GripHeight).background(ring, RoundedCornerShape(GripBar))) }
            }
        }
    }
}

@Preview
@Composable
internal fun DialSliderPreview() = PlataTheme { DialSlider(0.05f, "~5%") }

@Preview
@Composable
internal fun DialSlider70Preview() = PlataTheme { DialSlider(0.7f, "~70%") }
