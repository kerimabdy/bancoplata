package tech.bashga.plata.designsystem.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import tech.bashga.plata.designsystem.theme.PlataPalette

// Glyphs the Figma set lacks, traced from the source screens on the 24pt grid (step 2).
// Duotone and two-tone glyphs bake their paints (gradient/glyph spans each glyph's own height, like the source).

private fun glyphGradient(top: Float, bottom: Float): Brush = Brush.linearGradient(
    0f to PlataPalette.GlyphGradientTop,
    1f to PlataPalette.GlyphGradientBottom,
    start = Offset(12f, top),
    end = Offset(12f, bottom),
)

private val tint = SolidColor(PlataPalette.GlyphTint)
private val orange = SolidColor(PlataPalette.GlyphOrange)
private val orangeTint = SolidColor(PlataPalette.GlyphOrangeTint)
private val white = SolidColor(PlataPalette.White)
private val msiGradient = Brush.linearGradient(
    0f to PlataPalette.MsiGradientStart,
    1f to PlataPalette.MsiGradientEnd,
    start = Offset(3.5f, 3.5f),
    end = Offset(20.5f, 20.5f),
)

private val crown by lazy {
    plataGlyph("crown") {
        stroke("M 10.35 5.63 A 1.65 1.65 0 1 0 13.65 5.63 A 1.65 1.65 0 1 0 10.35 5.63 Z", width = 1.7f)
        stroke("M 1.57 9.52 A 1.27 1.27 0 1 0 4.12 9.52 A 1.27 1.27 0 1 0 1.57 9.52 Z", width = 1.7f)
        stroke("M 19.88 9.52 A 1.27 1.27 0 1 0 22.42 9.52 A 1.27 1.27 0 1 0 19.88 9.52 Z", width = 1.7f)
        stroke("M 4.05 10.5 L 7.88 12.23 L 11.17 7.05 M 12.83 7.05 L 16.12 12.23 L 19.95 10.5", width = 1.7f)
        stroke("M 3.3 11.32 L 5.48 18.98 Q 5.77 20.18 7.05 20.18 H 16.95 Q 18.23 20.18 18.52 18.98 L 20.7 11.32", width = 1.7f)
        stroke("M 5.55 16.73 H 18.45", width = 1.7f)
    }
}

/** Benefit row mark for "10 cashback categories" (787363a9); drawn at 32. */
val PlataIcons.Crown: ImageVector get() = crown

private val times2 by lazy {
    plataGlyph("times2") {
        stroke("M 2.93 9.9 L 10.65 17.1 M 10.65 9.9 L 2.93 17.1", width = 1.8f)
        stroke("M 13.42 9.45 C 13.65 7.12 15.15 6.07 17.1 6.07 C 19.2 6.07 20.55 7.43 20.55 9.3 C 20.55 10.73 19.88 11.7 18.75 12.9 L 13.35 17.85 H 21.3", width = 1.9f, cap = StrokeCap.Butt)
    }
}

/** Benefit row mark "×2" (787363a9); drawn at 32. */
val PlataIcons.Times2: ImageVector get() = times2

private val times3 by lazy {
    plataGlyph("times3") {
        stroke("M 2.93 9.9 L 10.65 17.1 M 10.65 9.9 L 2.93 17.1", width = 1.8f)
        stroke("M 13.2 8.62 C 13.58 6.9 15 6 16.88 6 C 18.97 6 20.4 7.2 20.4 8.93 C 20.4 10.73 18.9 11.77 16.88 11.77 H 15.67 M 16.88 11.77 C 19.28 11.77 20.85 12.98 20.85 14.85 C 20.85 16.8 19.12 18 16.88 18 C 14.85 18 13.28 16.95 12.9 15.15", width = 1.9f, cap = StrokeCap.Butt)
    }
}

/** Benefit row mark "×3" (787363a9); drawn at 32. */
val PlataIcons.Times3: ImageVector get() = times3

private val smile by lazy {
    plataGlyph("smile") {
        stroke("M 2.57 12 A 9.43 9.43 0 1 0 21.43 12 A 9.43 9.43 0 1 0 2.57 12 Z", width = 1.7f)
        fill("M 7.89 9.86 A 1.29 1.46 0 1 0 10.46 9.86 A 1.29 1.46 0 1 0 7.89 9.86 Z")
        fill("M 13.63 9.86 A 1.29 1.46 0 1 0 16.2 9.86 A 1.29 1.46 0 1 0 13.63 9.86 Z")
        stroke("M 8.74 15.26 Q 12 17.66 15.26 15.26", width = 1.7f)
    }
}

/** ChoiceCard "All good!" (3cbc958b); drawn at 28. */
val PlataIcons.Smile: ImageVector get() = smile

private val dollarCircle by lazy {
    plataGlyph("dollar-circle") {
        stroke("M 2.36 12 A 9.64 9.64 0 1 0 21.64 12 A 9.64 9.64 0 1 0 2.36 12 Z", width = 1.7f)
        stroke("M 14.91 9.94 C 14.4 8.83 13.37 8.23 12 8.23 C 10.2 8.23 9.09 9.17 9.09 10.46 C 9.09 12 10.37 12.34 12 12.69 C 13.71 13.03 15 13.63 15 15 C 15 16.2 13.71 16.71 12 16.71 C 10.63 16.71 9.51 16.2 9 15.26", width = 1.7f)
        stroke("M 12 6.77 V 8.23 M 12 16.71 V 18.26", width = 1.7f)
    }
}

/** ChoiceCard "Custom amount" (3cbc958b); drawn at 28. */
val PlataIcons.DollarCircle: ImageVector get() = dollarCircle

