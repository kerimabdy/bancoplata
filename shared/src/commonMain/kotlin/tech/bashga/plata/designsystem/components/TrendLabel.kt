package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class TrendDirection { Up, Down }

enum class TrendLabelTone { Default, OnMedia }

@Composable
fun TrendLabel(
    value: String,
    direction: TrendDirection,
    modifier: Modifier = Modifier,
    tone: TrendLabelTone = TrendLabelTone.Default,
    caption: String? = null,
    textStyle: TextStyle? = null,
    captionColor: Color? = null,
    arrowSize: Dp? = null,
) {
    val colors = PlataTheme.colors
    val onMedia = tone == TrendLabelTone.OnMedia
    val color = when {
        onMedia -> colors.textOnDark
        direction == TrendDirection.Up -> colors.textSuccess
        else -> colors.textNegative
    }
    // StockTile sets the value in 13 (3d1d2b9b) and keeps the 15-size arrow
    val style = (textStyle ?: if (onMedia) PlataTheme.typography.body else PlataTheme.typography.subhead).merge(TextStyle(fontFeatureSettings = "tnum"))
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        val arrow = if (direction == TrendDirection.Up) PlataIcons.ArrowUp else PlataIcons.ArrowDown
        if (onMedia) {
            Icon(arrow, size = PlataTheme.sizes.iconXSmall, tint = color)
            Spacer(Modifier.width(PlataTheme.spacing.s4))
        } else {
            // drawn at 15 so the arrow ink is 10 high like 11cfd731; the box padding gives the source's 4pt gap
            Icon(arrow, size = arrowSize ?: 15.dp, tint = color)
        }
        PlataText(value, style, color, maxLines = 1)
        if (caption != null) {
            Spacer(Modifier.width(PlataTheme.spacing.s4))
            PlataText(caption, style, captionColor ?: color, maxLines = 1)
        }
    }
}

@Preview
@Composable
internal fun TrendLabelUpPreview() = PlataTheme { TrendLabel("0.82%", TrendDirection.Up) }

@Preview
@Composable
internal fun TrendLabelDownPreview() = PlataTheme { TrendLabel("0.39%", TrendDirection.Down) }

@Preview
@Composable
internal fun TrendLabelOnMediaPreview() = PlataTheme {
    TrendLabel("0.38%", TrendDirection.Up, tone = TrendLabelTone.OnMedia, caption = "since advice release")
}
