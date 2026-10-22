package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_billing_timeline
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

/**
 * White card, radius 20, padding 20, 20pt title with a grey 22 chevron disc (8bda0dcf). Expanded, the [paragraphs]
 * (15/18) and then [content] (an illustration) follow 20 below the title, 24 apart.
 */
@Composable
fun Accordion(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    paragraphs: List<String> = emptyList(),
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    Column(
        modifier
            .background(colors.surfaceCard, ContinuousRoundedShape(PlataTheme.radii.xl))
            .padding(spacing.cardPadding),
    ) {
        Row(Modifier.pressable(onClick = onToggle), verticalAlignment = Alignment.CenterVertically) {
            PlataText(title, PlataTheme.typography.title3, colors.textPrimary, Modifier.weight(1f))
            IconButton(
                if (expanded) PlataIcons.ChevronUp else PlataIcons.ChevronDown,
                onToggle,
                style = IconButtonStyle.Grey,
                size = IconButtonSize.S20,
                glyphSize = 15.dp,
            )
        }
        if (expanded) {
            Spacer(Modifier.height(spacing.s20))
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s24)) {
                paragraphs.forEach { PlataText(it, PlataTheme.typography.subheadTight, colors.textPrimary) }
                content?.invoke(this)
            }
        }
    }
}

@Preview
@Composable
internal fun AccordionCollapsedPreview() = PlataTheme {
    Accordion("When to pay", expanded = false, onToggle = {}, modifier = Modifier.fillMaxWidth())
}

@Preview
@Composable
internal fun AccordionExpandedPreview() = PlataTheme {
    Accordion(
        "Billing period",
        expanded = true,
        onToggle = {},
        modifier = Modifier.fillMaxWidth(),
        paragraphs = listOf(
            "You spend money with you card during the billing period by the end of which you receive your statement with amount to pay and the payment date.",
            "You can either pay off the full amount or make the minimum payment.",
        ),
    ) {
        Image(
            painterResource(Res.drawable.raster_billing_timeline),
            contentDescription = "Billing period timeline",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
        )
    }
}

@Preview
@Composable
internal fun AccordionInteractivePreview() = PlataTheme {
    var expanded by remember { mutableStateOf(false) }
    Accordion(
        "When to pay",
        expanded = expanded,
        onToggle = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        paragraphs = listOf("Pay before the payment date to avoid interest."),
    )
}
