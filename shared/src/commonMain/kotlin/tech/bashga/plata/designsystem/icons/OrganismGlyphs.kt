package tech.bashga.plata.designsystem.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import tech.bashga.plata.designsystem.theme.PlataPalette

private const val ARROW_UP_RIGHT_OUTLINE =
    "M 9.7 4.9 L 20.9 3.5 L 20.5 14.6 L 16.4 10.95 L 7.2 20.1 A 1.7 1.7 0 0 1 4.8 17.7 L 14 8.55 Z"

private val arrowUpRightDuotone by lazy {
    plataGlyph("arrow-up-right-duotone", multicolour = true) {
        fill(ARROW_UP_RIGHT_OUTLINE, brush = SolidColor(PlataPalette.GlyphTint))
        stroke(
            ARROW_UP_RIGHT_OUTLINE,
            width = 2.2f,
            brush = Brush.linearGradient(
                0f to PlataPalette.GlyphGradientTop,
                1f to PlataPalette.GlyphGradientBottom,
                start = Offset(12f, 3.5f),
                end = Offset(12f, 20.1f),
            ),
        )
    }
}

/** Gradient outline arrow with a glyph-tint fill: the tinted "withdraw" IconButton on the Cash card (b658c9da). */
val PlataIcons.ArrowUpRightDuotone: ImageVector get() = arrowUpRightDuotone

private val calendarGrid by lazy {
    plataGlyph("calendar-grid") {
        fill(
            "M 5.7 3 H 18 A 3.2 3.2 0 0 1 21.2 6.2 V 17.5 A 3.2 3.2 0 0 1 18 20.7 H 5.7 A 3.2 3.2 0 0 1 2.5 17.5 V 6.2 A 3.2 3.2 0 0 1 5.7 3 Z " +
                "M 6 8 H 18.3 A 1.5 1.5 0 0 1 19.8 9.5 V 17.8 A 1.5 1.5 0 0 1 18.3 19.3 H 6 A 1.5 1.5 0 0 1 4.5 17.8 V 9.5 A 1.5 1.5 0 0 1 6 8 Z",
            evenOdd = true,
        )
        fill("M 9.60 10.25 A 0.8 0.8 0 1 0 11.20 10.25 A 0.8 0.8 0 1 0 9.60 10.25 Z M 12.70 10.25 A 0.8 0.8 0 1 0 14.30 10.25 A 0.8 0.8 0 1 0 12.70 10.25 Z M 15.60 10.25 A 0.8 0.8 0 1 0 17.20 10.25 A 0.8 0.8 0 1 0 15.60 10.25 Z M 6.70 13.25 A 0.8 0.8 0 1 0 8.30 13.25 A 0.8 0.8 0 1 0 6.70 13.25 Z M 9.60 13.25 A 0.8 0.8 0 1 0 11.20 13.25 A 0.8 0.8 0 1 0 9.60 13.25 Z M 12.70 13.25 A 0.8 0.8 0 1 0 14.30 13.25 A 0.8 0.8 0 1 0 12.70 13.25 Z M 15.60 13.25 A 0.8 0.8 0 1 0 17.20 13.25 A 0.8 0.8 0 1 0 15.60 13.25 Z M 6.70 16.2 A 0.8 0.8 0 1 0 8.30 16.2 A 0.8 0.8 0 1 0 6.70 16.2 Z M 9.60 16.2 A 0.8 0.8 0 1 0 11.20 16.2 A 0.8 0.8 0 1 0 9.60 16.2 Z M 12.70 16.2 A 0.8 0.8 0 1 0 14.30 16.2 A 0.8 0.8 0 1 0 12.70 16.2 Z")
    }
}

/** Calendar with a filled header band and a 3-4-3 dot grid: the "Add to calendar" action (ec4a6b30, 3b3eaed7). */
val PlataIcons.CalendarGrid: ImageVector get() = calendarGrid

private const val PHONE_HANDSET =
    "M 6.2 3.2 C 7.2 2.9 7.9 3.4 8.4 4.4 L 9.6 7 C 9.9 7.7 9.7 8.5 9.1 9 L 8.7 9.4 C 8.4 9.7 8.4 10.2 8.6 10.6 " +
        "C 9.8 12.6 11.4 14.2 13.4 15.4 C 13.8 15.6 14.3 15.6 14.6 15.3 L 15 14.9 C 15.5 14.3 16.3 14.1 17 14.4 " +
        "L 19.6 15.6 C 20.6 16.1 21.1 16.8 20.8 17.8 L 20.4 19 C 20.1 20 19.2 20.7 18.1 20.6 " +
        "C 10.6 20 4 13.4 3.4 5.9 C 3.3 4.8 4 3.9 5 3.6 Z"

private val phoneHandset by lazy {
    plataGlyph("phone-handset", multicolour = true) {
        fill(PHONE_HANDSET, brush = SolidColor(PlataPalette.GlyphTint))
        stroke(
            PHONE_HANDSET,
            width = 2.2f,
            brush = Brush.linearGradient(
                0f to PlataPalette.GlyphGradientTop,
                1f to PlataPalette.GlyphGradientBottom,
                start = Offset(12f, 2.9f),
                end = Offset(12f, 20.7f),
            ),
        )
    }
}

/** Duotone handset receiver for "By phone number to Plata user" (0b2e699a); PhoneDuotone is the smartphone. */
val PlataIcons.PhoneHandset: ImageVector get() = phoneHandset

private val closeXBold by lazy {
    plataGlyph("close-x-bold") {
        stroke("M 7.6 7.6 L 16.4 16.4 M 16.4 7.6 L 7.6 16.4", width = 2.2f)
    }
}

/** Heavier dialog close cross, drawn in text/primary on ec4a6b30 (CloseX is the thin grey 6f8f3145 one). */
val PlataIcons.CloseXBold: ImageVector get() = closeXBold

/** Glyphs added in step 4, for the icon sheet. */
val PlataIcons.organismGlyphs: List<Pair<String, ImageVector>>
    get() = listOf("arrow-up-right-duotone" to ArrowUpRightDuotone, "calendar-grid" to CalendarGrid, "phone-handset" to PhoneHandset, "close-x-bold" to CloseXBold)
