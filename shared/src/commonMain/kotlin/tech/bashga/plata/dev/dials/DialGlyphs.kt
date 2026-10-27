package tech.bashga.plata.dev.dials

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

/** The SF Symbols DialKit uses, drawn on a 24-unit grid so they scale to their point size. */
internal enum class DialGlyph { Sliders, ChevronDown, Copy, Check, Bolt, AddVersion }

@Composable
internal fun DialIcon(glyph: DialGlyph, size: Dp, color: Color, modifier: Modifier = Modifier, strokeWidth: Float = 2f) {
    Canvas(modifier.size(size)) {
        val unit = this.size.width / 24f
        val stroke = Stroke(strokeWidth * unit, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun p(x: Float, y: Float) = Offset(x * unit, y * unit)
        fun line(points: List<Offset>) = drawPath(Path().apply { points.forEachIndexed { i, o -> if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y) } }, color, style = stroke)
        when (glyph) {
            DialGlyph.Sliders -> drawSliders(unit, color, stroke)
            DialGlyph.ChevronDown -> line(listOf(p(5f, 8.5f), p(12f, 15.5f), p(19f, 8.5f)))
            DialGlyph.Check -> line(listOf(p(4.5f, 12.5f), p(9.5f, 17.5f), p(19.5f, 6.5f)))
            DialGlyph.Copy -> {
                drawRoundRect(color, p(8f, 8f), Size(12f * unit, 13f * unit), CornerRadius(2.5f * unit), style = stroke)
                line(listOf(p(4f, 15f), p(4f, 5.5f), p(5.5f, 4f), p(14f, 4f)))
            }
            DialGlyph.Bolt -> drawPath(
                Path().apply {
                    moveTo(13.5f * unit, 2f * unit)
                    lineTo(4f * unit, 13.5f * unit)
                    lineTo(11f * unit, 13.5f * unit)
                    lineTo(10.5f * unit, 22f * unit)
                    lineTo(20f * unit, 10f * unit)
                    lineTo(13f * unit, 10f * unit)
                    close()
                },
                color,
            )
            // ICON_ADD_PRESET in Josh Puckett's DialKit
            DialGlyph.AddVersion -> {
                line(listOf(p(4f, 6f), p(20f, 6f)))
                line(listOf(p(4f, 12f), p(10f, 12f)))
                line(listOf(p(4f, 18f), p(10f, 18f)))
                line(listOf(p(15f, 15f), p(21f, 15f)))
                line(listOf(p(18f, 12f), p(18f, 18f)))
            }
        }
    }
}

// slider.horizontal.3: three tracks, each broken by a ring-shaped knob
private fun DrawScope.drawSliders(unit: Float, color: Color, stroke: Stroke) {
    val knobRadius = 2.6f
    listOf(6f to 15f, 12f to 8f, 18f to 12.5f).forEach { (y, knob) ->
        val gap = knobRadius + 1.4f
        drawLine(color, Offset(3f * unit, y * unit), Offset((knob - gap) * unit, y * unit), stroke.width, StrokeCap.Round)
        drawLine(color, Offset((knob + gap) * unit, y * unit), Offset(21f * unit, y * unit), stroke.width, StrokeCap.Round)
        drawCircle(color, knobRadius * unit, Offset(knob * unit, y * unit), style = stroke)
    }
}
