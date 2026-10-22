package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme

// a0a0ef2b: 40×56 white cells, 12 apart
private val CellWidth = 40.dp
private val CaretHeight = 24.dp

/**
 * A one-time-code input: [length] cells fed by the numeric keyboard. Non-digits are dropped and input stops at
 * [length]; the caller reacts when `value.length == length`. Focus it with [focusRequester]; the next empty cell then
 * shows the caret.
 */
@Composable
fun CodeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 4,
    focusRequester: FocusRequester? = null,
) {
    val colors = PlataTheme.colors
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = { typed -> onValueChange(typed.filter(Char::isDigit).take(length)) },
        modifier = modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            // a password-typed field makes Android's password manager offer to save the code as a password
            .semantics { contentType = ContentType.SmsOtpCode },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = { innerTextField ->
            Box {
                // the real field stays laid out (it holds the input session) but the cells draw the code
                Box(Modifier.alpha(0f)) { innerTextField() }
                Row(horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s12)) {
                    repeat(length) { index ->
                        Box(
                            Modifier
                                .size(CellWidth, PlataTheme.sizes.rowSingle)
                                .background(colors.surfaceCard, ContinuousRoundedShape(PlataTheme.radii.md)),
                            contentAlignment = Alignment.Center,
                        ) {
                            val digit = value.getOrNull(index)
                            when {
                                digit != null -> PlataText(digit.toString(), PlataTheme.typography.title2, colors.textPrimary)
                                focused && index == value.length -> Box(Modifier.size(2.dp, CaretHeight).background(colors.accent))
                            }
                        }
                    }
                }
            }
        },
    )
}

@Preview
@Composable
internal fun CodeFieldEmptyPreview() = PlataTheme { CodeField("", {}) }

@Preview
@Composable
internal fun CodeFieldPartialPreview() = PlataTheme { CodeField("27", {}) }
