package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class TextButtonTone { Accent, Primary, Secondary, OnDark }

enum class TextButtonWeight { Regular, Semibold }

enum class TextButtonSize { Body, Subhead }

/** Icons are 16 with a 4 gap like the source (43e51dd1, 083f8017); Figma's 20/6 draws them about 3pt larger. */
@Composable
fun TextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: TextButtonTone = TextButtonTone.Accent,
    weight: TextButtonWeight = TextButtonWeight.Regular,
    size: TextButtonSize = TextButtonSize.Body,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val color: Color = when (tone) {
        TextButtonTone.Accent -> colors.accent
        TextButtonTone.Primary -> colors.textPrimary
        TextButtonTone.Secondary -> colors.textSecondary
        TextButtonTone.OnDark -> colors.textOnDark
    }
    val style: TextStyle = when (size) {
        TextButtonSize.Body -> if (weight == TextButtonWeight.Semibold) typography.headline else typography.body
        TextButtonSize.Subhead -> if (weight == TextButtonWeight.Semibold) typography.subheadStrong else typography.subhead
    }
    Row(
        modifier
            .height(PlataTheme.sizes.nav)
            .pressable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) Icon(leadingIcon, size = PlataTheme.sizes.iconXSmall, tint = color)
        PlataText(label, style, color, maxLines = 1)
        if (trailingIcon != null) Icon(trailingIcon, size = PlataTheme.sizes.iconXSmall, tint = color)
    }
}

@Preview
@Composable
internal fun TextButtonAccentBodyPreview() = PlataTheme { TextButton("Close", {}) }

@Preview
@Composable
internal fun TextButtonAccentSemiboldSubheadPreview() = PlataTheme {
    TextButton("See all", {}, weight = TextButtonWeight.Semibold, size = TextButtonSize.Subhead)
}

@Preview
@Composable
internal fun TextButtonPrimaryBodyPreview() = PlataTheme {
    TextButton("Pass the screening again", {}, tone = TextButtonTone.Primary)
}

@Preview
@Composable
internal fun TextButtonPrimarySemiboldBodyPreview() = PlataTheme {
    TextButton("Additional fields", {}, tone = TextButtonTone.Primary, weight = TextButtonWeight.Semibold, leadingIcon = PlataIcons.Plus)
}

@Preview
@Composable
internal fun TextButtonSecondarySubheadPreview() = PlataTheme {
    TextButton("By VestFi", {}, tone = TextButtonTone.Secondary, size = TextButtonSize.Subhead, trailingIcon = PlataIcons.Info)
}

@Preview
@Composable
internal fun TextButtonOnDarkBodyPreview() = PlataTheme { TextButton("Close", {}, tone = TextButtonTone.OnDark) }