private val clappingHands by lazy {
    plataGlyph("clapping-hands") {
        stroke("M 14.74 5.91 L 10.8 2.57 Q 9.69 0.64 8.23 3.69 Q 5.66 2.61 5.83 4.8 Q 2.49 4.59 3.6 6.26 Q 0.77 8.23 2.4 9.17 Q 1.37 13.03 2.06 13.11 C 2.91 14.49 5.31 17.23 7.54 19.29 C 9.77 21.34 11.49 22.2 13.54 22.2 C 16.63 22.2 19.2 20.83 20.74 18.6", width = 1.3f)
        stroke("M 8.23 3.69 L 15.43 11.49 M 5.83 4.8 L 11.57 11.49 M 3.6 6.26 L 9.94 13.63 M 2.4 9.17 L 8.23 15.69", width = 1.3f)
        stroke("M 14.74 5.91 C 15.26 5.14 16.37 4.54 17.31 4.37 C 18.6 4.11 19.54 4.54 20.14 5.57 C 21.26 7.63 22.29 10.37 22.97 13.11 C 23.49 15.51 22.8 17.4 20.74 18.6", width = 1.3f)
        stroke("M 16.97 5.4 C 17.83 7.63 18.86 10.37 19.71 13.11 C 20.31 15.09 20.4 16.71 19.89 18.09", width = 1.3f)
        stroke("M 14.74 6.09 L 15.51 11.4", width = 1.3f)
    }
}

/** ChoiceCard "Love it!" (3cbc958b); drawn at 28. */
val PlataIcons.ClappingHands: ImageVector get() = clappingHands

private val phoneDuotone by lazy {
    plataGlyph("phone-duotone", multicolour = true) {
        fill("M15.1 2.2 H8.9 C7.24 2.2 5.9 3.54 5.9 5.2 V18.8 C5.9 20.46 7.24 21.8 8.9 21.8 H15.1 C16.76 21.8 18.1 20.46 18.1 18.8 V5.2 C18.1 3.54 16.76 2.2 15.1 2.2 Z", brush = tint)
        stroke("M15.1 2.2 H8.9 C7.24 2.2 5.9 3.54 5.9 5.2 V18.8 C5.9 20.46 7.24 21.8 8.9 21.8 H15.1 C16.76 21.8 18.1 20.46 18.1 18.8 V5.2 C18.1 3.54 16.76 2.2 15.1 2.2 Z", width = 1.8f, brush = glyphGradient(2.2f, 21.8f))
        stroke("M11.2 18.6 H12.8", width = 2f, brush = glyphGradient(2.2f, 21.8f))
    }
}

/** Gradient outline with a glyph-tint fill: IconCircle neutral "Mobile" (a5a647ee). */
val PlataIcons.PhoneDuotone: ImageVector get() = phoneDuotone

private val cfe by lazy {
    plataGlyph("cfe", multicolour = true) {
        stroke("M7.51 9.24 C7.18 8.33 6.56 7.78 5.79 7.71 C5.02 7.64 4.17 8.06 3.44 8.87 C2.71 9.68 2.16 10.82 1.92 12 C1.68 13.18 1.78 14.32 2.19 15.13 C2.6 15.94 3.28 16.36 4.08 16.29 C4.87 16.22 5.71 15.67 6.4 14.76", width = 2.3f, brush = glyphGradient(6.55f, 17.45f), cap = StrokeCap.Butt)
        stroke("M11.81 6.55 L9.63 17.45 M10.43 7.7 L16.63 7.7 M10.74 11.9 L14.99 11.9 M18.46 6.55 L16.28 17.45 M17.08 7.7 L23.23 7.7 M17.39 11.9 L21.59 11.9 M15.36 16.3 L21.31 16.3", width = 2.3f, brush = glyphGradient(6.55f, 17.45f), cap = StrokeCap.Butt)
    }
}

/** Biller "Electricity" (a5a647ee). */
val PlataIcons.Cfe: ImageVector get() = cfe

private val water by lazy {
    plataGlyph("water", multicolour = true) {
        fill("M12 2.2 L18.03 9.02 A8.05 8.05 0 1 1 5.97 9.02 Z", brush = tint)
        stroke("M12 2.2 L18.03 9.02 A8.05 8.05 0 1 1 5.97 9.02 Z", width = 1.8f, brush = glyphGradient(2.2f, 18.1f))
        stroke("M16.6 15 Q16.3 17.5 13.6 18.1", width = 1.8f, brush = glyphGradient(2.2f, 18.1f))
    }
}

/** Biller "Water" (a5a647ee). */
val PlataIcons.Water: ImageVector get() = water

private val gas by lazy {
    plataGlyph("gas", multicolour = true) {
        fill("M10.4 10.7 L6.6 7.8 C5 9.8 3.6 11.6 3.6 13.9 A8.4 8.4 0 0 0 20.4 13.9 C20.4 10.9 17 6.4 14.3 2.4 Z", brush = tint)
        stroke("M10.4 10.7 L6.6 7.8 C5 9.8 3.6 11.6 3.6 13.9 A8.4 8.4 0 0 0 20.4 13.9 C20.4 10.9 17 6.4 14.3 2.4 Z", width = 1.8f, brush = glyphGradient(2.4f, 18.2f))
        stroke("M16.6 14.8 Q16.2 17.6 13.4 18.2", width = 1.8f, brush = glyphGradient(2.4f, 18.2f))
    }
}

/** Biller "Gas" (a5a647ee). */
val PlataIcons.Gas: ImageVector get() = gas

private val tv by lazy {
    plataGlyph("tv", multicolour = true) {
        fill("M4.55 5.4 H19.45 C20.83 5.4 21.95 6.52 21.95 7.9 V15.7 C21.95 17.08 20.83 18.2 19.45 18.2 H4.55 C3.17 18.2 2.05 17.08 2.05 15.7 V7.9 C2.05 6.52 3.17 5.4 4.55 5.4 Z", brush = tint)
        stroke("M4.55 5.4 H19.45 C20.83 5.4 21.95 6.52 21.95 7.9 V15.7 C21.95 17.08 20.83 18.2 19.45 18.2 H4.55 C3.17 18.2 2.05 17.08 2.05 15.7 V7.9 C2.05 6.52 3.17 5.4 4.55 5.4 Z", width = 1.8f, brush = glyphGradient(5.4f, 21f))
        stroke("M12 18.2 V21 M8.6 21 H15.4", width = 1.8f, brush = glyphGradient(5.4f, 21f))
    }
}

