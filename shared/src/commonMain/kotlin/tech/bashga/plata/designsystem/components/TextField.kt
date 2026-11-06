package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** [White] on the grey page; [Grey] on white cards and sheets, where the source fills it with surface/background (642128ff, 78f0c567). */
enum class TextFieldSurface { White, Grey }

/**
 * 56pt input, radius 16. Empty shows [placeholder]; with a value the optional [label] floats above it in 13pt.
 * [alwaysShowLabel] keeps the label up while empty, the placeholder then a light hint below it (MethodCard fields).
 * [trailing] holds a clear or copy action ([TextFieldClearButton]). Focus it through [focusRequester]; the real ink caret
 * then draws (state tests pause the clock to capture it). [showCaret] is the old frozen caret for the per-screenshot screens.
 * [placeholderColor] overrides the hint colour (control/off for the label-less "Other reasons", 586ac28a).
 */
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = placeholder.ifEmpty { null },
    surface: TextFieldSurface = TextFieldSurface.White,
    trailing: (@Composable () -> Unit)? = null,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    alwaysShowLabel: Boolean = false,
    showCaret: Boolean = false,
    placeholderColor: Color? = null,
    focusRequester: FocusRequester? = null,
) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val spacing = PlataTheme.spacing
    val focus = focusRequester ?: remember { FocusRequester() }
    Row(
        modifier
            .focusOnTap(focus, enabled = !readOnly)
            .heightIn(min = PlataTheme.sizes.rowSingle)
            .background(
                if (surface == TextFieldSurface.White) colors.surfaceCard else colors.surfaceBackground,
                ContinuousRoundedShape(PlataTheme.radii.lg),
            )
            // the clear disc centres 24 in from the right edge (62147891), closer than the 20 text inset
            .padding(PaddingValues(start = spacing.s20, end = if (trailing != null) spacing.s12 else spacing.s20)),
        horizontalArrangement = Arrangement.spacedBy(spacing.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val showLabel = label != null && (value.isNotEmpty() || alwaysShowLabel)
            if (showLabel) PlataText(label.orEmpty(), typography.footnote, colors.textSecondary, maxLines = 1)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.focusRequester(focus),
                readOnly = readOnly,
                textStyle = typography.body.merge(color = colors.textPrimary),
                singleLine = true,
                cursorBrush = SolidColor(colors.textPrimary),
                keyboardOptions = keyboardOptions,
                onTextLayout = { layout = it },
                decorationBox = { field ->
                    Box(Modifier.frozenCaret(showCaret, { layout }, colors.textPrimary), contentAlignment = Alignment.CenterStart) {
                        // under a label the placeholder is the light iOS hint ("18 digits", 43e51dd1)
                        if (value.isEmpty()) PlataText(placeholder, typography.body, placeholderColor ?: if (showLabel) colors.controlOff else colors.textSecondary, maxLines = 1)
                        field()
                    }
                },
            )
        }
        trailing?.invoke()
    }
}

/** A tap anywhere in a field's box focuses [focus] and raises the keyboard, not only a tap on the text line itself. */
@Composable
internal fun Modifier.focusOnTap(focus: FocusRequester, enabled: Boolean = true): Modifier {
    if (!enabled) return this
    val keyboard = LocalSoftwareKeyboardController.current
    return pointerInput(focus, keyboard) {
        detectTapGestures {
            focus.requestFocus()
            keyboard?.show()
        }
    }
}

/** Draws a 2pt caret at the end of [layout]'s text, as the source screens freeze it. */
internal fun Modifier.frozenCaret(show: Boolean, layout: () -> TextLayoutResult?, color: Color): Modifier =
    if (!show) {
        this
    } else {
        drawWithContent {
            drawContent()
            val text = layout() ?: return@drawWithContent
            val rect = text.getCursorRect(text.layoutInput.text.length)
            drawRect(color, topLeft = Offset(rect.left, rect.top), size = Size(2.dp.toPx(), rect.height))
        }
    }

/** The grey clear disc of a filled TextField; 22 across so its ink is the source's 16. */
@Composable
fun TextFieldClearButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Icon(
        PlataIcons.ClearX,
        modifier.pressable(onClickLabel = "Clear", onClick = onClick),
        size = 22.dp,
        tint = PlataTheme.colors.controlOff,
    )
}

@Preview
@Composable
internal fun TextFieldWhiteEmptyPreview() = PlataTheme {
    TextField("", {}, Modifier.width(353.dp), placeholder = "Beneficiary name")
}

@Preview
@Composable
internal fun TextFieldWhiteFilledPreview() = PlataTheme {
    TextField("Ana López", {}, Modifier.width(353.dp), placeholder = "Beneficiary name", trailing = { TextFieldClearButton({}) })
}

@Preview
@Composable
internal fun TextFieldGreyEmptyPreview() = PlataTheme {
    TextField("", {}, Modifier.width(353.dp), placeholder = "Details (optional)", surface = TextFieldSurface.Grey)
}

@Preview
@Composable
internal fun TextFieldGreyFilledPreview() = PlataTheme {
    TextField(
        "Transfer for rent",
        {},
        Modifier.width(353.dp),
        placeholder = "Details (optional)",
        surface = TextFieldSurface.Grey,
        trailing = { TextFieldClearButton({}) },
        showCaret = true,
    )
}
