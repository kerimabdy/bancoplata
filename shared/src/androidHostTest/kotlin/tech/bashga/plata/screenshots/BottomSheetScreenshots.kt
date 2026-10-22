package tech.bashga.plata.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.BottomSheetCenterPreview
import tech.bashga.plata.designsystem.components.BottomSheetLeadingPreview
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea

/** Backdrops are sampled: the 50% scrim over the page (0b2e699a) and the light map behind the sheet (78f0c567). */
class BottomSheetScreenshots : ScreenshotTest() {
    @Test fun leading() = component("bottom-sheet/align=leading", background = { Color(0xFF7F7F7F) }, margin = 0.dp) {
        ProvideSafeArea(PlataSafeArea.IPhone) {
            SpecimenFrame(393.dp, 338.dp, 0.dp, 4.dp) { BottomSheetLeadingPreview() }
        }
    }

    @Test fun center() = component("bottom-sheet/align=center", background = { Color(0xFFF1F1F1) }, margin = 0.dp) {
        ProvideSafeArea(PlataSafeArea.IPhone) {
            SpecimenFrame(393.dp, 328.dp, 0.dp, 5.67.dp) { BottomSheetCenterPreview() }
        }
    }
}
