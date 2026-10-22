package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.theme.PlataTheme

/** [Track]: equal segments in a grey capsule with a white thumb. [Plain]: a chart range picker, the selection a white disc. */
enum class SegmentedControlStyle { Track, Plain }

@Composable
fun SegmentedControl(
    segments: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    style: SegmentedControlStyle = SegmentedControlStyle.Track,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    when (style) {
        SegmentedControlStyle.Track -> Row(
            modifier
                .height(PlataTheme.sizes.nav)
                .background(colors.surfaceBackground, ContinuousRoundedShape(PlataTheme.radii.full))
                .padding(2.dp),
            // the thumb stops ~4 short of the centre line on fdc6c570, like UISegmentedControl's divider gap
            horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s4),
        ) {
            segments.forEachIndexed { index, segment ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(
                            if (index == selectedIndex) Modifier.background(colors.surfaceCard, ContinuousRoundedShape(PlataTheme.radii.full)) else Modifier,
                        )
                        .pressable(role = Role.Tab) { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) { PlataText(segment, typography.subheadStrong, colors.textPrimary, maxLines = 1) }
            }
        }
        // 13 semibold in 40-wide cells 20 apart, longer labels padded 10; the selection disc is 35 across (341fcc04)
        SegmentedControlStyle.Plain -> Row(
            modifier.height(PlataTheme.sizes.buttonSmall),
            horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s20),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            segments.forEachIndexed { index, segment ->
                Box(
                    Modifier
                        .widthIn(min = PlataTheme.sizes.buttonSmall)
                        .fillMaxHeight()
                        .pressable(role = Role.Tab) { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (index == selectedIndex) Box(Modifier.size(35.dp).background(colors.surfaceCard, CircleShape))
                    PlataText(segment, typography.footnoteStrong, colors.textPrimary, Modifier.padding(horizontal = 10.dp), maxLines = 1)
                }
            }
        }
    }
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
