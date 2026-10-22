package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.HintLabelPreview

class HintLabelScreenshots : ScreenshotTest() {
    @Test fun default() = component("hint-label/default", margin = 0.dp) {
        SpecimenFrame(218.dp, 28.dp, 4.dp, 4.dp) { HintLabelPreview() }
    }
}
