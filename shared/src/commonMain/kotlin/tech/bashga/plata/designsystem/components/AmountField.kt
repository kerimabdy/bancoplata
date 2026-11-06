package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.InfoFill
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/**
 * [Stacked]: amount over a meta line with the [AmountField] conversion on the left and the note on the right
 * (40d2b403, 6e1c2ab5; 353×77, padding 16). [SingleRow]: amount and note on one 56 line, padding 20 (a6fae664).
 */
enum class AmountFieldLayout { Stacked, SingleRow }

/**
 * White amount input: [prefix] ("USD", "$") then the typed [amount] in 20 bold, [placeholder] in control/off when empty;
 * the conversion and note lines are 13 regular.
 * Focus it through [focusRequester] for the real caret; [showCaret] is the old static caret for the per-screenshot screens.
 */
@Composable
fun AmountField(
    amount: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    prefix: String = "USD",
    placeholder: String = "0",
    conversion: String? = null,
    note: String? = null,
    noteIcon: ImageVector? = null,
    layout: AmountFieldLayout = AmountFieldLayout.Stacked,
    showCaret: Boolean = false,
    focusRequester: FocusRequester? = null,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val spacing = PlataTheme.spacing
    val shape = ContinuousRoundedShape(PlataTheme.radii.xl)
    val focus = focusRequester ?: remember { FocusRequester() }
    val amountInput: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlataText("$prefix ", typography.title3, colors.textPrimary, maxLines = 1)
            // the source caret sits before the placeholder in light grey (6e1c2ab5) and after typed digits in ink (40d2b403)
            if (showCaret && amount.isEmpty()) Caret(colors.controlOff)
            Box(contentAlignment = Alignment.CenterStart) {
                if (amount.isEmpty()) {
                    // the real caret draws at the start, so the placeholder steps past it as the frozen one did
                    PlataText(placeholder, typography.title3, colors.controlOff, Modifier.padding(start = if (showCaret) 0.dp else CaretWidth), maxLines = 1)
                }
                BasicTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    // an empty field is otherwise 0 wide and clips the real caret
                    modifier = Modifier.width(IntrinsicSize.Min).widthIn(min = CaretWidth)
                        .focusRequester(focus),
                    textStyle = typography.title3.merge(color = colors.textPrimary),
                    singleLine = true,
                    cursorBrush = SolidColor(if (amount.isEmpty()) colors.controlOff else colors.textPrimary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
            if (showCaret && amount.isNotEmpty()) Caret(colors.textPrimary)
        }
    }
    val noteContent: @Composable () -> Unit = {
        if (note != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.s4), verticalAlignment = Alignment.CenterVertically) {
                PlataText(note, typography.footnote, colors.textPrimary, maxLines = 1)
                if (noteIcon != null) Icon(noteIcon, size = 19.dp, tint = colors.controlOff)
            }
        }
    }
    when (layout) {
        AmountFieldLayout.Stacked -> Column(
            modifier
                .focusOnTap(focus)
                .background(colors.surfaceCard, shape)
                .padding(PaddingValues(start = spacing.s16, end = spacing.s16, top = spacing.s16, bottom = 14.dp)),
            // 4 under the amount puts the 13pt line where the source has it and the card at 77 (40d2b403)
            verticalArrangement = Arrangement.spacedBy(spacing.s4),
        ) {
            amountInput()
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlataText(conversion.orEmpty(), typography.footnote, colors.textPrimary, Modifier.weight(1f), maxLines = 1)
                noteContent()
            }
        }
        AmountFieldLayout.SingleRow -> Row(
            modifier
                .focusOnTap(focus)
                .background(colors.surfaceCard, shape)
                .heightIn(min = PlataTheme.sizes.rowSingle)
                .padding(horizontal = spacing.s20),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            amountInput()
            Spacer(Modifier.weight(1f))
            noteContent()
        }
    }
}

private val CaretWidth = 2.dp

@Composable
private fun Caret(color: Color) {
    Box(Modifier.size(CaretWidth, 25.dp).background(color))
}

@Preview
@Composable
internal fun AmountFieldFilledPreview() = PlataTheme {
    AmountField("1", {}, Modifier.width(353.dp), conversion = "$18.76", note = "No commission", showCaret = true)
}

@Preview
@Composable
internal fun AmountFieldEmptyPreview() = PlataTheme {
    AmountField("", {}, Modifier.width(353.dp), conversion = "USD 1 = $18.76", showCaret = true)
}

@Preview
@Composable
internal fun AmountFieldSingleRowPreview() = PlataTheme {
    AmountField(
        "50",
        {},
        Modifier.width(353.dp),
        prefix = "$",
        note = "Commission up to $230.84",
        noteIcon = PlataIcons.InfoFill,
        layout = AmountFieldLayout.SingleRow,
        showCaret = true,
    )
}
