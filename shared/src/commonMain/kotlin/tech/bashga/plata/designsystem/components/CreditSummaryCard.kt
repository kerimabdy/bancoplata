package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme

/**
 * Home credit card (025e71c5): 20 bold [amount] over a 15/18 [caption] 3 below (as SummaryCard), 13 [status]
 * top-right in text/secondary, a row of CardThumbs ([cards], 12 apart) and an InsetRow link ([rowLabel] / [rowAction];
 * none when [rowLabel] is null).
 * [sectionGap] separates the three blocks: 20 on 025e71c5 and 9f1aa9fc, 16–18 on 9533117e and f5db1665.
 */
@Composable
fun CreditSummaryCard(
    amount: String,
    caption: String,
    modifier: Modifier = Modifier,
    status: String? = null,
    rowLabel: String? = "Payment dates",
    rowAction: String = "Show",
    onRowAction: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(PlataTheme.spacing.cardPadding),
    sectionGap: Dp = PlataTheme.spacing.s20,
    cards: @Composable RowScope.() -> Unit = { DefaultCreditCards() },
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    Column(
        modifier
            .fillMaxWidth()
            .background(colors.surfaceCard, ContinuousRoundedShape(PlataTheme.radii.xl))
            .padding(contentPadding),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                PlataText(amount, typography.title3, colors.textPrimary, maxLines = 1)
                Spacer(Modifier.height(SummaryTitleSubtitleGap))
                PlataText(caption, typography.subheadTight, colors.textPrimary)
            }
            if (status != null) PlataText(status, typography.footnote, colors.textSecondary, maxLines = 1)
        }
        Spacer(Modifier.height(sectionGap))
        Row(horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s12), content = cards)
        if (rowLabel != null) {
            Spacer(Modifier.height(sectionGap))
            InsetRow(rowLabel, rowAction, Modifier.fillMaxWidth(), onValueClick = onRowAction)
        }
    }
}

@Composable
private fun DefaultCreditCards() {
    CardThumb(CardThumbKind.Physical)
    CardThumb(CardThumbKind.Digital)
    CardThumb(CardThumbKind.Add)
}

@Preview
@Composable
internal fun CreditSummaryCardDefaultPreview() = PlataTheme {
    CreditSummaryCard("$3,470", "Available in Plata Crédito", Modifier.width(353.dp), status = "Nothing due")
}