/** Biller "TV" (a5a647ee). */
val PlataIcons.Tv: ImageVector get() = tv

private val internet by lazy {
    plataGlyph("internet", multicolour = true) {
        fill("M2 6.55 C2 5.03 3.23 3.8 4.75 3.8 H19.25 C20.77 3.8 22 5.03 22 6.55 V17.45 C22 18.97 20.77 20.2 19.25 20.2 H4.75 C3.23 20.2 2 18.97 2 17.45 Z", brush = tint)
        stroke("M2 7.4 V6.55 C2 5.03 3.23 3.8 4.75 3.8 H19.25 C20.77 3.8 22 5.03 22 6.55 V17.45 C22 18.97 20.77 20.2 19.25 20.2 H14.6", width = 1.8f, brush = glyphGradient(3.8f, 20.2f))
        stroke("M2 11.4 A8.8 8.8 0 0 1 10.8 20.2 M2 15.4 A4.8 4.8 0 0 1 6.8 20.2", width = 1.8f, brush = glyphGradient(3.8f, 20.2f))
        fill("M1.35 19.6 A1.25 1.25 0 1 0 3.85 19.6 A1.25 1.25 0 1 0 1.35 19.6 Z", brush = glyphGradient(3.8f, 20.2f))
    }
}

/** Biller "Internet" (a5a647ee). */
val PlataIcons.Internet: ImageVector get() = internet

private val toll by lazy {
    plataGlyph("toll", multicolour = true) {
        fill("M8 2.95 L16 2.95 Q18.6 2.95 18.96 5.53 L20.74 18.47 Q21.1 21.05 18.5 21.05 L5.5 21.05 Q2.9 21.05 3.26 18.47 L5.04 5.53 Q5.4 2.95 8 2.95 Z", brush = tint)
        stroke("M8 2.95 L16 2.95 Q18.6 2.95 18.96 5.53 L20.74 18.47 Q21.1 21.05 18.5 21.05 L5.5 21.05 Q2.9 21.05 3.26 18.47 L5.04 5.53 Q5.4 2.95 8 2.95 Z", width = 1.8f, brush = glyphGradient(2.95f, 21.05f))
        stroke("M12 6.6 V7.3 M12 11.7 V12.4 M12 16.8 V17.5", width = 1.8f, brush = glyphGradient(2.95f, 21.05f))
    }
}

/** Biller "Toll" (a5a647ee). */
val PlataIcons.Toll: ImageVector get() = toll

private val parking by lazy {
    plataGlyph("parking", multicolour = true) {
        fill("M5.45 2.95 H18.55 C19.93 2.95 21.05 4.07 21.05 5.45 V18.55 C21.05 19.93 19.93 21.05 18.55 21.05 H5.45 C4.07 21.05 2.95 19.93 2.95 18.55 V5.45 C2.95 4.07 4.07 2.95 5.45 2.95 Z", brush = tint)
        stroke("M5.45 2.95 H18.55 C19.93 2.95 21.05 4.07 21.05 5.45 V18.55 C21.05 19.93 19.93 21.05 18.55 21.05 H5.45 C4.07 21.05 2.95 19.93 2.95 18.55 V5.45 C2.95 4.07 4.07 2.95 5.45 2.95 Z", width = 1.8f, brush = glyphGradient(2.95f, 21.05f))
        stroke("M9.85 16.6 V8.4 H12.1 A2.35 2.35 0 0 1 12.1 13.1 H9.85", width = 2f, brush = glyphGradient(2.95f, 21.05f))
    }
}

/** Biller "Parking" (a5a647ee). */
val PlataIcons.Parking: ImageVector get() = parking

private val topUp by lazy {
    plataGlyph("top-up", multicolour = true) {
        fill("M 8.47 11.92 A 6.82 6.82 0 1 0 22.12 11.92 A 6.82 6.82 0 1 0 8.47 11.92 Z", brush = orangeTint)
        stroke("M 8.47 11.92 A 6.82 6.82 0 1 0 22.12 11.92 A 6.82 6.82 0 1 0 8.47 11.92 Z", width = 2.3f, brush = orange)
        fill("M 8.36 4.5 L 10.39 4.5 Q 11.59 4.5 11.59 5.7 L 11.59 8.77 Q 11.59 9.38 12.19 9.38 L 15.26 9.38 Q 16.46 9.38 16.46 10.57 L 16.46 12.6 Q 16.46 13.8 15.26 13.8 L 12.19 13.8 Q 11.59 13.8 11.59 14.4 L 11.59 17.48 Q 11.59 18.67 10.39 18.67 L 8.36 18.67 Q 7.16 18.67 7.16 17.48 L 7.16 14.4 Q 7.16 13.8 6.56 13.8 L 3.49 13.8 Q 2.29 13.8 2.29 12.6 L 2.29 10.57 Q 2.29 9.38 3.49 9.38 L 6.56 9.38 Q 7.16 9.38 7.16 8.77 L 7.16 5.7 Q 7.16 4.5 8.36 4.5 Z", brush = tint)
        stroke("M 8.36 4.5 L 10.39 4.5 Q 11.59 4.5 11.59 5.7 L 11.59 8.77 Q 11.59 9.38 12.19 9.38 L 15.26 9.38 Q 16.46 9.38 16.46 10.57 L 16.46 12.6 Q 16.46 13.8 15.26 13.8 L 12.19 13.8 Q 11.59 13.8 11.59 14.4 L 11.59 17.48 Q 11.59 18.67 10.39 18.67 L 8.36 18.67 Q 7.16 18.67 7.16 17.48 L 7.16 14.4 Q 7.16 13.8 6.56 13.8 L 3.49 13.8 Q 2.29 13.8 2.29 12.6 L 2.29 10.57 Q 2.29 9.38 3.49 9.38 L 6.56 9.38 Q 7.16 9.38 7.16 8.77 L 7.16 5.7 Q 7.16 4.5 8.36 4.5 Z", width = 2.3f, brush = glyphGradient(4.5f, 18.67f))
    }
}

