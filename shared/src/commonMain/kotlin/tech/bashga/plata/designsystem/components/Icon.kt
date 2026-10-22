package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import tech.bashga.plata.designsystem.foundation.brushTint
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.isMulticolour
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class IconSize { Small, Medium, Large }

val IconSize.dp: Dp
    @Composable get() = when (this) {
        IconSize.Small -> PlataTheme.sizes.iconSmall
        IconSize.Medium -> PlataTheme.sizes.iconMedium
        IconSize.Large -> PlataTheme.sizes.iconLarge
    }

/** [size] scales the 24pt grid, so glyphs on other frames keep their proportions; multicolour glyphs ignore [tint] and [brush]. */
@Composable
fun Icon(
    glyph: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = PlataTheme.sizes.iconMedium,
    tint: Color = PlataTheme.colors.textPrimary,
    brush: Brush? = null,
    contentDescription: String? = null,
) {
    val sized = modifier.size(size * glyph.defaultWidth.value / 24f, size * glyph.defaultHeight.value / 24f)
    val multicolour = glyph.isMulticolour
    Image(
        painter = rememberVectorPainter(glyph),
        contentDescription = contentDescription,
        modifier = if (brush != null && !multicolour) sized.brushTint(brush) else sized,
        colorFilter = if (multicolour || brush != null) null else ColorFilter.tint(tint),
    )
}

@Composable
fun Icon(
    glyph: ImageVector,
    size: IconSize,
    modifier: Modifier = Modifier,
    tint: Color = PlataTheme.colors.textPrimary,
    brush: Brush? = null,
    contentDescription: String? = null,
) = Icon(glyph, modifier, size.dp, tint, brush, contentDescription)

@Preview
@Composable
internal fun IconSmallPreview() = PlataTheme {
    Icon(PlataIcons.Percent, IconSize.Small, tint = PlataTheme.colors.textPrimary)
}

@Preview
@Composable
internal fun IconMediumPreview() = PlataTheme {
    Icon(PlataIcons.Appearance, IconSize.Medium, tint = PlataTheme.colors.textPrimary)
}

@Preview
@Composable
internal fun IconLargePreview() = PlataTheme {
    Icon(PlataIcons.DoubleChevron, IconSize.Large, tint = PlataTheme.colors.textSecondary)
}

@Preview
@Composable
internal fun IconTintsPreview() = PlataTheme {
    val colors = PlataTheme.colors
    Row(
        Modifier.background(colors.surfaceCard).padding(PlataTheme.spacing.s8),
        horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(PlataIcons.House, IconSize.Medium, tint = colors.textPrimary)
        Icon(PlataIcons.House, IconSize.Medium, tint = colors.textSecondary)
        Icon(PlataIcons.House, IconSize.Medium, tint = colors.accent)
        Icon(PlataIcons.Document, IconSize.Medium, brush = PlataTheme.gradients.glyph)
        Icon(PlataIcons.Percent, IconSize.Medium, brush = PlataTheme.gradients.brand)
    }
}
