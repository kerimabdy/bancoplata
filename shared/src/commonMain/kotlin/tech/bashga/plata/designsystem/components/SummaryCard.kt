package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.ArrowUpRightDuotone
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.PlusOutlineDuotone
import tech.bashga.plata.designsystem.theme.PlataTheme
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_microsoft

/** [Center] centres the trailing slot on the title block; [Top] centres it on the title line ("Details", 5e61179c). */
enum class SummaryCardTrailingAlignment { Center, Top }

// title box bottom → subtitle box top on 025e71c5; CreditSummaryCard's amount → caption measures the same
internal val SummaryTitleSubtitleGap = 3.dp

/**
 * White r20 card, padding 20: 20 bold [title] over a 15/18 [subtitle] (86 high with one line, 025e71c5; 104 with two,
 * 5e61179c; b658c9da and d7d78e0b set it 17/20, `typography.bodyCompact`), with a [trailing] slot (SpendOrb,
 * CardThumb, Chevron, IconButton pair, IconCircle, TextButton) and an optional [body] 16 below the text (TextField + share button on 5e61179c, AmountRow on b658c9da).
 * The centred trailing slot never grows the card. [glass] draws it over Home's hero wave, as ShortcutTile does.
 */
@Composable
fun SummaryCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    subtitleStyle: TextStyle = PlataTheme.typography.subheadTight,
    trailingAlignment: SummaryCardTrailingAlignment = SummaryCardTrailingAlignment.Center,
    contentPadding: PaddingValues = PaddingValues(PlataTheme.spacing.cardPadding),
    bodyGap: Dp = PlataTheme.spacing.s16,
    glass: Boolean = false,
    body: (@Composable ColumnScope.() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val ink = if (glass) colors.textOnDark else colors.textPrimary
    val shape = ContinuousRoundedShape(PlataTheme.radii.xl)
    Column(
        modifier
            .fillMaxWidth()
            .then(if (glass) Modifier.background(colors.glassTile, shape).border(1.dp, colors.glassTileBorder, shape) else Modifier.background(colors.surfaceCard, shape))
            .then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier)
            .padding(contentPadding),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s12),
            verticalAlignment = if (trailingAlignment == SummaryCardTrailingAlignment.Top) Alignment.Top else Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                PlataText(title, typography.title3, ink)
                if (subtitle != null) {
                    Spacer(Modifier.height(SummaryTitleSubtitleGap))
                    PlataText(subtitle, subtitleStyle, ink)
                }
            }
            if (trailing != null) {
                if (trailingAlignment == SummaryCardTrailingAlignment.Top) {
                    val titleLine = with(LocalDensity.current) { typography.title3.lineHeight.toDp() }
                    Box(Modifier.height(titleLine), contentAlignment = Alignment.CenterEnd) { trailing() }
                } else {
                    Box(Modifier.heightNeutral()) { trailing() }
                }
            }
        }
        if (body != null) {
            Spacer(Modifier.height(bodyGap))
            body()
        }
    }
}

// the 48 SpendOrb overhangs the 46 text block on 025e71c5 without growing the 86 card
private fun Modifier.heightNeutral(): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
    layout(placeable.width, 0) { placeable.place(0, -placeable.height / 2) }
}

@Preview
@Composable
internal fun SummaryCardDefaultPreview() = PlataTheme {
    SummaryCard("Transactions", Modifier.width(353.dp), subtitle = "$70 Spent in June", onClick = {}) {
        SpendOrb(SpendOrbState.Active)
    }
}

@Preview
@Composable
internal fun SummaryCardChevronPreview() = PlataTheme {
    SummaryCard("Transactions", Modifier.width(353.dp), subtitle = "3 in July", subtitleStyle = PlataTheme.typography.bodyCompact, onClick = {}) {
        Chevron(ChevronDirection.Right)
    }
}

@Preview
@Composable
internal fun SummaryCardActionsPreview() = PlataTheme {
    SummaryCard("Cash", Modifier.width(353.dp), subtitle = "USD 1.00", subtitleStyle = PlataTheme.typography.bodyCompact) {
        Row(horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s8)) {
            IconButton(PlataIcons.PlusOutlineDuotone, {}, style = IconButtonStyle.Tinted, size = IconButtonSize.S48)
            IconButton(PlataIcons.ArrowUpRightDuotone, {}, style = IconButtonStyle.Tinted, size = IconButtonSize.S48)
        }
    }
}

@Preview
@Composable
internal fun SummaryCardIconPreview() = PlataTheme {
    SummaryCard("Bring your INE", Modifier.width(353.dp), subtitle = "To verify your identity") {
        IconCircle(PlataIcons.Ine, IconCircleSize.S40, tint = PlataTheme.colors.textPrimary)
    }
}

@Preview
@Composable
internal fun SummaryCardDetailsPreview() = PlataTheme {
    SummaryCard(
        "Credit payment",
        Modifier.width(353.dp),
        subtitle = "Everything is paid.\nStatement expected on Aug 6",
        trailingAlignment = SummaryCardTrailingAlignment.Top,
    ) {
        TextButton("Details", {}, weight = TextButtonWeight.Semibold)
    }
}

@Preview
@Composable
internal fun SummaryCardBodyPreview() = PlataTheme {
    SummaryCard("Your stocks", Modifier.width(353.dp), subtitle = "USD 1.01", subtitleStyle = PlataTheme.typography.bodyCompact, body = {
        AmountRow(
            "Microsoft",
            "USD 1.01",
            subtitle = "0.31 MSFT",
            secondary = "1.10%",
            secondaryTone = AmountSecondaryTone.Positive,
        ) { BrandLogo(painterResource(Res.drawable.raster_logo_microsoft), BrandLogoSize.S40, light = true) }
    })
}
