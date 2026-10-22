package tech.bashga.plata.designsystem.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import tech.bashga.plata.designsystem.theme.PlataPalette

private val glyphGradient = Brush.linearGradient(
    0f to PlataPalette.GlyphGradientTop,
    1f to PlataPalette.GlyphGradientBottom,
    start = Offset(12f, 7.09f),
    end = Offset(12f, 16.95f),
)
private val tint = SolidColor(PlataPalette.GlyphTint)
private val orange = SolidColor(PlataPalette.GlyphOrange)
private val orangeTint = SolidColor(PlataPalette.GlyphOrangeTint)

private val billPayDiagonal by lazy {
    plataGlyph("bill-pay-diagonal", multicolour = true) {
        val discs = listOf(
            "M 3.91 7.09 A 3.26 3.26 0 1 0 10.43 7.09 A 3.26 3.26 0 1 0 3.91 7.09 Z" to true,
            "M 13.54 7.09 A 3.26 3.26 0 1 0 20.06 7.09 A 3.26 3.26 0 1 0 13.54 7.09 Z" to false,
            "M 3.91 16.95 A 3.26 3.26 0 1 0 10.43 16.95 A 3.26 3.26 0 1 0 3.91 16.95 Z" to false,
            "M 13.54 16.95 A 3.26 3.26 0 1 0 20.06 16.95 A 3.26 3.26 0 1 0 13.54 16.95 Z" to true,
        )
        discs.forEach { (path, isOrange) ->
            fill(path, brush = if (isOrange) orangeTint else tint)
            stroke(path, width = 2.2f, brush = if (isOrange) orange else glyphGradient)
        }
    }
}

/** The Bill payments row mark in the Add to Favorites sheet (e94a49a9): orange on the diagonal, unlike BillPay's single orange disc. */
val PlataIcons.BillPayDiagonal: ImageVector get() = billPayDiagonal

private val heartFill by lazy {
    plataGlyph("heart-fill") {
        fill("M 12 20.5 C 12 20.5 3.5 15.5 3.5 9.5 C 3.385 8.45 3.642 7.393 4.228 6.514 C 4.814 5.634 5.69 4.989 6.703 4.691 C 7.717 4.393 8.803 4.461 9.771 4.883 C 10.74 5.305 11.529 6.055 12 7 C 12.471 6.055 13.261 5.305 14.229 4.883 C 15.197 4.461 16.283 4.393 17.297 4.691 C 18.31 4.989 19.186 5.634 19.772 6.514 C 20.358 7.393 20.615 8.45 20.5 9.5 C 20.5 15.5 12 20.5 12 20.5 Z")
    }
}

/** The filled heart in the "How it works" step ring (5f6bf512); same outline as [PlataIcons.Heart]. */
val PlataIcons.HeartFill: ImageVector get() = heartFill
