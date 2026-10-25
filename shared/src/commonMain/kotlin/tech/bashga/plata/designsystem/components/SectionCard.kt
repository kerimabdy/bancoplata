package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class SectionCardBorder { None, Gradient }

/**
 * White r20 card with an optional SectionHeader ([title], [titleEmphasis], [headerLeading]) and a [body] stack.
 * [headerTrailing] is the header's right side, centred on the title line: [SectionCardAction] ("Full info", ec4a6b30)
 * or [SectionCardAmount] ("$200", d2e81313). Without a [title] the card is body only (4e9baf69).
 * [headerGap] and [bodySpacing] vary per screen (header→body 23 on 22bf11f1, 22 on f1b38f8d, 6 on d2e81313).
 * [border] Gradient draws the 2pt pastel ring of the AI card (f1b38f8d) inside the card's bounds.
 * [headerPadding] pads the header alone: a card whose body scrolls sideways drops its side [contentPadding] so the
 * scroll runs to the card's edges, and gives the header that padding here and the scroll its own content padding.
 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    border: SectionCardBorder = SectionCardBorder.None,
    titleEmphasis: SectionHeaderEmphasis = SectionHeaderEmphasis.Plain,
    titleBrush: Brush? = null,
    headerLeading: (@Composable () -> Unit)? = null,
    headerTrailing: (@Composable RowScope.() -> Unit)? = null,
    headerGap: Dp = PlataTheme.spacing.s20,
    bodySpacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(PlataTheme.spacing.cardPadding),
    headerPadding: PaddingValues = PaddingValues(0.dp),
    body: @Composable ColumnScope.() -> Unit,
) {
    val shape = ContinuousRoundedShape(PlataTheme.radii.xl)
    Column(
        modifier
            .fillMaxWidth()
            .background(PlataTheme.colors.surfaceCard, shape)
            .then(if (border == SectionCardBorder.Gradient) Modifier.border(2.dp, PlataTheme.gradients.cardBorder, shape) else Modifier)
            .padding(contentPadding),
    ) {
        if (title != null) {
            SectionHeader(
                title,
                Modifier.fillMaxWidth().padding(headerPadding),
                emphasis = titleEmphasis,
                emphasisBrush = titleBrush,
                leading = headerLeading,
                trailing = headerTrailing,
            )
            Spacer(Modifier.height(headerGap))
        }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(bodySpacing), content = body)
    }
}

/** Header action: accent 17 semibold link ("Full info", "See all"). */
@Composable
fun SectionCardAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(label, onClick, modifier, weight = TextButtonWeight.Semibold)
}

/** Header amount: 17 semibold text/primary ("$200", "$3,722.03"). */
@Composable
fun SectionCardAmount(amount: String, modifier: Modifier = Modifier) {
    PlataText(amount, PlataTheme.typography.headline, PlataTheme.colors.textPrimary, modifier, maxLines = 1)
}

@Preview
@Composable
internal fun SectionCardNonePreview() = PlataTheme {
    val bold = SpanStyle(fontWeight = FontWeight.SemiBold)
    val items = listOf(
        buildAnnotatedString {
            append("We’ll send your statement ")
            withStyle(bold) { append("on Jul 8") }
        },
        buildAnnotatedString { append("Statement describes how much\nyou’ve spent") },
        buildAnnotatedString {
            append("You’ll have ")
            withStyle(bold) { append("a month to pay") }
            append("\nyour balance")
        },
        buildAnnotatedString { append("The following statements will be\ngenerated monthly") },
    )
    // 22bf11f1 spaces this card 23 under the header and 24 above the bottom edge
    SectionCard(
        Modifier.width(353.dp),
        title = "About payment dates",
        headerGap = 23.dp,
        bodySpacing = PlataTheme.spacing.s20,
        contentPadding = PaddingValues(start = PlataTheme.spacing.s20, top = PlataTheme.spacing.s20, end = PlataTheme.spacing.s20, bottom = PlataTheme.spacing.s24),
    ) {
        items.forEachIndexed { index, text -> BulletItem(text, marker = BulletMarker.Number, number = index + 1, markerAlignment = Alignment.CenterVertically) }
    }
}

@Preview
@Composable
internal fun SectionCardGradientPreview() = PlataTheme {
    SectionCard(
        Modifier.width(353.dp),
        title = "Portfolio analysis",
        border = SectionCardBorder.Gradient,
        titleEmphasis = SectionHeaderEmphasis.Full,
        headerLeading = { Icon(PlataIcons.AiBadge, size = PlataTheme.sizes.iconMedium) },
        headerGap = 22.dp,
    ) {
        PlataText(
            "AI analysis is for portfolios over\nUSD 1,000. Keep building your portfolio\nto unlock it.",
            PlataTheme.typography.bodyCompact,
            PlataTheme.colors.textPrimary,
        )
        Spacer(Modifier.height(PlataTheme.spacing.s20))
        ProgressBar(
            0.002f,
            Modifier.fillMaxWidth(),
            trackBrush = PlataTheme.gradients.progressTrackTinted,
            showLabels = true,
            leadingLabel = "Already 2.00",
            trailingLabel = "998.00 left",
            labelGap = 9.dp,
        )
        Spacer(Modifier.height(21.dp))
        Button("Add more cash", {}, Modifier.fillMaxWidth(), style = ButtonStyle.SecondaryOnCard)
    }
}

@Preview
@Composable
internal fun SectionCardAmountPreview() = PlataTheme {
    SectionCard(
        Modifier.width(353.dp),
        title = "Minimum",
        headerTrailing = { SectionCardAmount("$200") },
        headerGap = 6.dp,
    ) {
        PlataText(
            "Includes part of the period’s debt and lets\nyou avoid the late fees, but still generates\ninterest charges on the debt",
            PlataTheme.typography.subheadTight,
            PlataTheme.colors.textPrimary,
        )
        Spacer(Modifier.height(PlataTheme.spacing.s20))
        Button("Paid", {}, Modifier.fillMaxWidth(), style = ButtonStyle.Success, size = ButtonSize.Medium, icon = PlataIcons.Check)
    }
}

@Preview
@Composable
internal fun SectionCardActionPreview() = PlataTheme {
    SectionCard(
        Modifier.width(353.dp),
        title = "Card delivery",
        headerTrailing = { SectionCardAction("Full info", {}) },
    ) {
        ListRow("Delivery address", subtitle = "Calle Durango 263, Roma Norte", horizontalPadding = 0.dp)
    }
}
