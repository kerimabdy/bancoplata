package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class ProgressBarSize { Nav, Full }

private val TrackHeight = 8.dp
private val NavWidth = 140.dp

/** The gradient spans the track, so a short fill reads blue (00e6fc01); sub-height fills are clipped slivers (537a4b5a). */
@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    size: ProgressBarSize = ProgressBarSize.Full,
    trackBrush: Brush? = null,
    showLabels: Boolean = false,
    leadingLabel: String = "",
    trailingLabel: String = "",
    labelGap: Dp = PlataTheme.spacing.s12,
) {
    val colors = PlataTheme.colors
    val track = trackBrush ?: SolidColor(if (size == ProgressBarSize.Nav) colors.accentTrack else colors.progressTrackFull)
    val shape = ContinuousRoundedShape(PlataTheme.radii.full)
    val fill = PlataTheme.gradients.progress
    val bar: @Composable (Modifier) -> Unit = { barModifier ->
        Box(
            barModifier
                .height(TrackHeight)
                .clip(shape)
                .background(track)
                .drawBehind {
                    val height = this.size.height
                    val width = this.size.width * progress.coerceIn(0f, 1f)
                    if (width >= height) {
                        drawRoundRect(fill, size = Size(width, height), cornerRadius = CornerRadius(height / 2))
                    } else {
                        drawRect(fill, size = Size(width, height))
                    }
                },
        )
    }
    when {
        size == ProgressBarSize.Nav -> bar(modifier.width(NavWidth))
        !showLabels -> bar(modifier.fillMaxWidth())
        else -> Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(labelGap)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PlataText(leadingLabel, PlataTheme.typography.subheadStrong, colors.textPrimary, maxLines = 1)
                PlataText(trailingLabel, PlataTheme.typography.subhead, colors.textSecondary, maxLines = 1)
            }
            bar(Modifier.fillMaxWidth())
        }
    }
}

@Preview
@Composable
internal fun ProgressBarNavPreview() = PlataTheme { ProgressBar(0.06f, size = ProgressBarSize.Nav) }

@Preview
@Composable
internal fun ProgressBarFullPreview() = PlataTheme {
    ProgressBar(0.002f, Modifier.width(313.dp), size = ProgressBarSize.Full)
}

@Preview
@Composable
internal fun ProgressBarLabelsPreview() = PlataTheme {
    ProgressBar(
        0.35f,
        Modifier.width(313.dp),
        trackBrush = PlataTheme.gradients.progressTrackTinted,
        showLabels = true,
        leadingLabel = "Already 2.00",
        trailingLabel = "998.00 left",
    )
}
