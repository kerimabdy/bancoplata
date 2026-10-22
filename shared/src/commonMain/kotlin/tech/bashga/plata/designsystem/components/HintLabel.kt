package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

@Composable
fun HintLabel(label: String, modifier: Modifier = Modifier, icon: ImageVector = PlataIcons.HandTap) {
    val color = PlataTheme.colors.textTertiary
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, size = PlataTheme.sizes.iconXSmall, tint = color)
        PlataText(label, PlataTheme.typography.subheadStrong, color, maxLines = 1)
    }
}

@Preview
@Composable
internal fun HintLabelPreview() = PlataTheme { HintLabel("Swipe or tap to see details") }
