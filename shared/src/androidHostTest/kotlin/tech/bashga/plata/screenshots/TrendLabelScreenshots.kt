package tech.bashga.plata.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.TrendLabelDownPreview
import tech.bashga.plata.designsystem.components.TrendLabelOnMediaPreview
import tech.bashga.plata.designsystem.components.TrendLabelUpPreview

class TrendLabelScreenshots : ScreenshotTest() {
    @Test fun up() = component("trend-label/direction=up,tone=default", margin = 0.dp) {
        SpecimenFrame(68.dp, 24.dp, 6.8.dp, 2.dp) { TrendLabelUpPreview() }
    }

    @Test fun down() = component("trend-label/direction=down,tone=default", margin = 0.dp) {
        SpecimenFrame(68.dp, 24.dp, 6.8.dp, 2.dp) { TrendLabelDownPreview() }
    }

    // over the blue hero photo of 09c22ffa
    @Test fun onMedia() = component("trend-label/direction=up,tone=onMedia", background = { Color(0xFF5B6BA0) }, margin = 0.dp) {
        SpecimenFrame(238.dp, 26.dp, 4.dp, 2.dp) { TrendLabelOnMediaPreview() }
    }
}
