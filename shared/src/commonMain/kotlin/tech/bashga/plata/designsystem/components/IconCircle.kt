package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.icons.DoubleChevronDuotone
import tech.bashga.plata.designsystem.icons.HouseDollar
import tech.bashga.plata.designsystem.icons.PhoneDuotone
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class IconCircleSize { S40, S56, S80 }

val IconCircleSize.diameter: Dp
    @Composable get() = when (this) {
        IconCircleSize.S40 -> PlataTheme.sizes.buttonSmall
        IconCircleSize.S56 -> 56.dp
        IconCircleSize.S80 -> 80.dp
    }

/** Figma puts a 28 glyph in the 56 circle; the source Bill payments circles (a5a647ee) draw 24. */
val IconCircleSize.glyph: Dp
    @Composable get() = when (this) {
        IconCircleSize.S40, IconCircleSize.S56 -> PlataTheme.sizes.iconMedium
        IconCircleSize.S80 -> 36.dp
    }

enum class IconCircleTone { Neutral, Success, Danger, Accent, Filled }

@Composable
fun IconCircle(
    size: IconCircleSize,
    modifier: Modifier = Modifier,
    tone: IconCircleTone = IconCircleTone.Neutral,
    fill: Brush? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = PlataTheme.colors
    val background = fill ?: SolidColor(
        when (tone) {
            IconCircleTone.Neutral -> colors.surfaceBackground
            IconCircleTone.Success -> colors.successTint
            IconCircleTone.Danger -> colors.dangerTint
            IconCircleTone.Accent -> colors.accentTint
            IconCircleTone.Filled -> colors.brandPrimary
        },
    )
    Box(
        modifier.size(size.diameter).background(background, CircleShape),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** [tint] overrides the tone's glyph colour (the neutral 80 thumbs-down on b95e6964 is text/secondary). */
@Composable
fun IconCircle(
    glyph: ImageVector,
    size: IconCircleSize,
    modifier: Modifier = Modifier,
    tone: IconCircleTone = IconCircleTone.Neutral,
    fill: Brush? = null,
    tint: Color? = null,
    contentDescription: String? = null,
) {
    val colors = PlataTheme.colors
    val toneColor = when (tone) {
        IconCircleTone.Neutral -> null
        IconCircleTone.Success -> colors.success
        IconCircleTone.Danger -> colors.danger
        IconCircleTone.Accent -> colors.accent
        IconCircleTone.Filled -> colors.textOnDark
    }
    IconCircle(size, modifier, tone, fill) {
        Icon(
            glyph,
            size = size.glyph,
            tint = tint ?: toneColor ?: colors.accent,
            brush = if (tint == null && toneColor == null) PlataTheme.gradients.glyph else null,
            contentDescription = contentDescription,
        )
    }
}

@Preview
@Composable
internal fun IconCircle40NeutralPreview() = PlataTheme {
    IconCircle(PlataIcons.DoubleChevronDuotone, IconCircleSize.S40)
}

@Preview
@Composable
internal fun IconCircle56NeutralPreview() = PlataTheme {
    IconCircle(PlataIcons.PhoneDuotone, IconCircleSize.S56)
}

@Preview
@Composable
internal fun IconCircle80NeutralPreview() = PlataTheme {
    IconCircle(PlataIcons.ThumbsDown, IconCircleSize.S80, tint = PlataTheme.colors.textSecondary)
}

@Preview
@Composable
internal fun IconCircle80SuccessPreview() = PlataTheme {
    IconCircle(PlataIcons.ThumbsUp, IconCircleSize.S80, tone = IconCircleTone.Success)
}

@Preview
@Composable
internal fun IconCircle40FilledPreview() = PlataTheme {
    IconCircle(PlataIcons.HouseDollar, IconCircleSize.S40, tone = IconCircleTone.Filled, fill = PlataTheme.gradients.violet)
}

@Preview
@Composable
internal fun IconCircleTonesPreview() = PlataTheme {
    Row(horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s8)) {
        IconCircle(PlataIcons.Check, IconCircleSize.S40, tone = IconCircleTone.Success)
        IconCircle(PlataIcons.Warning, IconCircleSize.S40, tone = IconCircleTone.Danger)
        IconCircle(PlataIcons.Info, IconCircleSize.S40, tone = IconCircleTone.Accent)
        IconCircle(PlataIcons.Bank, IconCircleSize.S40)
        IconCircle(PlataIcons.Plus, IconCircleSize.S40, tone = IconCircleTone.Filled)
    }
}
