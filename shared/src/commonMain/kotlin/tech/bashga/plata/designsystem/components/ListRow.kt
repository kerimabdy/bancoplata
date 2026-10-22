package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_telcel
import plata.shared.generated.resources.raster_logo_vanguard
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.Baby
import tech.bashga.plata.designsystem.icons.Crown
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.QuestionMark
import tech.bashga.plata.designsystem.icons.Reorder
import tech.bashga.plata.designsystem.icons.Times2
import tech.bashga.plata.designsystem.icons.Transfer
import tech.bashga.plata.designsystem.theme.PlataTheme

/** [Plain] sits inside a ListGroup, SectionCard or sheet; [Grey] is a standalone 70-high row card on the page (cc4f1c47). */
enum class ListRowSurface { Plain, Grey }

// iOS edit controls, measured on 2be9ff97: the minus disc starts 14 from the edge and the logo 18 after it;
// the reorder bars end 17.3 from the edge, i.e. the 24 glyph box 16 in.
private val EditMinusSize = 28.dp
private val EditMinusInset = 14.dp
private val EditMinusGap = 18.dp

/**
 * Settings-style row: optional [leading] (Icon 24, IconCircle, IconTile, BrandLogo, a benefit glyph), [title] with an
 * optional [subtitle], an optional [value] in text/secondary, then [trailing] (Toggle, CheckCircle, IconButton, a Row
 * of them) and/or the disclosure chevron. Single rows are 56, rows with a subtitle 68 ([Grey] 70); longer titles
 * wrap and the row grows with 12 above and below the text, the trailing element staying centred.
 *
 * [editing] is the iOS edit mode: a red minus disc ([onDelete]) before the leading slot and a reorder handle in place
 * of the trailing content. [horizontalPadding] is 20 in cards; sheets that inset their rows pass 0.
 * [subtitleColor] is text/secondary on most screens, text/primary in the Payments sheet (e94a49a9).
 * Benefit glyphs (crown, ×2, ×3 at 32) go in [ListRowGlyphSlot] so titles line up with 40 tiles (787363a9).
 * [textMaxWidth] caps the title/subtitle column where a screen wraps text short of the trailing edge (200 on 787363a9: SF breaks the ×2 row after "instead" and keeps "instead of" on the ×3 row).
 */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    showChevron: Boolean = false,
    enabled: Boolean = true,
    surface: ListRowSurface = ListRowSurface.Plain,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    editing: Boolean = false,
    onDelete: () -> Unit = {},
    horizontalPadding: Dp = PlataTheme.spacing.s20,
    titleStyle: TextStyle = PlataTheme.typography.body,
    subtitleStyle: TextStyle = PlataTheme.typography.footnote,
    subtitleColor: Color = PlataTheme.colors.textSecondary,
    textMaxWidth: Dp = Dp.Unspecified,
) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    val minHeight = when {
        surface == ListRowSurface.Grey -> 70.dp
        subtitle != null -> 68.dp
        else -> PlataTheme.sizes.rowSingle
    }
    val end = if (editing) spacing.s16 else horizontalPadding
    Row(
        modifier
            .alpha(if (enabled) 1f else 0.3f)
            .then(
                if (surface == ListRowSurface.Grey) {
                    Modifier.background(colors.surfaceField, ContinuousRoundedShape(PlataTheme.radii.xl))
                } else {
                    Modifier
                },
            )
            .then(if (onClick != null) Modifier.pressable(enabled = enabled, onClick = onClick) else Modifier)
            .heightIn(min = minHeight)
            .padding(PaddingValues(start = if (editing) EditMinusInset else horizontalPadding, end = end)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (editing) {
            Icon(
                PlataIcons.MinusCircle,
                Modifier.pressable(onClickLabel = "Delete", onClick = onDelete),
                size = EditMinusSize,
                tint = colors.dangerSystem,
            )
            Spacer(Modifier.width(EditMinusGap))
        }
        if (leading != null) {
            leading()
            Spacer(Modifier.width(PlataTheme.spacing.rowGap))
        }
        // only the text keeps 12 above and below, so a 40 leading tile still fits the 56 row (84e28520)
        Column(Modifier.weight(1f).padding(vertical = spacing.s12), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val cap = Modifier.widthIn(max = textMaxWidth)
            PlataText(title, titleStyle, colors.textPrimary, cap)
            if (subtitle != null) PlataText(subtitle, subtitleStyle, subtitleColor, cap)
        }
        if (editing) {
            Spacer(Modifier.width(spacing.s12))
            Icon(PlataIcons.Reorder, IconSize.Medium, tint = colors.controlOff, contentDescription = "Reorder")
            return@Row
        }
        if (value != null) {
            Spacer(Modifier.width(spacing.s12))
            PlataText(value, PlataTheme.typography.body, colors.textSecondary, maxLines = 1)
        }
        if (trailing != null) {
            Spacer(Modifier.width(spacing.s16))
            trailing()
        }
        if (showChevron) {
            Spacer(Modifier.width(spacing.s8))
            // the chevron ink ends right at the padding (59741a55, cc4f1c47, e94a49a9), so shift its frame's 5pt inset out
            Chevron(ChevronDirection.Right, Modifier.offset(x = 5.dp))
        }
    }
}

