package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.icons.TopUp
import tech.bashga.plata.designsystem.theme.PlataTheme

/** Width is the caller's grid cell: [Half] 166, [Third] 104; both 86 high. */
enum class ActionTileLayout { Half, Third }

/**
 * White radius-20 action: glyph centred in a 28 box 18 from the top, the 13 semibold label 7 below it, 15 at the
 * bottom (3b3eaed7, a5a647ee). The two-tone glyphs (TopUp, BillPay, Transfer) are drawn at 32 over that box, as the source does.
 */
@Composable
fun ActionTile(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    layout: ActionTileLayout = ActionTileLayout.Half,
    iconSize: Dp = PlataTheme.sizes.iconLarge,
    onClick: (() -> Unit)? = null,
) {
    val colors = PlataTheme.colors
    Column(
        modifier
            .width(if (layout == ActionTileLayout.Half) 166.dp else 104.dp)
            .background(colors.surfaceCard, ContinuousRoundedShape(PlataTheme.radii.xl))
            .then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier)
            .padding(PaddingValues(top = 18.dp, bottom = 15.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(PlataTheme.sizes.iconLarge), contentAlignment = Alignment.Center) {
            Icon(icon, Modifier.wrapContentSize(unbounded = true), size = iconSize, brush = PlataTheme.gradients.glyph)
        }
        Spacer(Modifier.height(7.dp))
        PlataText(label, PlataTheme.typography.footnoteStrong, colors.textPrimary, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Preview
@Composable
internal fun ActionTileHalfPreview() = PlataTheme { ActionTile("Top-up", PlataIcons.TopUp, iconSize = 32.dp) }

@Preview
@Composable
internal fun ActionTileThirdPreview() = PlataTheme {
    ActionTile("Freeze", PlataIcons.Snowflake, layout = ActionTileLayout.Third)
}
