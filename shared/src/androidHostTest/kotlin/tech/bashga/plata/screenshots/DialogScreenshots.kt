package tech.bashga.plata.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.DialogBleedPreview
import tech.bashga.plata.designsystem.components.DialogInsetPreview
import tech.bashga.plata.designsystem.components.DialogNoActionsPreview
import tech.bashga.plata.designsystem.components.DialogSecondaryActionPreview

/**
 * Backdrop is the sampled scrim (#7A7A7C). no-actions ← b95e6964 @ 4,410,385,415;
 * secondary-action ← ec4a6b30 @ 4,361,385,490.
 */
class DialogScreenshots : ScreenshotTest() {
    private val scrim = Color(0xFF7A7A7C)

    @Test fun inset() = component("dialog/media=inset", background = { scrim }, margin = 0.dp) {
        SpecimenFrame(385.dp, 448.dp, 4.dp, 4.67.dp) { DialogInsetPreview() }
    }

    @Test fun bleed() = component("dialog/media=bleed", background = { scrim }, margin = 0.dp) {
        SpecimenFrame(385.dp, 679.dp, 4.dp, 5.33.dp) { DialogBleedPreview() }
    }

    @Test fun noActions() = component("dialog/no-actions", background = { scrim }, margin = 0.dp) {
        SpecimenFrame(385.dp, 415.dp, 4.dp, 4.33.dp) { DialogNoActionsPreview() }
    }

    @Test fun secondaryAction() = component("dialog/secondary-action", background = { scrim }, margin = 0.dp) {
        SpecimenFrame(385.dp, 490.dp, 4.dp, 4.dp) { DialogSecondaryActionPreview() }
    }
}
