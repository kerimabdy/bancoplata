package tech.bashga.plata.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.theme.PlataTheme

/** [Track]: equal segments in a grey capsule with a white thumb. The thumb slides and resizes to the selected segment. [Plain]: a chart range picker, the selection a white disc that widens to fit longer labels; `equalWidths` splits the full width evenly. */
enum class SegmentedControlStyle { Track, Plain }

@Composable
fun SegmentedControl(
    segments: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    style: SegmentedControlStyle = SegmentedControlStyle.Track,
    equalWidths: Boolean = false,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val cells = remember { mutableStateMapOf<Int, Cell>() }
    val labelWidths = remember { mutableStateMapOf<Int, Float>() }
    val position = remember { Animatable(selectedIndex.toFloat()) }
    LaunchedEffect(selectedIndex) { position.animateTo(selectedIndex.toFloat(), ThumbSpring) }
    // the thumb reads the cells while drawing, so it stays on its label when the width changes; only the index animates
    val thumb = SegmentThumb(position, cells, segments.lastIndex) { cell, index ->
        when (style) {
            SegmentedControlStyle.Track -> cell
            SegmentedControlStyle.Plain -> {
                val thumbWidth = ((labelWidths[index] ?: 0f) + 20.dp.toPx()).coerceIn(PlainThumb.toPx(), maxOf(PlainThumb.toPx(), cell.width))
                Cell(cell.left + (cell.width - thumbWidth) / 2, thumbWidth)
            }
        }
    }

    when (style) {
        SegmentedControlStyle.Track -> Row(
            modifier
                .height(PlataTheme.sizes.nav)
                .background(colors.surfaceBackground, ContinuousRoundedShape(PlataTheme.radii.full))
                .padding(2.dp)
                .drawThumb(thumb, colors.surfaceCard, ContinuousRoundedShape(PlataTheme.radii.full)),
            // the thumb stops ~4 short of the centre line on fdc6c570, like UISegmentedControl's divider gap
            horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s4),
        ) {
            segments.forEachIndexed { index, segment ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .trackCell(index, cells)
                        .pressable(role = Role.Tab) { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) { PlataText(segment, typography.subheadStrong, colors.textPrimary, maxLines = 1) }
            }
        }
        // 13 semibold in 40-wide cells 20 apart, longer labels padded 10; the selection is 35 tall and at least as wide (341fcc04)
        SegmentedControlStyle.Plain -> Row(
            modifier
                .height(PlataTheme.sizes.buttonSmall)
                .drawThumb(thumb, colors.surfaceCard, CircleShape, height = PlainThumb),
            horizontalArrangement = if (equalWidths) Arrangement.Start else Arrangement.spacedBy(PlataTheme.spacing.s20),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            segments.forEachIndexed { index, segment ->
                Box(
                    (if (equalWidths) Modifier.weight(1f) else Modifier.widthIn(min = PlataTheme.sizes.buttonSmall))
                        .fillMaxHeight()
                        .trackCell(index, cells)
                        .pressable(role = Role.Tab) { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    // no padding when equal: "All time" (~47 dp) must fit a sixth of a 343 dp row (~57 dp)
                    PlataText(
                        segment,
                        typography.footnoteStrong,
                        colors.textPrimary,
                        (if (equalWidths) Modifier else Modifier.padding(horizontal = 10.dp))
                            .onSizeChanged { labelWidths[index] = it.width.toFloat() },
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private val PlainThumb = 35.dp

private data class Cell(val left: Float, val width: Float)

private fun Modifier.trackCell(index: Int, cells: MutableMap<Int, Cell>) = onPlaced { coordinates ->
    val cell = Cell(coordinates.positionInParent().x, coordinates.size.width.toFloat())
    if (cells[index] != cell) cells[index] = cell
}

private class SegmentThumb(
    val position: Animatable<Float, AnimationVector1D>,
    val cells: Map<Int, Cell>,
    val lastIndex: Int,
    val bounds: Density.(Cell, Int) -> Cell,
)

private val ThumbSpring = spring<Float>(dampingRatio = 0.8f, stiffness = 500f, visibilityThreshold = 0.001f)

private fun Modifier.drawThumb(thumb: SegmentThumb, color: Color, shape: Shape, height: Dp? = null) = drawBehind {
    val position = thumb.position.value
    val from = floor(position).toInt().coerceIn(0, thumb.lastIndex)
    val to = (from + 1).coerceAtMost(thumb.lastIndex)
    val start = thumb.cells[from]?.let { thumb.bounds(this, it, from) } ?: return@drawBehind
    val end = thumb.cells[to]?.let { thumb.bounds(this, it, to) } ?: start
    val fraction = position - from
    val thumbHeight = height?.toPx() ?: size.height
    val outline = shape.createOutline(Size(lerp(start.width, end.width, fraction), thumbHeight), layoutDirection, this)
    translate(lerp(start.left, end.left, fraction), (size.height - thumbHeight) / 2) { drawOutline(outline, color) }
}

@Preview
@Composable
internal fun SegmentedControlTrackPreview() = PlataTheme {
    SegmentedControl(listOf("Gainers", "Losers"), 0, {}, Modifier.width(313.dp))
}

@Preview
@Composable
internal fun SegmentedControlPlainPreview() = PlataTheme {
    SegmentedControl(listOf("1d", "1w", "1m", "1y", "All time"), 0, {}, style = SegmentedControlStyle.Plain)
}
