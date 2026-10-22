package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.ChevronLeftPreview
import tech.bashga.plata.designsystem.components.ChevronRightPreview

class ChevronScreenshots : ScreenshotTest() {
    @Test fun right() = component("chevron/direction=right", background = { it.surfaceCard }, margin = 0.dp) { ChevronRightPreview() }

    @Test fun left() = component("chevron/direction=left", margin = 0.dp) {
        SpecimenFrame(22.dp, 26.dp, 0.dp, 0.dp) { ChevronLeftPreview() }
    }
}
