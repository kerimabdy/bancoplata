package tech.bashga.plata.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

// The signature sheet's action glyphs (aee18063), traced from the source on the 24 grid with the buttons turned upright.
private val trash by lazy {
    plataGlyph("trash") {
        stroke("M3 6 H19.5", width = 1.9f)
        stroke("M7.5 6 C7.5 3.6 9.2 2.3 11.5 2.3 C13.8 2.3 15.5 3.6 15.5 6", width = 1.9f)
        stroke("M5.5 6 V18 C5.5 19.8 6.7 21 8.5 21 H14.5 C16.3 21 17.5 19.8 17.5 18 V6", width = 1.9f)
        stroke("M9.6 11 V16 M12.9 11 V16", width = 1.9f)
    }
}

private val undo by lazy {
    plataGlyph("undo") {
        stroke("M8.3 4.8 L3 9.5 L8.3 14.2 M3 9.5 H13.5 C16.6 9.5 18.5 11.4 18.5 14 C18.5 16.6 16.6 18.5 14 18.5 H10.3", width = 2f)
    }
}

/** Trash can (Clear on the signature sheet, aee18063). */
val PlataIcons.Trash: ImageVector get() = trash

/** Curved back arrow (Undo on the signature sheet, aee18063). */
val PlataIcons.Undo: ImageVector get() = undo