/**
 * A 40 leading slot with [glyph] centred at [size], for rows whose leading is a bare glyph beside tile rows.
 * [offsetY] nudges the drawing only: the benefit glyphs on 787363a9 sit 2–3 above the text block's centre.
 */
@Composable
fun ListRowGlyphSlot(glyph: ImageVector, modifier: Modifier = Modifier, size: Dp = 32.dp, offsetY: Dp = 0.dp) {
    Box(modifier.size(PlataTheme.sizes.leadingTile), contentAlignment = Alignment.Center) {
        Icon(glyph, Modifier.wrapContentSize(unbounded = true).offset(y = offsetY), size = size)
    }
}

@Preview
@Composable
internal fun ListRowSingleDefaultPreview() = PlataTheme {
    ListRow(
        "Appearance",
        Modifier.width(353.dp),
        value = "System",
        showChevron = true,
        leading = { Icon(PlataIcons.Appearance, IconSize.Medium) },
    )
}

@Preview
@Composable
internal fun ListRowDoubleDefaultPreview() = PlataTheme {
    ListRow(
        "Transfer",
        Modifier.width(353.dp),
        subtitle = "By CLABE or card number",
        showChevron = true,
        horizontalPadding = 0.dp,
        subtitleColor = PlataTheme.colors.textPrimary,
        leading = { IconCircle(PlataIcons.Transfer, IconCircleSize.S40) },
    )
}

@Preview
@Composable
internal fun ListRowSingleDisabledPreview() = PlataTheme {
    ListRow(
        "5% Children's stores",
        Modifier.width(393.dp),
        enabled = false,
        leading = { IconTile(PlataIcons.Baby, fill = PlataTheme.gradients.categoryPink) },
        trailing = {
            Row(horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s16), verticalAlignment = Alignment.CenterVertically) {
                IconButton(PlataIcons.QuestionMark, {}, size = IconButtonSize.S20)
                CheckCircle(CheckCircleState.Unchecked)
            }
        },
    )
}

@Preview
@Composable
internal fun ListRowDoubleGreyPreview() = PlataTheme {
    ListRow(
        "Vanguard FTSE Europe ETF",
        Modifier.width(353.dp),
        subtitle = "VGK",
        showChevron = true,
        surface = ListRowSurface.Grey,
        leading = { BrandLogo(painterResource(Res.drawable.raster_logo_vanguard), BrandLogoSize.S40) },
    )
}

@Preview
@Composable
internal fun ListRowEditingPreview() = PlataTheme {
    ListRow(
        "My mobile",
        Modifier.width(393.dp),
        subtitle = "55 1234 5678",
        editing = true,
        leading = { BrandLogo(painterResource(Res.drawable.raster_logo_telcel), BrandLogoSize.S40) },
    )
}

@Preview
@Composable
internal fun ListRowBenefitPreview() = PlataTheme {
    ListRow(
        "Double cashback",
        Modifier.width(353.dp),
        subtitle = "Get 1% on all purchases instead of 0.5%",
        showChevron = true,
        subtitleStyle = PlataTheme.typography.footnoteTight,
        textMaxWidth = 200.dp,
        leading = { ListRowGlyphSlot(PlataIcons.Times2, offsetY = (-3).dp) },
    )
}

@Preview
@Composable
internal fun ListRowCheckPreview() = PlataTheme {
    ListRow(
        "I am senior executive at or a 10% or greater shareholder of a publicly traded company",
        Modifier.width(393.dp),
        trailing = { CheckCircle(CheckCircleState.Unchecked) },
    )
}

@Preview
@Composable
internal fun ListRowCrownPreview() = PlataTheme {
    ListRow(
        "10 cashback categories",
        Modifier.width(353.dp),
        subtitle = "Instead of 7 each month",
        showChevron = true,
        subtitleStyle = PlataTheme.typography.footnoteTight,
        textMaxWidth = 200.dp,
        leading = { ListRowGlyphSlot(PlataIcons.Crown, offsetY = (-2.2).dp) },
    )
}