/** Two-tone ActionTile glyph (5e61179c, a5a647ee); drawn at 32. */
val PlataIcons.TopUp: ImageVector get() = topUp

private val billPay by lazy {
    plataGlyph("bill-pay", multicolour = true) {
        fill("M 3.91 7.09 A 3.26 3.26 0 1 0 10.43 7.09 A 3.26 3.26 0 1 0 3.91 7.09 Z", brush = tint)
        stroke("M 3.91 7.09 A 3.26 3.26 0 1 0 10.43 7.09 A 3.26 3.26 0 1 0 3.91 7.09 Z", width = 2.2f, brush = glyphGradient(7.09f, 16.95f))
        fill("M 13.54 7.09 A 3.26 3.26 0 1 0 20.06 7.09 A 3.26 3.26 0 1 0 13.54 7.09 Z", brush = orangeTint)
        stroke("M 13.54 7.09 A 3.26 3.26 0 1 0 20.06 7.09 A 3.26 3.26 0 1 0 13.54 7.09 Z", width = 2.2f, brush = orange)
        fill("M 3.91 16.95 A 3.26 3.26 0 1 0 10.43 16.95 A 3.26 3.26 0 1 0 3.91 16.95 Z", brush = tint)
        stroke("M 3.91 16.95 A 3.26 3.26 0 1 0 10.43 16.95 A 3.26 3.26 0 1 0 3.91 16.95 Z", width = 2.2f, brush = glyphGradient(7.09f, 16.95f))
        fill("M 13.54 16.95 A 3.26 3.26 0 1 0 20.06 16.95 A 3.26 3.26 0 1 0 13.54 16.95 Z", brush = tint)
        stroke("M 13.54 16.95 A 3.26 3.26 0 1 0 20.06 16.95 A 3.26 3.26 0 1 0 13.54 16.95 Z", width = 2.2f, brush = glyphGradient(7.09f, 16.95f))
    }
}

/** Two-tone ActionTile glyph (5e61179c); drawn at 32. */
val PlataIcons.BillPay: ImageVector get() = billPay

private val transfer by lazy {
    plataGlyph("transfer", multicolour = true) {
        fill("M 13.49 6.33 L 18.83 10.1 Q 21.52 12 18.83 13.9 L 13.49 17.67 Q 10.8 19.58 10.8 16.27 L 10.8 7.73 Q 10.8 4.43 13.49 6.33 Z", brush = orangeTint)
        stroke("M 13.49 6.33 L 18.83 10.1 Q 21.52 12 18.83 13.9 L 13.49 17.67 Q 10.8 19.58 10.8 16.27 L 10.8 7.73 Q 10.8 4.43 13.49 6.33 Z", width = 2.3f, brush = orange)
        fill("M 5.97 6.26 L 11.81 10.16 Q 14.55 12 11.81 13.84 L 5.97 17.74 Q 3.22 19.58 3.22 16.27 L 3.22 7.73 Q 3.22 4.43 5.97 6.26 Z", brush = tint)
        stroke("M 5.97 6.26 L 11.81 10.16 Q 14.55 12 11.81 13.84 L 5.97 17.74 Q 3.22 19.58 3.22 16.27 L 3.22 7.73 Q 3.22 4.43 5.97 6.26 Z", width = 2.3f, brush = glyphGradient(4.43f, 19.58f))
    }
}

/** Two-tone ActionTile glyph (5e61179c, a5a647ee, e94a49a9); drawn at 32. */
val PlataIcons.Transfer: ImageVector get() = transfer

private val swapArrows by lazy {
    plataGlyph("swap-arrows") {
        stroke("M 6.86 5.53 L 2.31 10.24 L 7.2 14.87 M 17.31 9.13 L 21.69 13.76 L 17.31 18.47", width = 2f)
        stroke("M 6.6 8.53 H 14.31 M 6.34 11.87 H 20.66 M 10.2 15.73 H 19.37", width = 2f)
    }
}

/** "To my Plata account" row (99ceac12); tint with `gradients.glyph` via brushTint; drawn at 28. */
val PlataIcons.SwapArrows: ImageVector get() = swapArrows

private val baby by lazy {
    plataGlyph("baby") {
        stroke("M4.25 11.05 A7.75 7.75 0 1 0 19.75 11.05 A7.75 7.75 0 1 0 4.25 11.05 Z", width = 1.8f)
        stroke("M4.5 10 C1 10.1 1 14.6 4.7 14.8 M19.5 10 C23 10.1 23 14.6 19.3 14.8", width = 1.5f)
        stroke("M7 17.4 C5.9 18.4 5.3 19.8 5.2 21.6 M17 17.4 C18.1 18.4 18.7 19.8 18.8 21.6", width = 1.8f)
        stroke("M11 3.5 C10.2 5.5 11.3 7.6 12.9 7.4 C14 7.2 13.9 5.6 12.9 5.6", width = 1.6f)
        fill("M8.65 12 A1.15 1.15 0 1 0 10.95 12 A1.15 1.15 0 1 0 8.65 12 Z")
        fill("M13.15 12 A1.15 1.15 0 1 0 15.45 12 A1.15 1.15 0 1 0 13.15 12 Z")
    }
}

/** Category "Children's stores" (e7edfe4c). */
val PlataIcons.Baby: ImageVector get() = baby

private val tShirt by lazy {
    plataGlyph("tshirt") {
        stroke("M8.6 3.3 C9.6 5.7 14.4 5.7 15.4 3.3 L18.8 3.6 C19.6 3.7 20.2 4.2 20.5 5 L21.9 9.4 C22.1 10 21.8 10.5 21.2 10.6 L18.1 11.2 V19.8 C18.1 20.5 17.6 21 16.9 21 H7.1 C6.4 21 5.9 20.5 5.9 19.8 V11.2 L2.8 10.6 C2.2 10.5 1.9 10 2.1 9.4 L3.5 5 C3.8 4.2 4.4 3.7 5.2 3.6 Z", width = 1.8f)
        stroke("M6 8.2 V11 M18 8.2 V11", width = 1.8f)
    }
}

