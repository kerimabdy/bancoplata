package tech.bashga.plata.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.PillGlassPreview
import tech.bashga.plata.designsystem.components.PillWhiteIconPreview
import tech.bashga.plata.designsystem.components.PillWhitePreview

class PillScreenshots : ScreenshotTest() {
    // over the blue hero photo of 09c22ffa
    @Test fun glass() = component("pill/tone=glass", background = { Color(0xFF2F618E) }, margin = 0.dp) {
        SpecimenFrame(80.dp, 38.dp, 1.25.dp, 4.4.dp) { PillGlassPreview() }
    }

    // over the dark red hero photo of b68fc03a
    @Test fun white() = component("pill/tone=white", background = { Color(0xFF5A1C1F) }, margin = 0.dp) {
        SpecimenFrame(54.dp, 36.dp, 4.2.dp, 5.4.dp) { PillWhitePreview() }
    }

    @Test fun whiteIcon() = component("pill/tone=white,icon", background = { it.surfaceBackground }) { PillWhiteIconPreview() }
}
