package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class InsetRowTrailing { Link, Value }

/** Link: 48 tall, 15pt label, accent "Show" link (025e71c5). Value: 56 tall, 17pt label and secondary value (764b45eb). */
@Composable
fun InsetRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trailing: InsetRowTrailing = InsetRowTrailing.Link,
    onValueClick: () -> Unit = {},
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    val link = trailing == InsetRowTrailing.Link
    Row(
        modifier
            .height(if (link) PlataTheme.sizes.buttonMedium else PlataTheme.sizes.rowSingle)
            .background(colors.surfaceInset, ContinuousRoundedShape(14.dp))
            .padding(horizontal = PlataTheme.spacing.s20),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlataText(label, if (link) typography.subhead else typography.body, colors.textPrimary, Modifier.weight(1f), maxLines = 1)
        if (link) {
            TextButton(value, onValueClick, weight = TextButtonWeight.Semibold, size = TextButtonSize.Subhead)
        } else {
            PlataText(value, typography.body, colors.textSecondary, maxLines = 1)
        }
    }
}

@Preview
@Composable
internal fun InsetRowLinkPreview() = PlataTheme {
    InsetRow("Payment dates", "Show", Modifier.fillMaxWidth())
}

@Preview
@Composable
internal fun InsetRowValuePreview() = PlataTheme {
    InsetRow("Minimum amount", "$100", Modifier.fillMaxWidth(), trailing = InsetRowTrailing.Value)
}
