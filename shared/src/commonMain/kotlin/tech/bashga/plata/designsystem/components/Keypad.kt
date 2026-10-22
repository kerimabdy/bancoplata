package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class KeypadStyle { Light, OnGradient }

private val KeyWidth = 104.dp
private val KeyHeight = 76.dp

/**
 * 3×4 passcode keypad, 320×304: keys 104×76 on a 108 column pitch (1bad6d9f, b76b9dda). [leading] and [trailing]
 * fill the bottom corners (logout, delete, Face ID, or nothing); build them with [KeypadKey] so they share the hit area.
 * [KeypadStyle.OnGradient] sets the digits in keypad/onGradient-ink, the single dark ink standing in for the iOS vibrancy.
 */
@Composable
fun Keypad(
    onDigit: (Int) -> Unit,
    modifier: Modifier = Modifier,
    style: KeypadStyle = KeypadStyle.Light,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val digitColor = if (style == KeypadStyle.OnGradient) colors.keypadOnGradientInk else colors.textPrimary
    val row: @Composable (List<Int>) -> Unit = { digits ->
        Row(horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s4)) {
            digits.forEach { digit -> KeypadKey(digit.toString(), { onDigit(digit) }, color = digitColor) }
        }
    }
    Column(modifier) {
        row(listOf(1, 2, 3))
        row(listOf(4, 5, 6))
        row(listOf(7, 8, 9))
        Row(horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s4)) {
            KeySlot(leading)
            KeypadKey("0", { onDigit(0) }, color = digitColor)
            KeySlot(trailing)
        }
    }
}

@Composable
private fun KeySlot(content: (@Composable () -> Unit)?) {
    Box(Modifier.size(KeyWidth, KeyHeight)) { content?.invoke() }
}

@Preview
@Composable
internal fun KeypadLightPreview() = PlataTheme {
    Keypad({}, trailing = { KeypadKey(PlataIcons.Delete, {}, contentDescription = "Delete") })
}

@Preview
@Composable
internal fun KeypadOnGradientPreview() = PlataTheme {
    val accent = PlataTheme.colors.accent
    Keypad(
        {},
        style = KeypadStyle.OnGradient,
        leading = { KeypadKey(PlataIcons.Logout, {}, tint = accent, contentDescription = "Log out") },
        trailing = { KeypadKey(PlataIcons.FaceId, {}, tint = accent, contentDescription = "Face ID") },
    )
}
