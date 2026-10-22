package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.ProgressBarFullPreview
import tech.bashga.plata.designsystem.components.ProgressBarLabelsPreview
import tech.bashga.plata.designsystem.components.ProgressBarNavPreview

class ProgressBarScreenshots : ScreenshotTest() {
    @Test fun nav() = component("progress-bar/size=nav") { ProgressBarNavPreview() }

    @Test fun full() = component("progress-bar/size=full", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(321.dp, 14.dp, 4.dp, 3.dp) { ProgressBarFullPreview() }
    }

    @Test fun labels() = component("progress-bar/size=full,labels", background = { it.surfaceCard }) { ProgressBarLabelsPreview() }
}
