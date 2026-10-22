package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.icons.PlataIcons
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class BulletMarker { Number, Icon, Dot }

// the heart in the 20 ring on 5f6bf512
private val RingGlyphSize = 12.dp

/**
 * List item with a leading marker; spacing between items belongs to the parent stack (20 or 24).
 * - [BulletMarker.Number]: a 20 ringed [number], 20 before the 15/18 text (202bee50, 22bf11f1).
 * - [BulletMarker.Icon]: a 28 [icon] centred on 17/20 text up to 224 wide, 20 apart (dda69155).
 * - [BulletMarker.Dot]: a 3pt dot 2 in from the edge, text 16 in, 15/20 (c5882da1).
 * [ringGlyph] puts a glyph in the Number ring instead of the number (the heart on 5f6bf512); [markerGap] is the marker → text gap.
 * [textStyle] overrides the marker's default text style. [markerAlignment] centres the number on multi-line text (22bf11f1).
 */
@Composable
fun BulletItem(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    marker: BulletMarker = BulletMarker.Dot,
    number: Int = 1,
    icon: ImageVector = PlataIcons.MapPin,
    textStyle: TextStyle? = null,
    markerAlignment: Alignment.Vertical = Alignment.Top,
    markerGap: Dp = PlataTheme.spacing.s20,
    ringGlyph: ImageVector? = null,
) {
    val colors = PlataTheme.colors
    val typography = PlataTheme.typography
    when (marker) {
        BulletMarker.Number -> Row(
            modifier,
            horizontalArrangement = Arrangement.spacedBy(markerGap),
            verticalAlignment = markerAlignment,
        ) {
            NumberMarker(number, ringGlyph)
            // offset, not padding: the 1pt nudge must not grow a two-line item past lineHeight × lines (22bf11f1 pitch 56)
            PlataText(text, textStyle ?: typography.subheadTight, colors.textPrimary, Modifier.weight(1f).offset(y = 1.dp))
        }
        BulletMarker.Icon -> Row(
            modifier,
            horizontalArrangement = Arrangement.spacedBy(PlataTheme.spacing.s20),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // the source pin's ink centres 2 right of this glyph's (dda69155); offset only the drawing so the text keeps its place
            Icon(icon, Modifier.offset(x = 2.dp), size = PlataTheme.sizes.iconLarge)
            PlataText(text, textStyle ?: typography.bodyCompact, colors.textPrimary, Modifier.widthIn(max = 224.dp))
        }
        BulletMarker.Dot -> Row(modifier) {
            Box(Modifier.width(PlataTheme.spacing.s16)) {
                // centred a little below the first line's cap middle, as on c5882da1
                Box(Modifier.offset(x = 2.dp, y = 10.dp).size(3.dp).background(colors.textPrimary, CircleShape))
            }
            PlataText(text, textStyle ?: typography.subhead, colors.textPrimary, Modifier.weight(1f))
        }
    }
}

@Composable
fun BulletItem(
    text: String,
    modifier: Modifier = Modifier,
    marker: BulletMarker = BulletMarker.Dot,
    number: Int = 1,
    icon: ImageVector = PlataIcons.MapPin,
    textStyle: TextStyle? = null,
    markerAlignment: Alignment.Vertical = Alignment.Top,
    markerGap: Dp = PlataTheme.spacing.s20,
    ringGlyph: ImageVector? = null,
) = BulletItem(AnnotatedString(text), modifier, marker, number, icon, textStyle, markerAlignment, markerGap, ringGlyph)

@Composable
private fun NumberMarker(number: Int, glyph: ImageVector?) {
    val ink = PlataTheme.colors.textPrimary
    Box(Modifier.size(PlataTheme.sizes.iconSmall), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(PlataTheme.sizes.iconSmall)) {
            val stroke = 1.6.dp.toPx()
            drawCircle(ink, radius = size.minDimension / 2 - stroke / 2, style = Stroke(stroke))
        }
        if (glyph != null) {
            Icon(glyph, size = RingGlyphSize, tint = ink)
        } else {
            PlataText(number.toString(), PlataTheme.typography.footnoteStrong, ink, textAlign = TextAlign.Center)
        }
    }
}

@Preview
@Composable
internal fun BulletItemNumberPreview() = PlataTheme {
    BulletItem("Works just like physical card", Modifier.width(313.dp), marker = BulletMarker.Number)
}

@Preview
@Composable
internal fun BulletItemIconPreview() = PlataTheme {
    BulletItem("Choose a place for your card delivery", Modifier.width(314.dp), marker = BulletMarker.Icon)
}

@Preview
@Composable
internal fun BulletItemDotPreview() = PlataTheme {
    BulletItem("What stocks are", Modifier.width(242.dp))
}
