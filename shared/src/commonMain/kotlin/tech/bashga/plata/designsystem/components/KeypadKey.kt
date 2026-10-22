package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

private val KeyWidth = 104.dp
private val KeyHeight = 76.dp

@Composable
fun KeypadKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = PlataTheme.colors.textPrimary,
) = KeyBox(onClick, modifier, label) {
    PlataText(label, PlataTheme.typography.keypad, color)
}

@Composable
fun KeypadKey(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = PlataTheme.colors.textTertiary,
    contentDescription: String? = null,
) = KeyBox(onClick, modifier, contentDescription) {
    Icon(icon, size = PlataTheme.sizes.iconLarge, tint = tint, contentDescription = contentDescription)
}

@Composable
private fun KeyBox(onClick: () -> Unit, modifier: Modifier, label: String?, content: @Composable () -> Unit) {
    Box(
        modifier.size(KeyWidth, KeyHeight).pressable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Preview
@Composable
internal fun KeypadKeyDigitPreview() = PlataTheme { KeypadKey("1", {}) }

@Preview
@Composable
internal fun KeypadKeyIconPreview() = PlataTheme { KeypadKey(PlataIcons.Delete, {}, contentDescription = "Delete") }
