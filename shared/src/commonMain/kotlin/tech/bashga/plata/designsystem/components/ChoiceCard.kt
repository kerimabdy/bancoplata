package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.tilePressable
import tech.bashga.plata.designsystem.icons.ClappingHands
import tech.bashga.plata.designsystem.icons.DollarCircle
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.Smile
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class ChoiceCardLayout { Tile, Row, Centered }

/**
 * Tile: 70 tall, radius 16, 15 semibold title over a 13pt subtitle (24f9ddbc). Row: 72 tall, radius 20, 28pt accent icon,
 * 17pt title, semibold value (3cbc958b; [valueMuted] greys the "Custom amount" placeholder). Centered: 56 tall, radius 16,
 * 17pt label (586ac28a, regular in the source; the inventory's semibold doesn't match). Selected adds a 2pt accent ring.
 */
@Composable
fun ChoiceCard(
    title: String,
    modifier: Modifier = Modifier,
    layout: ChoiceCardLayout = ChoiceCardLayout.Row,
    selected: Boolean = false,
    subtitle: String? = null,
    icon: ImageVector? = null,
    valueMuted: Boolean = false,
    onClick: () -> Unit = {},
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val radius: Dp = if (layout == ChoiceCardLayout.Row) PlataTheme.radii.xl else PlataTheme.radii.lg
    val shape = ContinuousRoundedShape(radius)
    val frame = modifier
        .tilePressable(onClick)
        .background(colors.surfaceCard, shape)
        .then(if (selected) Modifier.border(2.dp, colors.accent, shape) else Modifier)
    when (layout) {
        ChoiceCardLayout.Tile -> Column(
            frame.height(70.dp).padding(horizontal = PlataTheme.spacing.s12, vertical = 11.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            PlataText(title, typography.subheadTightStrong, colors.textPrimary, maxLines = 1)
            if (subtitle != null) PlataText(subtitle, typography.footnote, colors.textPrimary, maxLines = 1)
        }
        ChoiceCardLayout.Row -> Row(
            frame.height(72.dp).padding(start = 26.dp, end = PlataTheme.spacing.s20),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, size = PlataTheme.sizes.iconLarge, tint = colors.accent)
                Spacer(Modifier.width(18.dp))
            }
            PlataText(title, typography.body, colors.textPrimary, Modifier.weight(1f), maxLines = 1)
            if (subtitle != null) PlataText(subtitle, typography.headline, if (valueMuted) colors.textSecondary else colors.textPrimary, maxLines = 1)
        }
        ChoiceCardLayout.Centered -> Box(
            frame.height(PlataTheme.sizes.rowSingle).padding(horizontal = PlataTheme.spacing.s20),
            contentAlignment = Alignment.Center,
        ) {
            PlataText(title, typography.body, colors.textPrimary, Modifier.fillMaxWidth(), textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

@Preview
@Composable
internal fun ChoiceCardTilePreview() = PlataTheme {
    ChoiceCard("Today", Modifier.width(110.dp), layout = ChoiceCardLayout.Tile, subtitle = "Fri, 23 May")
}

@Preview
@Composable
internal fun ChoiceCardTileSelectedPreview() = PlataTheme {
    ChoiceCard("Closest", Modifier.width(110.dp), layout = ChoiceCardLayout.Tile, selected = true, subtitle = "Tue, 27 May")
}

@Preview
@Composable
internal fun ChoiceCardRowPreview() = PlataTheme {
    ChoiceCard("All good!", Modifier.fillMaxWidth(), icon = PlataIcons.Smile, subtitle = "$20")
}

@Preview
@Composable
internal fun ChoiceCardRowSelectedPreview() = PlataTheme {
    ChoiceCard("Thank you!", Modifier.fillMaxWidth(), selected = true, icon = PlataIcons.Heart, subtitle = "$30")
}

@Preview
@Composable
internal fun ChoiceCardRowListPreview() = PlataTheme {
    Column(verticalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s20)) {
        ChoiceCard("All good!", Modifier.fillMaxWidth(), icon = PlataIcons.Smile, subtitle = "$20")
        ChoiceCard("Thank you!", Modifier.fillMaxWidth(), selected = true, icon = PlataIcons.Heart, subtitle = "$30")
        ChoiceCard("Love it!", Modifier.fillMaxWidth(), icon = PlataIcons.ClappingHands, subtitle = "$50")
        ChoiceCard("Custom amount", Modifier.fillMaxWidth(), icon = PlataIcons.DollarCircle, subtitle = "$100", valueMuted = true)
    }
}

@Preview
@Composable
internal fun ChoiceCardCenteredPreview() = PlataTheme {
    ChoiceCard("Inconvenient delivery time", Modifier.fillMaxWidth(), layout = ChoiceCardLayout.Centered)
}
