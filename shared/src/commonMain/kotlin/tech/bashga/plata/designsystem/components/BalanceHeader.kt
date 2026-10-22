package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.GradientText
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.ChevronDisclosure
import tech.bashga.plata.designsystem.icons.Msi
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.SnowflakeSolid
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class BalanceHeaderSize { Large, Medium, Status }

enum class BalanceHeaderAlign { Leading, Center }

/** The trend line that replaces the sublink on the investment portfolio: "↑ USD 0.01 · 0.55% · All time" (b658c9da, 99ceac12). */
data class BalanceTrend(val value: String, val direction: TrendDirection, val caption: String? = null)

/**
 * Large: 17pt [caption], 36pt [amount], 17pt [sublink] with its [sublinkIcon] (5e61179c), or a [trend] line instead.
 * Large centred: amount over a text/primary sublink with a tertiary pencil 6 after it (b9c441c0). Medium: 20pt amount
 * over a 15pt caption (3b3eaed7). Status: [titleIcon] and the [amount] as a 20pt gradient title over the caption
 * (697dbf17). Rows sit 6 apart in every source. [trailing] centres on the amount row (Large: a whiteAccent xsmall
 * Button, a white 40 IconButton) or on the caption row (Medium, Status: a TextButton), without growing it.
 */
@Composable
fun BalanceHeader(
    amount: String,
    modifier: Modifier = Modifier,
    size: BalanceHeaderSize = BalanceHeaderSize.Large,
    align: BalanceHeaderAlign = BalanceHeaderAlign.Leading,
    caption: String? = null,
    sublink: String? = null,
    sublinkIcon: ImageVector? = if (align == BalanceHeaderAlign.Center) PlataIcons.Pencil else PlataIcons.ChevronDisclosure,
    onSublinkClick: (() -> Unit)? = null,
    trend: BalanceTrend? = null,
    titleIcon: ImageVector? = if (size == BalanceHeaderSize.Status) PlataIcons.SnowflakeSolid else null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val gap = Modifier.height(6.dp)
    val center = align == BalanceHeaderAlign.Center
    Column(modifier, horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start) {
        when (size) {
            BalanceHeaderSize.Large -> {
                if (caption != null) {
                    PlataText(caption, typography.body, colors.textPrimary, maxLines = 1)
                    Spacer(gap)
                }
                LineWithTrailing(typography.amountDisplay, if (center) null else trailing) {
                    PlataText(amount, typography.amountDisplay, colors.textPrimary, maxLines = 1)
                }
                when {
                    trend != null -> {
                        Spacer(gap)
                        TrendLabel(
                            trend.value,
                            trend.direction,
                            caption = trend.caption,
                            textStyle = typography.body,
                            captionColor = colors.textPrimary,
                            arrowSize = 18.dp,
                        )
                    }
                    sublink != null -> {
                        Spacer(gap)
                        val linkColor = if (center) colors.textPrimary else colors.textSecondary
                        Row(
                            if (onSublinkClick != null) Modifier.pressable(onClick = onSublinkClick) else Modifier,
                            horizontalArrangement = Arrangement.spacedBy(if (center) 6.dp else 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PlataText(sublink, typography.body, linkColor, maxLines = 1)
                            if (sublinkIcon != null && center) {
                                Icon(sublinkIcon, size = PlataTheme.sizes.iconXSmall, tint = colors.textTertiary)
                            } else if (sublinkIcon != null) {
                                // disclosure at 20: 5×9.4 ink at a 1.6 stroke, its ink 4.6 after the text and on the x-height like 5e61179c
                                Icon(sublinkIcon, Modifier.offset((-2).dp, 1.5.dp), size = PlataTheme.sizes.iconSmall, tint = colors.textSecondary)
                            }
                        }
                    }
                }
            }
            BalanceHeaderSize.Medium -> {
                PlataText(amount, typography.value, colors.textPrimary, maxLines = 1)
                if (caption != null) {
                    Spacer(gap)
                    LineWithTrailing(typography.subhead, trailing) {
                        PlataText(caption, typography.subhead, colors.textPrimary, maxLines = 1)
                    }
                }
            }
            BalanceHeaderSize.Status -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (titleIcon != null) Icon(titleIcon, size = PlataTheme.sizes.iconSmall, tint = colors.accent)
                    GradientText(amount, typography.title3, PlataTheme.gradients.titleViolet, maxLines = 1)
                }
                if (caption != null) {
                    Spacer(gap)
                    LineWithTrailing(typography.subhead, trailing) {
                        PlataText(caption, typography.subhead, colors.textPrimary, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun LineWithTrailing(style: TextStyle, trailing: (@Composable RowScope.() -> Unit)?, content: @Composable () -> Unit) {
    if (trailing == null) {
        content()
        return
    }
    val lineBox = with(LocalDensity.current) { style.lineHeight.toDp() }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f)) { content() }
        Row(Modifier.height(lineBox).wrapContentHeight(unbounded = true), verticalAlignment = Alignment.CenterVertically, content = trailing)
    }
}

@Preview
@Composable
internal fun BalanceHeaderLargePreview() = PlataTheme {
    BalanceHeader(
        "$3,457.97",
        Modifier.fillMaxWidth(),
        caption = "Available in Plata Crédito",
        sublink = "$42.03 Total balance",
        onSublinkClick = {},
        trailing = { Button("Withdraw", {}, style = ButtonStyle.WhiteAccent, size = ButtonSize.XSmall, icon = PlataIcons.Msi) },
    )
}

@Preview
@Composable
internal fun BalanceHeaderLargeTrendPreview() = PlataTheme {
    BalanceHeader(
        "USD 2.01",
        Modifier.fillMaxWidth(),
        caption = "Investment portfolio",
        trend = BalanceTrend("USD 0.01 · 0.55%", TrendDirection.Up, "· All time"),
        trailing = { IconButton(PlataIcons.Pie, {}, style = IconButtonStyle.White, size = IconButtonSize.S40) },
    )
}

@Preview
@Composable
internal fun BalanceHeaderLargeTrendDownPreview() = PlataTheme {
    BalanceHeader(
        "USD 1.00",
        Modifier.fillMaxWidth(),
        caption = "Investment portfolio",
        trend = BalanceTrend("USD 0.00 · 0.09%", TrendDirection.Down, "· All time"),
        trailing = { IconButton(PlataIcons.Pie, {}, style = IconButtonStyle.White, size = IconButtonSize.S40) },
    )
}

@Preview
@Composable
internal fun BalanceHeaderLargeCenterPreview() = PlataTheme {
    BalanceHeader("USD 2.00", Modifier.fillMaxWidth(), align = BalanceHeaderAlign.Center, sublink = "Moderate profile", onSublinkClick = {})
}

@Preview
@Composable
internal fun BalanceHeaderMediumPreview() = PlataTheme {
    BalanceHeader(
        "$3,500",
        Modifier.fillMaxWidth(),
        size = BalanceHeaderSize.Medium,
        caption = "No spending limit",
        trailing = { TextButton("Set limit", {}, weight = TextButtonWeight.Semibold, size = TextButtonSize.Subhead) },
    )
}

@Preview
@Composable
internal fun BalanceHeaderStatusPreview() = PlataTheme {
    BalanceHeader("Frozen", Modifier.fillMaxWidth(), size = BalanceHeaderSize.Status, caption = "Charges can’t be made to your card")
}
