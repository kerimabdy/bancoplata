package tech.bashga.plata.designsystem.icons

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import tech.bashga.plata.designsystem.theme.PlataPalette

// same geometry as the Notification override in tools/figma_glyphs_to_kotlin.py
private const val OUTLINE = "M14 4.3 H6.1 A3 3 0 0 0 3.1 7.3 V17.9 A3 3 0 0 0 6.1 20.9 H16.4 A3 3 0 0 0 19.4 17.9 V10"
private const val DOT = "M15.35 5.3 A3.35 3.35 0 1 0 22.05 5.3 A3.35 3.35 0 1 0 15.35 5.3 Z"

private val notificationOutline by lazy {
    plataGlyph("notification-outline") { stroke(OUTLINE, width = 2.2f, cap = StrokeCap.Butt) }
}

private val notificationDot by lazy {
    plataGlyph("notification-dot", multicolour = true) { fill(DOT, brush = SolidColor(PlataPalette.Danger)) }
}

/**
 * Two-tone use of [PlataIcons.Notification]: draw this tinted, then [NotificationDot] untinted in the same box,
 * so the outline takes any colour while the dot stays `danger` red.
 */
val PlataIcons.NotificationOutline: ImageVector get() = notificationOutline

val PlataIcons.NotificationDot: ImageVector get() = notificationDot
