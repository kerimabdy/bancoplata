package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_logo_amazon_prime
import plata.shared.generated.resources.raster_logo_apple
import plata.shared.generated.resources.raster_logo_microsoft
import tech.bashga.plata.designsystem.foundation.GradientText
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.HouseDollar
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class AmountTone { Default, Positive }

/**
 * [Positive]/[Negative] draw a TrendLabel; [Neutral] is text/secondary ("with VAT", d58c1486), [Primary] text/primary
 * (the "−$0.20" fee under a reversal, 894b1b01), [Gradient] the brand-gradient cashback ("+$0.20").
 */
enum class AmountSecondaryTone { Positive, Negative, Neutral, Primary, Gradient }

/**
 * Asset or transaction row, 80 high: 40 [leading] (BrandLogo, IconCircle, IconTile), title over subtitle, amount over
 * a secondary line on the right. The title stays on one line (ellipsis) so long names don't push the amount.
 */
@Composable
fun AmountRow(
    title: String,
    amount: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    secondary: String? = null,
    amountTone: AmountTone = AmountTone.Default,
    secondaryTone: AmountSecondaryTone = AmountSecondaryTone.Neutral,
    horizontalPadding: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    minHeight: Dp = PlataTheme.sizes.rowAsset,
    subtitleStyle: TextStyle = PlataTheme.typography.footnote,
    leading: @Composable () -> Unit,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val spacing = PlataTheme.spacing
    Row(
        modifier
            .then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier)
            .heightIn(min = minHeight)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Spacer(Modifier.width(spacing.rowGap))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            PlataText(title, typography.body, colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) PlataText(subtitle, subtitleStyle, colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(spacing.s12))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            PlataText(amount, typography.body, if (amountTone == AmountTone.Positive) colors.textSuccess else colors.textPrimary, maxLines = 1)
            if (secondary != null) {
                when (secondaryTone) {
                    AmountSecondaryTone.Positive -> TrendLabel(secondary, TrendDirection.Up)
                    AmountSecondaryTone.Negative -> TrendLabel(secondary, TrendDirection.Down)
                    AmountSecondaryTone.Neutral -> PlataText(secondary, typography.subhead, colors.textSecondary, maxLines = 1)
                    AmountSecondaryTone.Primary -> PlataText(secondary, typography.subhead, colors.textPrimary, maxLines = 1)
                    AmountSecondaryTone.Gradient -> GradientText(secondary, typography.subhead, PlataTheme.gradients.brand, maxLines = 1)
                }
            }
        }
    }
}

@Preview
@Composable
internal fun AmountRowPositivePreview() = PlataTheme {
    AmountRow(
        "Microsoft",
        "USD 490.01",
        Modifier.width(353.dp),
        subtitle = "MSFT",
        secondary = "0.82%",
        secondaryTone = AmountSecondaryTone.Positive,
    ) { BrandLogo(painterResource(Res.drawable.raster_logo_microsoft), BrandLogoSize.S40, light = true) }
}

@Preview
@Composable
internal fun AmountRowNegativePreview() = PlataTheme {
    AmountRow(
        "Apple",
        "USD 200.71",
        Modifier.width(353.dp),
        subtitle = "AAPL",
        secondary = "0.39%",
        secondaryTone = AmountSecondaryTone.Negative,
    ) { BrandLogo(painterResource(Res.drawable.raster_logo_apple), BrandLogoSize.S40) }
}

@Preview
@Composable
internal fun AmountRowNeutralPreview() = PlataTheme {
    AmountRow(
        "Commission for operation",
        "−$230.84",
        Modifier.width(353.dp),
        subtitle = "Financial services",
        secondary = "with VAT",
    ) { IconCircle(PlataIcons.HouseDollar, IconCircleSize.S40, tone = IconCircleTone.Filled, fill = PlataTheme.gradients.violet) }
}

@Preview
@Composable
internal fun AmountRowIncomingPreview() = PlataTheme {
    AmountRow(
        "Reversal from Amazon Prime",
        "+$20",
        Modifier.width(353.dp),
        subtitle = "Incoming",
        secondary = "−$0.20",
        amountTone = AmountTone.Positive,
        secondaryTone = AmountSecondaryTone.Primary,
    ) { BrandLogo(painterResource(Res.drawable.raster_logo_amazon_prime), BrandLogoSize.S40) }
}

@Preview
@Composable
internal fun AmountRowCashbackPreview() = PlataTheme {
    AmountRow(
        "Amazon Prime",
        "−$20",
        Modifier.width(353.dp),
        subtitle = "Subscriptions",
        secondary = "+$0.20",
        secondaryTone = AmountSecondaryTone.Gradient,
    ) { BrandLogo(painterResource(Res.drawable.raster_logo_amazon_prime), BrandLogoSize.S40) }
}