/** Category "Clothing" (e7edfe4c). */
val PlataIcons.TShirt: ImageVector get() = tShirt

private val homeDiy by lazy {
    plataGlyph("home-diy") {
        stroke("M3.6 18.3 V9.9 C3.6 9.3 3.9 8.8 4.3 8.4 L10.7 3.8 C11.5 3.2 12.5 3.2 13.3 3.8 L19.7 8.4 C20.1 8.8 20.4 9.3 20.4 9.9 V18.3 C20.4 19.1 19.7 19.8 18.9 19.8 H5.1 C4.3 19.8 3.6 19.1 3.6 18.3 Z", width = 1.8f)
        stroke("M18.4 3.6 V6.2", width = 1.8f)
        stroke("M10.3 11 H13.7 C14 11 14.3 11.3 14.3 11.6 V14.7 C14.3 15 14 15.3 13.7 15.3 H10.3 C10 15.3 9.7 15 9.7 14.7 V11.6 C9.7 11.3 10 11 10.3 11 Z", width = 1.8f)
    }
}

/** Category "Home and DIY" (e7edfe4c). */
val PlataIcons.HomeDiy: ImageVector get() = homeDiy

private val globe by lazy {
    plataGlyph("globe") {
        stroke("M2.95 12 A9.05 9.05 0 1 0 21.05 12 A9.05 9.05 0 1 0 2.95 12 Z", width = 1.8f)
        stroke("M7.6 12 A4.4 9.05 0 1 0 16.4 12 A4.4 9.05 0 1 0 7.6 12 Z", width = 1.8f)
        stroke("M12 2.95 V21.05 M3.6 8.9 H20.4 M3.6 15.1 H20.4", width = 1.8f)
    }
}

/** Category "Mobile, TV and Internet" (e7edfe4c). */
val PlataIcons.Globe: ImageVector get() = globe

private val volleyball by lazy {
    plataGlyph("volleyball") {
        stroke("M3 12 A9 9 0 1 0 21 12 A9 9 0 1 0 3 12 Z", width = 1.7f)
        stroke("M8.6 4 C10.6 6 12 8.8 12 12 M12 12 H21 M12 12 C9.6 13.6 8 16.4 8.1 20.5", width = 1.7f)
        stroke("M14.1 4.3 C15.9 6.3 17 8.8 17.2 12 M9.4 8.1 C6.9 9.6 5 12.4 4.4 15.6 M9 15.6 C12.6 16.8 16.6 16.6 20.3 15.3", width = 1.7f)
    }
}

/** Category / Chip "Sporting goods" (e7edfe4c, 9e2d2593, 9356b352). */
val PlataIcons.Volleyball: ImageVector get() = volleyball

private val taxi by lazy {
    plataGlyph("taxi") {
        stroke("M10.2 5.6 V4.6 C10.2 4 10.7 3.5 11.3 3.5 H12.7 C13.3 3.5 13.8 4 13.8 4.6 V5.6", width = 1.6f)
        stroke("M5.4 12.5 L6.8 7.2 C7 6.3 7.8 5.6 8.8 5.6 H15.2 C16.2 5.6 17 6.3 17.2 7.2 L18.6 12.5", width = 1.8f)
        stroke("M3 14.5 C3 13.4 3.9 12.5 5 12.5 H19 C20.1 12.5 21 13.4 21 14.5 V17.4 C21 18.2 20.4 18.8 19.6 18.8 H4.4 C3.6 18.8 3 18.2 3 17.4 Z", width = 1.8f)
        stroke("M5.6 18.8 V20.2 C5.6 20.9 6.1 21.4 6.8 21.4 H7.6 C8.3 21.4 8.8 20.9 8.8 20.2 V18.8 M15.2 18.8 V20.2 C15.2 20.9 15.7 21.4 16.4 21.4 H17.2 C17.9 21.4 18.4 20.9 18.4 20.2 V18.8", width = 1.8f)
        stroke("M2.4 10.7 H4.6 M19.4 10.7 H21.6 M6.6 14.4 V15.4 M17.4 14.4 V15.4", width = 1.6f)
    }
}

/** Category "Taxi" (e7edfe4c). */
val PlataIcons.Taxi: ImageVector get() = taxi

private val coins by lazy {
    plataGlyph("coins") {
        stroke("M 10.6 5.1 A 6.22 2.84 0 1 0 23.04 5.1 A 6.22 2.84 0 1 0 10.6 5.1 Z", width = 1.8f)
        stroke("M 23.04 5.1 V 13.04 M 10.6 5.1 V 10.57", width = 1.8f)
        stroke("M 11.14 10.23 A 6.22 2.84 0 0 0 23.04 9.07 M 14.46 15.66 A 6.22 2.84 0 0 0 23.04 13.04", width = 1.8f)
        stroke("M 0.96 12.66 A 6.27 2.47 0 1 0 13.5 12.66 A 6.27 2.47 0 1 0 0.96 12.66 Z", width = 1.8f)
        stroke("M 0.96 12.66 V 19.2 M 13.5 12.66 V 19.2", width = 1.8f)
        stroke("M 0.96 15.93 A 6.27 2.47 0 0 0 13.5 15.93 M 0.96 19.2 A 6.27 2.47 0 0 0 13.5 19.2", width = 1.8f)
    }
}

/** Portfolio "Cash" tile (b9c441c0). */
val PlataIcons.Coins: ImageVector get() = coins

private val shieldHalf by lazy {
    plataGlyph("shield-half") {
        stroke("M12 2.6 L19.2 5.2 C19.7 5.4 20.1 5.9 20.1 6.5 V14.3 C20.1 15.2 19.7 16 19 16.5 L13.1 20.8 C12.4 21.3 11.6 21.3 10.9 20.8 L5 16.5 C4.3 16 3.9 15.2 3.9 14.3 V6.5 C3.9 5.9 4.3 5.4 4.8 5.2 Z", width = 2f)
        fill("M12 2.6 L19.2 5.2 C19.7 5.4 20.1 5.9 20.1 6.5 V14.3 C20.1 15.2 19.7 16 19 16.5 L13.1 20.8 C12.7 21.1 12.4 21.2 12 21.2 Z")
    }
}

