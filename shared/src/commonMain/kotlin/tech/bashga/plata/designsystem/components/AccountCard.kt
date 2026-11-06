package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.tilePressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.PlataWordmark
import tech.bashga.plata.designsystem.theme.PlataTheme

/** White 353×80 source-account card: 40 [tile], balance over a 13pt caption, both text/primary (40d2b403). */
@Composable
fun AccountCard(
    amount: String,
    caption: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    tile: @Composable () -> Unit = { PlataAppTile() },
) {
    val colors = PlataTheme.colors
    val spacing = PlataTheme.spacing
    Row(
        modifier
            .tilePressable(onClick)
            .background(colors.surfaceCard, ContinuousRoundedShape(PlataTheme.radii.xl))
            .heightIn(min = PlataTheme.sizes.rowAsset)
            .padding(horizontal = spacing.cardPadding, vertical = spacing.s12),
        horizontalArrangement = Arrangement.spacedBy(spacing.rowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tile()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            PlataText(amount, PlataTheme.typography.body, colors.textPrimary, maxLines = 1)
            PlataText(caption, PlataTheme.typography.footnote, colors.textPrimary, maxLines = 1)
        }
    }
}

/** The orange PLATA app tile used as the account leading. */
@Composable
fun PlataAppTile(modifier: Modifier = Modifier) {
    IconTile(modifier, fill = PlataTheme.gradients.appIcon) {
        Image(
            rememberVectorPainter(PlataIcons.PlataWordmark),
            contentDescription = "Plata",
            modifier = Modifier.size(32.dp, 4.8.dp),
            colorFilter = ColorFilter.tint(PlataTheme.colors.textOnDark),
        )
    }
}

@Preview
@Composable
internal fun AccountCardPreview() = PlataTheme {
    AccountCard("$97.57", "From Plata Crédito", Modifier.width(353.dp))
}
