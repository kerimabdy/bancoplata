package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.GradientText
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.tilePressable
import tech.bashga.plata.designsystem.icons.Msi
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/**
 * [Regular]: the Home shortcuts, min 80 high, 15pt subtitle (025e71c5, 9f1aa9fc). [Compact]: 72 high, 13pt subtitle,
 * the stacked pair beside a tall tile (894b1b01). [Tall]: 156 high with the [ShortcutTile] body at the bottom.
 */
enum class ShortcutTileSize { Regular, Compact, Tall }

enum class ShortcutTitleTone { Primary, Gradient, Success }

private class ShortcutMetrics(val padding: PaddingValues, val minHeight: Dp, val subtitle: TextStyle, val subtitleGap: Dp)

/**
 * White radius-20 shortcut. [title] is 17 semibold in [titleTone], optionally after a [titleIcon] (the MSI mark, drawn
 * in gradients.glyph) and followed by a gradient [titleSecondLine] ("Choose / cashback"). [badge] puts a count in the
 * top-right corner, [showChevron] a disclosure chevron level with the title. [body] holds an icon row or a SpendOrb.
 * [eyebrow] is a subtitle-style line above the title (a figure's label). [glass] draws the tile over Home's hero wave:
 * a white wash and rim, every line in white.
 */
@Composable
fun ShortcutTile(
    title: String,
    modifier: Modifier = Modifier,
    size: ShortcutTileSize = ShortcutTileSize.Regular,
    titleTone: ShortcutTitleTone = ShortcutTitleTone.Gradient,
    titleIcon: ImageVector? = null,
    titleSecondLine: String? = null,
    subtitle: String? = null,
    badge: String? = null,
    showChevron: Boolean = false,
    eyebrow: String? = null,
    glass: Boolean = false,
    onClick: (() -> Unit)? = null,
    body: (@Composable () -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val ink = if (glass) colors.textOnDark else colors.textPrimary
    val shape = ContinuousRoundedShape(PlataTheme.radii.xl)
    val typography = PlataTheme.typography
    val spacing = PlataTheme.spacing
    // measured: Home tiles pad 20 sides / 18 top / 16 bottom; the Credit tiles (894b1b01) pad 16 all round
    val metrics = when (size) {
        ShortcutTileSize.Regular -> ShortcutMetrics(
            PaddingValues(start = spacing.s20, end = spacing.s20, top = 18.dp, bottom = spacing.s16),
            PlataTheme.sizes.rowAsset,
            typography.subhead,
            2.dp,
        )
        ShortcutTileSize.Compact -> ShortcutMetrics(PaddingValues(spacing.s16), 72.dp, typography.footnote, 0.dp)
        ShortcutTileSize.Tall -> ShortcutMetrics(PaddingValues(spacing.s16), 156.dp, typography.footnote, 0.dp)
    }
    val frame = modifier.then(if (size == ShortcutTileSize.Tall) Modifier.height(metrics.minHeight) else Modifier.heightIn(min = metrics.minHeight))
    val tile: @Composable BoxScope.() -> Unit = {
        Column(Modifier.then(if (size == ShortcutTileSize.Tall) Modifier.fillMaxHeight() else Modifier).padding(metrics.padding)) {
            if (eyebrow != null) {
                PlataText(eyebrow, metrics.subtitle, ink, maxLines = 1)
                Spacer(Modifier.height(metrics.subtitleGap))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (titleIcon != null) {
                    if (glass) Icon(titleIcon, size = 17.dp, tint = ink) else Icon(titleIcon, size = 17.dp, brush = PlataTheme.gradients.glyph)
                    Spacer(Modifier.width(spacing.s4))
                }
                val style = typography.headline
                when {
                    glass -> PlataText(title, style, ink, maxLines = 1)
                    titleTone == ShortcutTitleTone.Gradient -> GradientText(title, style, PlataTheme.gradients.brand, maxLines = 1)
                    titleTone == ShortcutTitleTone.Primary -> PlataText(title, style, colors.textPrimary, maxLines = 1)
                    else -> PlataText(title, style, colors.textSuccess, maxLines = 1)
                }
                if (showChevron) {
                    Spacer(Modifier.weight(1f))
                    // like ListRow, the chevron ink ends at the padding, so its frame's 5pt inset hangs outside
                    Chevron(ChevronDirection.Right, Modifier.offset(x = 5.dp))
                }
            }
            if (titleSecondLine != null) {
                if (glass) PlataText(titleSecondLine, typography.headline, ink, maxLines = 1)
                else GradientText(titleSecondLine, typography.headline, PlataTheme.gradients.brand, maxLines = 1)
            }
            if (subtitle != null) {
                Spacer(Modifier.height(metrics.subtitleGap))
                PlataText(subtitle, metrics.subtitle, ink, maxLines = 1)
            }
            if (body != null) {
                if (size == ShortcutTileSize.Tall) Spacer(Modifier.weight(1f)) else Spacer(Modifier.height(spacing.s4))
                body()
            }
        }
        if (badge != null) {
            Badge(badge, Modifier.align(Alignment.TopEnd).padding(top = spacing.s12, end = spacing.s12))
        }
    }
    if (glass) {
        GlassSurface(colors.glassTile, frame.tilePressable(onClick), PlataTheme.radii.xl, colors.glassTileBorder, content = tile)
    } else {
        Box(frame.tilePressable(onClick).background(colors.surfaceCard, shape)) { tile() }
    }
}

/** The cashback-category glyphs under a Regular tile title: 20pt, 8 apart. */
@Composable
fun ShortcutTileGlyphRow(glyphs: List<ImageVector>, modifier: Modifier = Modifier, tint: Color = PlataTheme.colors.textPrimary) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s8)) {
        glyphs.forEach { Icon(it, size = PlataTheme.sizes.iconSmall, tint = tint) }
    }
}

@Preview
@Composable
internal fun ShortcutTileRegularPreview() = PlataTheme {
    ShortcutTile("Cashback", Modifier.width(168.dp)) {
        ShortcutTileGlyphRow(listOf(PlataIcons.Percent, PlataIcons.Plane, PlataIcons.Music, PlataIcons.ForkKnife))
    }
}

@Preview
@Composable
internal fun ShortcutTileMsiPreview() = PlataTheme {
    ShortcutTile(
        "MSI",
        Modifier.width(168.dp),
        titleTone = ShortcutTitleTone.Primary,
        titleIcon = PlataIcons.Msi,
        subtitle = "None to defer",
        badge = "1",
    )
}

@Preview
@Composable
internal fun ShortcutTileChoosePreview() = PlataTheme {
    ShortcutTile("Choose", Modifier.width(168.dp), titleTone = ShortcutTitleTone.Primary, titleSecondLine = "cashback", badge = "3")
}

@Preview
@Composable
internal fun ShortcutTileCompactPreview() = PlataTheme {
    ShortcutTile(
        "$40",
        Modifier.width(170.dp),
        size = ShortcutTileSize.Compact,
        titleTone = ShortcutTitleTone.Success,
        subtitle = "Received",
        showChevron = true,
    )
}

@Preview
@Composable
internal fun ShortcutTileTallPreview() = PlataTheme {
    ShortcutTile(
        "$70",
        Modifier.width(170.dp),
        size = ShortcutTileSize.Tall,
        titleTone = ShortcutTitleTone.Primary,
        subtitle = "Spent",
        showChevron = true,
    ) { SpendOrb(SpendOrbState.Active) }
}
