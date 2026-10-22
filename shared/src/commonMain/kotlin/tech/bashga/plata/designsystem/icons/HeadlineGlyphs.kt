package tech.bashga.plata.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

// Step 3: the PromoHeadline chevron after "CASHBACK" (025e71c5) is 12.7×21.5 ink at a 3.5 stroke, far heavier than the disclosure chevron.
private val promoChevron by lazy {
    plataGlyph("promo-chevron", width = 13f, height = 24f) {
        stroke("M1.8 3 L11 12 L1.8 21", width = 3.5f)
    }
}

/** PromoHeadline align=leading chevron; draw at 24 (13×24 frame, ink from its left edge). */
val PlataIcons.PromoChevron: ImageVector get() = promoChevron