/** Portfolio "Fixed income" tile (b9c441c0). */
val PlataIcons.ShieldHalf: ImageVector get() = shieldHalf

private val beauty by lazy {
    plataGlyph("beauty") {
        stroke("M4.7 3.5 C4.7 7 6.5 10.5 9.6 16.8 M10.5 3.5 C10.5 7 8.7 10.5 5.6 16.8", width = 1.6f)
        stroke("M2.9 18.4 A1.6 1.6 0 1 0 6.1 18.4 A1.6 1.6 0 1 0 2.9 18.4 Z", width = 1.6f)
        stroke("M9.1 18.4 A1.6 1.6 0 1 0 12.3 18.4 A1.6 1.6 0 1 0 9.1 18.4 Z", width = 1.6f)
        stroke("M15.6 3.5 V20.8 M15.6 3.75 H20 M15.6 6.9 H20 M15.6 9.9 H20 M15.6 13 H20 M15.6 16 H20", width = 1.6f)
    }
}

/** Chip "Beauty" (816019bf, 9e2d2593, 9356b352). */
val PlataIcons.Beauty: ImageVector get() = beauty

private val carRental by lazy {
    plataGlyph("car-rental") {
        stroke("M4.4 8.9 L6.4 5.4 C7.1 4.3 8.2 3.7 9.5 3.7 H14.5 C15.8 3.7 16.9 4.3 17.6 5.4 L19.6 8.9", width = 1.8f)
        stroke("M1.9 11.9 C1.9 10.2 3.2 8.9 4.9 8.9 H19.1 C20.8 8.9 22.1 10.2 22.1 11.9 V15.3 C22.1 16.2 21.4 17 20.5 17 H3.5 C2.6 17 1.9 16.2 1.9 15.3 Z", width = 1.8f)
        stroke("M2 11.4 Q12 12.6 22 11.4", width = 1.8f)
        stroke("M4.6 17 V19.4 C4.6 19.8 5 20.2 5.4 20.2 H6.8 C7.2 20.2 7.6 19.8 7.6 19.4 V17 M16.4 17 V19.4 C16.4 19.8 16.8 20.2 17.2 20.2 H18.6 C19 20.2 19.4 19.8 19.4 19.4 V17", width = 1.8f)
        fill("M4.65 14.6 A0.95 0.95 0 1 0 6.55 14.6 A0.95 0.95 0 1 0 4.65 14.6 Z")
        fill("M17.45 14.6 A0.95 0.95 0 1 0 19.35 14.6 A0.95 0.95 0 1 0 17.45 14.6 Z")
    }
}

/** Chip "Car rental" (816019bf). */
val PlataIcons.CarRental: ImageVector get() = carRental

private val houseDollar by lazy {
    plataGlyph("house-dollar") {
        stroke("M3.3 9.6 C3.3 9 3.6 8.4 4.1 8 L10.6 3.6 C11.4 3 12.6 3 13.4 3.6 L19.9 8 C20.4 8.4 20.7 9 20.7 9.6 V18.7 C20.7 19.8 19.8 20.7 18.7 20.7 H5.3 C4.2 20.7 3.3 19.8 3.3 18.7 Z", width = 1.8f)
        stroke("M14.3 11.1 C13.9 10.4 13.1 10 12 10 C10.6 10 9.7 10.7 9.7 11.7 C9.7 12.8 10.6 13.1 12 13.4 C13.5 13.7 14.4 14.2 14.4 15.2 C14.4 16.3 13.4 16.9 12 16.9 C10.8 16.9 9.9 16.4 9.5 15.6", width = 1.8f)
        stroke("M12 8.6 V10 M12 16.9 V18.2", width = 1.8f)
    }
}

/** IconCircle filled (d58c1486). */
val PlataIcons.HouseDollar: ImageVector get() = houseDollar

private val msi by lazy {
    plataGlyph("msi") {
        fill("M2 12 A10 10 0 0 1 5 4.86 L5 19.14 A10 10 0 0 1 2 12 Z", brush = SolidColor(PlataPalette.Ink.copy(alpha = 0.38f)))
        fill("M7 3.34 A10 10 0 0 1 11 2.05 L11 21.95 A10 10 0 0 1 7 20.66 Z", brush = SolidColor(PlataPalette.Ink.copy(alpha = 0.56f)))
        fill("M13 2.05 A10 10 0 0 1 17 3.34 L17 20.66 A10 10 0 0 1 13 21.95 Z", brush = SolidColor(PlataPalette.Ink.copy(alpha = 0.78f)))
        fill("M19 4.86 A10 10 0 0 1 22 12 L22 12 A10 10 0 0 1 19 19.14 Z", brush = SolidColor(PlataPalette.Ink.copy(alpha = 1f)))
    }
}

/** MSI mark: four stripes at stepped opacity, so a tint or brushTint keeps the steps (9f1aa9fc, 5e61179c); drawn at 17 beside a 15pt title. */
val PlataIcons.Msi: ImageVector get() = msi

private val msiBadge by lazy {
    plataGlyph("msi-badge", multicolour = true) {
        fill("M0 12 A12 12 0 1 0 24 12 A12 12 0 1 0 0 12 Z", brush = msiGradient)
        fill("M5.75 12 A6.25 6.25 0 0 1 7.62 7.54 L7.62 16.46 A6.25 6.25 0 0 1 5.75 12 Z", brush = SolidColor(PlataPalette.White.copy(alpha = 0.38f)))
        fill("M8.88 6.59 A6.25 6.25 0 0 1 11.38 5.78 L11.38 18.22 A6.25 6.25 0 0 1 8.88 17.41 Z", brush = SolidColor(PlataPalette.White.copy(alpha = 0.56f)))
        fill("M12.62 5.78 A6.25 6.25 0 0 1 15.12 6.59 L15.12 17.41 A6.25 6.25 0 0 1 12.62 18.22 Z", brush = SolidColor(PlataPalette.White.copy(alpha = 0.78f)))
        fill("M16.38 7.54 A6.25 6.25 0 0 1 18.25 12 L18.25 12 A6.25 6.25 0 0 1 16.38 16.46 Z", brush = white)
    }
}

