package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** A balance before and after a transfer: "USD 0 → USD 1", the old value struck through (705c4f5e, 372e6fd9). */
@Composable
fun ValueChange(from: String, to: String, modifier: Modifier = Modifier) {
    val color = PlataTheme.colors.textSecondary
    val style = PlataTheme.typography.subhead
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlataText(from, style.merge(TextStyle(textDecoration = TextDecoration.LineThrough)), color, maxLines = 1)
        Icon(PlataIcons.ArrowRight, size = PlataTheme.sizes.iconXSmall, tint = color)
        PlataText(to, style, color, maxLines = 1)
    }
}

@Preview
@Composable
internal fun ValueChangePreview() = PlataTheme { ValueChange("USD 0", "USD 1") }

@Preview
@Composable
internal fun ValueChangeAmountsPreview() = PlataTheme { ValueChange("$97.57", "$78.81") }
