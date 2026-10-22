package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class BadgeSize(val height: Dp) { Small(18.dp), Regular(20.dp) }

/** [outline] rings the badge in the surface it overlaps: the tab-bar badge on 9f1aa9fc cuts into the house glyph. */
@Composable
fun Badge(count: String, modifier: Modifier = Modifier, size: BadgeSize = BadgeSize.Regular, outline: Color? = null) {
    val colors = PlataTheme.colors
    val shape = ContinuousRoundedShape(PlataTheme.radii.full)
    Box(
        modifier
            .then(if (outline != null) Modifier.border(1.5.dp, outline, shape).padding(1.5.dp) else Modifier)
            .height(size.height)
            .defaultMinSize(minWidth = size.height)
            .background(if (size == BadgeSize.Small) colors.dangerSystem else colors.danger, shape)
            .padding(horizontal = PlataTheme.spacing.s4),
        contentAlignment = Alignment.Center,
    ) {
        PlataText(count, PlataTheme.typography.footnoteStrong, colors.textOnDark, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Preview
@Composable
internal fun BadgeSmallPreview() = PlataTheme { Badge("2", size = BadgeSize.Small, outline = PlataTheme.colors.surfaceBackground) }

@Preview
@Composable
internal fun BadgeRegularPreview() = PlataTheme { Badge("3") }

@Preview
@Composable
internal fun BadgeWidePreview() = PlataTheme { Badge("12") }
