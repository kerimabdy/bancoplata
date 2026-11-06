package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

// the magnifier and clear discs read 16–17 across (083f8017, 7e1c7742): the 24-grid glyphs drawn at 22
private val SearchGlyph = 22.dp
private val SearchInset = 5.dp

/**
 * iOS search bar, 361×36 at the 16pt search margin. [active] adds the clear button (when there is text) and a
 * Cancel TextButton, and the field shrinks to fit them. Focus it through [focusRequester] for the real accent caret;
 * [showCaret] is the old frozen caret (7e1c7742) for the per-screenshot screens.
 */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    active: Boolean = false,
    onClear: () -> Unit = { onValueChange("") },
    onCancel: () -> Unit = {},
    showCaret: Boolean = false,
    focusRequester: FocusRequester? = null,
) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val colors = PlataTheme.colors
    val body = PlataTheme.typography.body
    val focus = focusRequester ?: remember { FocusRequester() }
    Row(
        modifier.height(PlataTheme.sizes.search),
        horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .weight(1f)
                .height(PlataTheme.sizes.search)
                .focusOnTap(focus)
                .background(colors.surfaceSearch, ContinuousRoundedShape(PlataTheme.radii.sm))
                .padding(horizontal = SearchInset),
            horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(PlataIcons.Search, size = SearchGlyph, tint = colors.textTertiary)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).focusRequester(focus),
                textStyle = body.merge(color = colors.textPrimary),
                singleLine = true,
                cursorBrush = SolidColor(colors.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                onTextLayout = { layout = it },
                decorationBox = { field ->
                    Box(Modifier.frozenCaret(showCaret, { layout }, colors.accent), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) PlataText(placeholder, body, colors.textTertiary, maxLines = 1)
                        field()
                    }
                },
            )
            if (active && value.isNotEmpty()) {
                Icon(
                    PlataIcons.ClearX,
                    Modifier.pressable(onClickLabel = "Clear", onClick = onClear),
                    size = SearchGlyph,
                    tint = colors.textTertiary,
                )
            }
        }
        if (active) TextButton("Cancel", onCancel)
    }
}

@Preview
@Composable
internal fun SearchFieldPlaceholderPreview() = PlataTheme {
    SearchField("", {}, Modifier.width(361.dp), placeholder = "Stocks or ETFs")
}

@Preview
@Composable
internal fun SearchFieldActivePreview() = PlataTheme {
    SearchField("krisp", {}, Modifier.width(361.dp), active = true, showCaret = true)
}
