package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.SegmentedControlPlainPreview
import tech.bashga.plata.designsystem.components.SegmentedControlTrackPreview

class SegmentedControlScreenshots : ScreenshotTest() {
    @Test fun track() = component("segmented-control/style=track", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(320.7.dp, 52.dp, 4.dp, 4.3.dp) { SegmentedControlTrackPreview() }
    }

    @Test fun plain() = component("segmented-control/style=plain", margin = 0.dp) {
        SpecimenFrame(310.7.dp, 48.dp, 4.dp, 4.5.dp) { SegmentedControlPlainPreview() }
    }
}
