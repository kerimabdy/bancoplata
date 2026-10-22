package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class CheckCircleState { Checked, Unchecked, Disabled }

/** The unchecked ring is 22 across at a 1pt stroke inside the 24 box (00e6fc01, 84e28520; Figma says 24 at 1.5). */
@Composable
fun CheckCircle(state: CheckCircleState, modifier: Modifier = Modifier) {
    val colors = PlataTheme.colors
    val accent = colors.accent
    val check = colors.textOnDark
    val ring = colors.controlOff
    Canvas(modifier.size(PlataTheme.sizes.iconMedium).alpha(if (state == CheckCircleState.Disabled) 0.3f else 1f)) {
        val unit = size.width / 24f
        if (state == CheckCircleState.Checked) {
            drawCircle(accent)
            val path = Path().apply {
                moveTo(7.5f * unit, 12.2f * unit)
                lineTo(10.7f * unit, 15.4f * unit)
                lineTo(16.5f * unit, 9f * unit)
            }
            drawPath(path, check, style = Stroke(2.4f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round))
        } else {
            val stroke = unit
            drawCircle(ring, radius = 11f * unit - stroke / 2, center = Offset(size.width / 2, size.height / 2), style = Stroke(stroke))
        }
    }
}

@Preview
@Composable
internal fun CheckCircleCheckedPreview() = PlataTheme { CheckCircle(CheckCircleState.Checked) }

@Preview
@Composable
internal fun CheckCircleUncheckedPreview() = PlataTheme { CheckCircle(CheckCircleState.Unchecked) }

@Preview
@Composable
internal fun CheckCircleDisabledPreview() = PlataTheme { CheckCircle(CheckCircleState.Disabled) }
