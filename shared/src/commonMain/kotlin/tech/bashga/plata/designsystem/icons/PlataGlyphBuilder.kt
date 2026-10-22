package tech.bashga.plata.designsystem.icons

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.theme.PlataPalette

private const val MULTICOLOUR_PREFIX = "multicolour/"

/**
 * Multicolour glyphs carry their own paints (gradient/glyph, brand orange, wallet stripes) and must be drawn untinted.
 * Monochrome glyphs are drawn in ink and are meant to be tinted with `ColorFilter.tint(color)`.
 */
val ImageVector.isMulticolour: Boolean get() = name.startsWith(MULTICOLOUR_PREFIX)

class GlyphScope internal constructor(private val builder: ImageVector.Builder) {
    private val ink = SolidColor(PlataPalette.Ink)

    fun fill(pathData: String, brush: Brush = ink, evenOdd: Boolean = false) {
        builder.addPath(
            pathData = PathParser().parsePathString(pathData).toNodes(),
            pathFillType = if (evenOdd) PathFillType.EvenOdd else PathFillType.NonZero,
            fill = brush,
        )
    }

    fun stroke(
        pathData: String,
        width: Float,
        brush: Brush = ink,
        cap: StrokeCap = StrokeCap.Round,
        join: StrokeJoin = StrokeJoin.Round,
        miter: Float = 4f,
    ) {
        builder.addPath(
            pathData = PathParser().parsePathString(pathData).toNodes(),
            stroke = brush,
            strokeLineWidth = width,
            strokeLineCap = cap,
            strokeLineJoin = join,
            strokeLineMiter = miter,
        )
    }
}

/** Builds a glyph on a [width]×[height] point grid (24×24 unless the source frame differs). */
fun plataGlyph(
    name: String,
    width: Float = 24f,
    height: Float = 24f,
    multicolour: Boolean = false,
    block: GlyphScope.() -> Unit,
): ImageVector {
    val builder = ImageVector.Builder(
        name = if (multicolour) MULTICOLOUR_PREFIX + name else name,
        defaultWidth = width.dp,
        defaultHeight = height.dp,
        viewportWidth = width,
        viewportHeight = height,
    )
    GlyphScope(builder).block()
    return builder.build()
}
