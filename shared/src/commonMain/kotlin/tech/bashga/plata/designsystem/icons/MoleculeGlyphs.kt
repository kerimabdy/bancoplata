package tech.bashga.plata.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

// Glyphs the step-3 molecules need that neither Figma nor step 2 drew, traced from the source on the 24pt grid.

private val infoFill by lazy {
    plataGlyph("info-fill") {
        fill(
            "M3 12 A9 9 0 1 0 21 12 A9 9 0 1 0 3 12 Z " +
                "M10.85 7.75 A1.15 1.15 0 1 0 13.15 7.75 A1.15 1.15 0 1 0 10.85 7.75 Z " +
                "M11.05 11.2 A0.95 0.95 0 0 1 12.95 11.2 V16.1 A0.95 0.95 0 0 1 11.05 16.1 Z",
            evenOdd = true,
        )
    }
}

/** iOS `info.circle.fill`: a disc with the i cut out, so a tint keeps the mark see-through (AmountField note a6fae664, bank MethodCard). */
val PlataIcons.InfoFill: ImageVector get() = infoFill

private val reorder by lazy {
    plataGlyph("reorder") {
        stroke("M1.85 7.2 H22.15 M1.85 12 H22.15 M1.85 16.8 H22.15", width = 1.4f)
    }
}

/** iOS reorder control in ListRow edit mode: three 21.7-long bars 4.8 apart (2be9ff97); Figma's DragHandle has two. */
val PlataIcons.Reorder: ImageVector get() = reorder

/** Every step-3 molecule glyph (this file, StatusGlyphs.kt, HeadlineGlyphs.kt), for the icon sheet. */
val PlataIcons.moleculeGlyphs: List<Pair<String, ImageVector>>
    get() = listOf("info-fill" to InfoFill, "reorder" to Reorder, "snowflake-solid" to SnowflakeSolid, "promo-chevron" to PromoChevron)