/** MSI badge: gradient disc with the white MSI mark, any size (894b1b01 40, d7d78e0b 36, d55a4d32 96). */
val PlataIcons.MsiBadge: ImageVector get() = msiBadge

private val doubleChevronDuotone by lazy {
    plataGlyph("double-chevron-duotone", multicolour = true) {
        fill("M14.08 6.05 L19.42 9.65 Q22.9 12 19.42 14.35 L14.08 17.95 Q10.6 20.3 10.6 16.1 L10.6 7.9 Q10.6 3.7 14.08 6.05 Z", brush = tint)
        stroke("M14.08 6.05 L19.42 9.65 Q22.9 12 19.42 14.35 L14.08 17.95 Q10.6 20.3 10.6 16.1 L10.6 7.9 Q10.6 3.7 14.08 6.05 Z", width = 1.9f, brush = glyphGradient(3.7f, 20.3f))
        fill("M6.58 6.05 L11.92 9.65 Q15.4 12 11.92 14.35 L6.58 17.95 Q3.1 20.3 3.1 16.1 L3.1 7.9 Q3.1 3.7 6.58 6.05 Z", brush = tint)
        stroke("M6.58 6.05 L11.92 9.65 Q15.4 12 11.92 14.35 L6.58 17.95 Q3.1 20.3 3.1 16.1 L3.1 7.9 Q3.1 3.7 6.58 6.05 Z", width = 1.9f, brush = glyphGradient(3.7f, 20.3f))
    }
}

/** Gradient outline with a glyph-tint fill: IconCircle neutral "By CLABE" (0b2e699a, 99ceac12). */
val PlataIcons.DoubleChevronDuotone: ImageVector get() = doubleChevronDuotone

private val plusOutlineDuotone by lazy {
    plataGlyph("plus-outline-duotone", multicolour = true) {
        fill("M 9.5 4.5 C 9.5 4.102 9.658 3.721 9.939 3.439 C 10.221 3.158 10.602 3 11 3 H 13 C 13.398 3 13.779 3.158 14.061 3.439 C 14.342 3.721 14.5 4.102 14.5 4.5 V 9.5 H 19.5 C 19.898 9.5 20.279 9.658 20.561 9.939 C 20.842 10.221 21 10.602 21 11 V 13 C 21 13.398 20.842 13.779 20.561 14.061 C 20.279 14.342 19.898 14.5 19.5 14.5 H 14.5 V 19.5 C 14.5 19.898 14.342 20.279 14.061 20.561 C 13.779 20.842 13.398 21 13 21 H 11 C 10.602 21 10.221 20.842 9.939 20.561 C 9.658 20.279 9.5 19.898 9.5 19.5 V 14.5 H 4.5 C 4.102 14.5 3.721 14.342 3.439 14.061 C 3.158 13.779 3 13.398 3 13 V 11 C 3 10.602 3.158 10.221 3.439 9.939 C 3.721 9.658 4.102 9.5 4.5 9.5 H 9.5 V 4.5 Z", brush = tint)
        stroke("M 9.5 4.5 C 9.5 4.102 9.658 3.721 9.939 3.439 C 10.221 3.158 10.602 3 11 3 H 13 C 13.398 3 13.779 3.158 14.061 3.439 C 14.342 3.721 14.5 4.102 14.5 4.5 V 9.5 H 19.5 C 19.898 9.5 20.279 9.658 20.561 9.939 C 20.842 10.221 21 10.602 21 11 V 13 C 21 13.398 20.842 13.779 20.561 14.061 C 20.279 14.342 19.898 14.5 19.5 14.5 H 14.5 V 19.5 C 14.5 19.898 14.342 20.279 14.061 20.561 C 13.779 20.842 13.398 21 13 21 H 11 C 10.602 21 10.221 20.842 9.939 20.561 C 9.658 20.279 9.5 19.898 9.5 19.5 V 14.5 H 4.5 C 4.102 14.5 3.721 14.342 3.439 14.061 C 3.158 13.779 3 13.398 3 13 V 11 C 3 10.602 3.158 10.221 3.439 9.939 C 3.721 9.658 4.102 9.5 4.5 9.5 H 9.5 V 4.5 Z", width = 2.2f, brush = glyphGradient(3f, 21f))
    }
}

/** Gradient outline with a glyph-tint fill: IconButton tinted (537a4b5a, 99ceac12). */
val PlataIcons.PlusOutlineDuotone: ImageVector get() = plusOutlineDuotone

private val questionMark by lazy {
    plataGlyph("question-mark") {
        stroke("M7.6 8.2 C7.6 5.4 9.6 3.6 12.1 3.6 C14.7 3.6 16.6 5.3 16.6 7.7 C16.6 9.7 15.4 10.7 13.9 11.6 C12.7 12.4 12.1 13.2 12.1 14.6 V15.2", width = 3.6f)
        fill("M10 19.7 A2.1 2.1 0 1 0 14.2 19.7 A2.1 2.1 0 1 0 10 19.7 Z")
    }
}

/** Bare question mark for the grey IconButton 20 (84e28520, e7edfe4c); Question has a ring. */
val PlataIcons.QuestionMark: ImageVector get() = questionMark

private val personPlaceholder by lazy {
    plataGlyph("person-placeholder") {
        stroke("M7.18 6.3 A4.82 4.82 0 1 0 16.82 6.3 A4.82 4.82 0 1 0 7.18 6.3 Z", width = 1.85f)
        stroke("M4.45 22.55 A7.5 7.5 0 0 1 19.55 22.55 Z", width = 1.85f)
    }
}

/** Avatar placeholder bust: ring head over a closed half-dome (1d655cd9, ec4a6b30, b95e6964). */
val PlataIcons.PersonPlaceholder: ImageVector get() = personPlaceholder

