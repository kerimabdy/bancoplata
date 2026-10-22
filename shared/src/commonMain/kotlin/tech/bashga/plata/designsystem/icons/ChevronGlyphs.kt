package tech.bashga.plata.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

// From the Chevron component (Figma 79:15): different frames and strokes from the 24pt chevron glyphs.
private val chevronDisclosure by lazy {
    plataGlyph("chevron-disclosure", width = 18f, height = 22f) {
        // ink 6×11.3 ending at x13 like the source rows (59741a55, e94a49a9, cc4f1c47, 894b1b01); Figma's 8×14 read heavier
        stroke("M7.95 6.3 L12.05 11 L7.95 15.7", width = 1.9f)
    }
}

private val chevronBack by lazy {
    plataGlyph("chevron-back", width = 24f, height = 26f) {
        stroke("M13.2 5.1 L4.4 13.5 L13.2 21.9", width = 2.2f)
    }
}

/** ListRow disclosure chevron: 6×11.3 ink in an 18×22 frame, right end at x13 (Chevron direction=right). */
val PlataIcons.ChevronDisclosure: ImageVector get() = chevronDisclosure

/** NavigationBar back chevron: 11×19 ink at x3.3–14.3, y4–23 of its 24×26 frame; the frame sits at screen (6, 62) on 11cfd731 and 72a00afe. */
val PlataIcons.ChevronBack: ImageVector get() = chevronBack
