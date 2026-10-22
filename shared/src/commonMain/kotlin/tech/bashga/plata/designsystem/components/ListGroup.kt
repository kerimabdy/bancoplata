package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/** Settings group: ListRows (56 each) stacked without separators in a white r20 card; groups sit 20 apart (59741a55). */
@Composable
fun ListGroup(modifier: Modifier = Modifier, rows: @Composable ColumnScope.() -> Unit) {
    val shape = ContinuousRoundedShape(PlataTheme.radii.xl)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PlataTheme.colors.surfaceCard),
        content = rows,
    )
}

@Preview
@Composable
internal fun ListGroupDefaultPreview() = PlataTheme {
    val glyphBrush = PlataTheme.gradients.notification
    ListGroup(Modifier.width(353.dp)) {
        ListRow(
            "Appearance",
            value = "System",
            showChevron = true,
            onClick = {},
            leading = { Icon(PlataIcons.Appearance, IconSize.Medium) },
        )
        ListRow(
            "Language",
            value = "English",
            showChevron = true,
            onClick = {},
            leading = { Icon(PlataIcons.TextAa, IconSize.Medium, brush = glyphBrush) },
        )
        ListRow(
            "Notifications",
            showChevron = true,
            onClick = {},
            leading = { Icon(PlataIcons.Notification, IconSize.Medium, brush = glyphBrush) },
        )
    }
}
