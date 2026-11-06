package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_adidas
import plata.shared.generated.resources.raster_logo_booking
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.GradientText
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.tilePressable
import tech.bashga.plata.designsystem.theme.PlataTheme

/** [Percent]: the cashback rate in the brand gradient, top-aligned with the name. [Minimum]: an amount over [PartnerCard]'s caption. */
enum class PartnerCardTrailing { Percent, Minimum }

/** White 353×80 partner card: 40 [logo], name over category, cashback rate or minimum amount on the right. */
@Composable
fun PartnerCard(
    name: String,
    category: String,
    value: String,
    modifier: Modifier = Modifier,
    trailing: PartnerCardTrailing = PartnerCardTrailing.Percent,
    caption: String = "Minimum amount",
    onClick: (() -> Unit)? = null,
    logo: @Composable () -> Unit,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val spacing = PlataTheme.spacing
    val shape = ContinuousRoundedShape(PlataTheme.radii.xl)
    Row(
        modifier
            .tilePressable(onClick)
            .background(colors.surfaceCard, shape)
            .heightIn(min = PlataTheme.sizes.rowAsset)
            .padding(horizontal = spacing.cardPadding, vertical = spacing.s12),
        horizontalArrangement = Arrangement.spacedBy(spacing.rowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        logo()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            PlataText(name, typography.body, colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            PlataText(category, typography.footnote, colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        when (trailing) {
            // the rate shares the name's line, so the column keeps an empty footnote line under it (917b0cb2)
            PartnerCardTrailing.Percent -> Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                GradientText(value, typography.headline, PlataTheme.gradients.brand, maxLines = 1)
                PlataText("", typography.footnote, colors.textSecondary)
            }
            PartnerCardTrailing.Minimum -> Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                PlataText(value, typography.body, colors.textPrimary, maxLines = 1)
                PlataText(caption, typography.footnote, colors.textSecondary, maxLines = 1)
            }
        }
    }
}

@Preview
@Composable
internal fun PartnerCardPercentPreview() = PlataTheme {
    PartnerCard("Booking", "Travel", "4%", Modifier.width(353.dp)) {
        BrandLogo(painterResource(Res.drawable.raster_logo_booking), BrandLogoSize.S40)
    }
}

@Preview
@Composable
internal fun PartnerCardMinimumPreview() = PlataTheme {
    PartnerCard("Adidas", "Sporting goods", "$300", Modifier.width(353.dp), trailing = PartnerCardTrailing.Minimum) {
        BrandLogo(painterResource(Res.drawable.raster_logo_adidas), BrandLogoSize.S40)
    }
}
