package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressScale
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.Msi
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** [SecondaryOnCard] sits on white cards and sheets (surface/background fill); [Secondary] on the grey page. */
enum class ButtonStyle { Primary, Secondary, SecondaryOnCard, White, WhiteAccent, Black, Success, Destructive }

enum class ButtonSize { Large, Medium, Small, XSmall }

private class ButtonMetrics(
    val height: Dp,
    val radius: Dp,
    val start: Dp,
    val startWithIcon: Dp,
    val end: Dp,
    val gap: Dp,
    val icon: Dp,
    val label: TextStyle,
)

@Composable
private fun ButtonSize.metrics(style: ButtonStyle): ButtonMetrics {
    val spacing = PlataTheme.spacing
    val sizes = PlataTheme.sizes
    val radii = PlataTheme.radii
    val type = PlataTheme.typography
    return when (this) {
        ButtonSize.Large -> if (style == ButtonStyle.Black) {
            // Add to Apple Wallet (3b3eaed7): 20 semibold beside a 28×20 wallet
            ButtonMetrics(sizes.buttonLarge, radii.lg, spacing.s20, spacing.s20, spacing.s20, spacing.s8, sizes.iconLarge, type.value)
        } else {
            ButtonMetrics(sizes.buttonLarge, radii.lg, spacing.s20, spacing.s20, spacing.s20, spacing.s8, sizes.iconMedium, type.body)
        }
        // the "Paid" check (d2e81313) is 12 wide, i.e. the check glyph at 18, with Figma's 8 gap
        ButtonSize.Medium -> ButtonMetrics(sizes.buttonMedium, 14.dp, spacing.s20, spacing.s20, spacing.s20, spacing.s8, 18.dp, type.subheadStrong)
        // the source pads 12 ("Show PIN" f5984c79); the play glyph's own margins make it 8 in and 4 to the label ("Lesson 2" 5d3ebc67)
        ButtonSize.Small -> ButtonMetrics(sizes.buttonSmall, radii.md, spacing.s12, spacing.s8, spacing.s12, spacing.s4, sizes.iconSmall, type.subheadStrong)
        // "Withdraw" (5e61179c) is 17 semibold with a 17 MSI mark, 104×32 overall
        ButtonSize.XSmall -> ButtonMetrics(sizes.buttonXSmall, radii.full, spacing.s8, spacing.s8, spacing.s8, spacing.s4, 17.dp, type.headline)
    }
}

/**
 * Filled button. Width is the caller's: pass `Modifier.fillMaxWidth()` in cards and footers, leave it to hug for
 * standalone actions. [loading] swaps the label for the white spinner and disables the tap.
 * [contentPadding] replaces the size's side padding (the 46-high "Select the closest slot" on eb4cdf05 pads 10/12).
 */
@Composable
fun Button(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    size: ButtonSize = ButtonSize.Large,
    icon: ImageVector? = null,
    iconSize: Dp? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
    labelStyle: TextStyle? = null,
    contentPadding: PaddingValues? = null,
) {
    val colors = PlataTheme.colors
    val metrics = size.metrics(style)
    val (fill: Color, ink: Color) = when (style) {
        ButtonStyle.Primary -> colors.brandPrimary to colors.textOnDark
        ButtonStyle.Secondary -> colors.surfaceSecondaryButton to colors.textPrimary
        ButtonStyle.SecondaryOnCard -> colors.surfaceBackground to colors.textPrimary
        ButtonStyle.White -> colors.surfaceCard to colors.textPrimary
        ButtonStyle.WhiteAccent -> colors.surfaceCard to colors.accent
        ButtonStyle.Black -> colors.surfaceBlack to colors.textOnDark
        ButtonStyle.Success -> colors.successTint to colors.textSuccess
        ButtonStyle.Destructive -> colors.surfaceCard to colors.textDestructive
    }
    val shape = ContinuousRoundedShape(metrics.radius)
    val showIcon = icon != null && !loading
    Row(
        modifier
            .alpha(if (enabled) 1f else 0.3f)
            .pressScale(enabled = enabled && !loading)
            .pressable(enabled = enabled && !loading, indication = null, onClick = onClick)
            .height(metrics.height)
            .background(fill, shape)
            .semantics { if (loading) contentDescription = label }
            .padding(contentPadding ?: PaddingValues(start = if (showIcon) metrics.startWithIcon else metrics.start, end = metrics.end)),
        horizontalArrangement = Arrangement.spacedBy(metrics.gap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            // the source spinner (7464b63b) is a 26pt ring; the 24-grid glyph's ring is 19.8 of its box
            Icon(PlataIcons.Spinner, size = 31.dp, tint = colors.textOnDark)
        } else {
            if (icon != null) Icon(icon, size = iconSize ?: metrics.icon, tint = ink)
            PlataText(label, labelStyle ?: metrics.label, ink, maxLines = 1)
        }
    }
}

@Preview
@Composable
internal fun ButtonPrimaryLargePreview() = PlataTheme {
    Button("Continue", {}, Modifier.width(353.dp))
}

@Preview
@Composable
internal fun ButtonPrimaryLargeLoadingPreview() = PlataTheme {
    Button("Continue", {}, Modifier.width(313.dp), loading = true)
}

@Preview
@Composable
internal fun ButtonPrimaryMediumPreview() = PlataTheme {
    Button("Share link", {}, Modifier.width(313.dp), size = ButtonSize.Medium)
}

@Preview
@Composable
internal fun ButtonPrimarySmallPreview() = PlataTheme {
    Button("Lesson 2", {}, size = ButtonSize.Small, icon = PlataIcons.Play)
}

@Preview
@Composable
internal fun ButtonSecondaryLargePreview() = PlataTheme {
    Button("Continue later", {}, Modifier.width(353.dp), style = ButtonStyle.Secondary)
}

@Preview
@Composable
internal fun ButtonSecondaryOnCardMediumPreview() = PlataTheme {
    Button("Show my contacts", {}, Modifier.width(313.dp), style = ButtonStyle.SecondaryOnCard, size = ButtonSize.Medium)
}

@Preview
@Composable
internal fun ButtonSecondaryOnCardSmallPreview() = PlataTheme {
    Button("Show PIN", {}, style = ButtonStyle.SecondaryOnCard, size = ButtonSize.Small)
}

@Preview
@Composable
internal fun ButtonWhiteLargePreview() = PlataTheme {
    Button("Open an account", {}, Modifier.width(313.dp), style = ButtonStyle.White)
}

@Preview
@Composable
internal fun ButtonWhiteAccentXSmallPreview() = PlataTheme {
    Button("Withdraw", {}, style = ButtonStyle.WhiteAccent, size = ButtonSize.XSmall, icon = PlataIcons.Msi)
}

@Preview
@Composable
internal fun ButtonBlackLargePreview() = PlataTheme {
    Button("Add to Apple Wallet", {}, Modifier.width(353.dp), style = ButtonStyle.Black, icon = PlataIcons.Wallet)
}

@Preview
@Composable
internal fun ButtonSuccessMediumPreview() = PlataTheme {
    Button("Paid", {}, Modifier.width(313.dp), style = ButtonStyle.Success, size = ButtonSize.Medium, icon = PlataIcons.Check)
}

@Preview
@Composable
internal fun ButtonDestructiveLargePreview() = PlataTheme {
    Button("Log out", {}, Modifier.width(353.dp), style = ButtonStyle.Destructive)
}