private val applePayMark by lazy {
    plataGlyph("apple-pay-mark", width = 21f, height = 13f, multicolour = true) {
        fill("M2.5 0.5 H18.5 A2 2 0 0 1 20.5 2.5 V10.5 A2 2 0 0 1 18.5 12.5 H2.5 A2 2 0 0 1 0.5 10.5 V2.5 A2 2 0 0 1 2.5 0.5 Z", brush = white)
        stroke("M2.5 0.5 H18.5 A2 2 0 0 1 20.5 2.5 V10.5 A2 2 0 0 1 18.5 12.5 H2.5 A2 2 0 0 1 0.5 10.5 V2.5 A2 2 0 0 1 2.5 0.5 Z", width = 0.8f, brush = SolidColor(PlataPalette.Ink.copy(alpha = 0.7f)))
        fill("M4.6 4.6 C3.4 4 2.6 5 2.6 6.4 C2.6 7.9 3.5 9.6 4.4 9.6 C4.8 9.6 4.9 9.4 5.3 9.4 C5.7 9.4 5.8 9.6 6.2 9.6 C6.9 9.6 7.4 8.6 7.6 8 C7 7.7 6.7 7.1 6.7 6.5 C6.7 5.9 7 5.4 7.4 5.1 C7 4.5 6.4 4.4 6 4.4 C5.4 4.4 5 4.7 4.6 4.6 Z", brush = SolidColor(PlataPalette.Ink.copy(alpha = 1f)))
        fill("M5.4 4 C5.4 3.3 5.9 2.8 6.5 2.7 C6.5 3.4 6 3.9 5.4 4 Z", brush = SolidColor(PlataPalette.Ink.copy(alpha = 1f)))
        stroke("M8.9 9.3 V4.1 H10.3 A1.35 1.35 0 0 1 10.3 6.8 H8.9 M14.4 6.8 V9.3 M15.6 6.8 L17.1 9.3 M18.6 6.8 L16.8 10.6 Q16.5 11.1 16 11", width = 0.85f, brush = SolidColor(PlataPalette.Ink.copy(alpha = 1f)))
        stroke("M11.95 8.05 A1.2 1.2 0 1 0 14.35 8.05 A1.2 1.2 0 1 0 11.95 8.05 Z", width = 0.85f, brush = SolidColor(PlataPalette.Ink.copy(alpha = 1f)))
    }
}

/** Apple Pay acceptance mark on the physical CardThumb, 21×13 (025e71c5, 9f1aa9fc). */
val PlataIcons.ApplePayMark: ImageVector get() = applePayMark

private val chevronLeftHeavy by lazy {
    plataGlyph("chevron-left-heavy") {
        stroke("M15.1 4.9 L8.7 12 L15.1 19.1", width = 2.6f)
    }
}

/** Back chevron for the glass IconButton over media: the source stroke is 1.9pt at 20 (816019bf). */
val PlataIcons.ChevronLeftHeavy: ImageVector get() = chevronLeftHeavy

private val personPlaceholderMedium by lazy {
    plataGlyph("person-placeholder-medium") {
        stroke("M7.97 7.54 A4.03 4.03 0 1 0 16.03 7.54 A4.03 4.03 0 1 0 7.97 7.54 Z", width = 1.97f)
        stroke("M3.3 22 A8.65 6.97 0 0 1 20.6 22 Z", width = 1.97f)
    }
}

/** Avatar Medium (56) placeholder bust: wider and lower than the 96 one (ec4a6b30, b95e6964). */
val PlataIcons.PersonPlaceholderMedium: ImageVector get() = personPlaceholderMedium

private val mastercard by lazy {
    plataGlyph("mastercard", width = 17f, height = 11f, multicolour = true) {
        fill("M 0 5.5 A 5.5 5.5 0 1 0 11 5.5 A 5.5 5.5 0 1 0 0 5.5 Z", brush = SolidColor(PlataPalette.MastercardRed))
        fill("M 6 5.5 A 5.5 5.5 0 1 0 17 5.5 A 5.5 5.5 0 1 0 6 5.5 Z", brush = SolidColor(PlataPalette.MastercardYellow))
        fill("M 8.5 0.89 A 5.5 5.5 0 0 1 8.5 10.11 A 5.5 5.5 0 0 1 8.5 0.89 Z", brush = SolidColor(PlataPalette.MastercardOverlap))
    }
}

/** Card network mark on CardThumb, 17×11 (Figma CardThumb 86:144). */
val PlataIcons.Mastercard: ImageVector get() = mastercard

/** Every glyph in this file, for the icon sheet. */
val PlataIcons.sourceGlyphs: List<Pair<String, ImageVector>>
    get() = listOf(
        "crown" to Crown,
        "times2" to Times2,
        "times3" to Times3,
        "smile" to Smile,
        "dollar-circle" to DollarCircle,
        "clapping-hands" to ClappingHands,
        "phone-duotone" to PhoneDuotone,
        "cfe" to Cfe,
        "water" to Water,
        "gas" to Gas,
        "tv" to Tv,
        "internet" to Internet,
        "toll" to Toll,
        "parking" to Parking,
        "top-up" to TopUp,
        "bill-pay" to BillPay,
        "transfer" to Transfer,
        "swap-arrows" to SwapArrows,
        "baby" to Baby,
        "tshirt" to TShirt,
        "home-diy" to HomeDiy,
        "globe" to Globe,
        "volleyball" to Volleyball,
        "taxi" to Taxi,
        "coins" to Coins,
        "shield-half" to ShieldHalf,
        "beauty" to Beauty,
        "car-rental" to CarRental,
        "house-dollar" to HouseDollar,
        "msi" to Msi,
        "msi-badge" to MsiBadge,
        "mastercard" to Mastercard,
        "double-chevron-duotone" to DoubleChevronDuotone,
        "plus-outline-duotone" to PlusOutlineDuotone,
        "question-mark" to QuestionMark,
        "person-placeholder" to PersonPlaceholder,
        "apple-pay-mark" to ApplePayMark,
        "chevron-left-heavy" to ChevronLeftHeavy,
        "person-placeholder-medium" to PersonPlaceholderMedium,
    )
