package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.DialSlider70Preview
import tech.bashga.plata.designsystem.components.DialSliderPreview

class DialSliderScreenshots : ScreenshotTest() {
    // the crop is 63,322 268×269 of b3c28fcb; the ring's box starts at (67.35, 326.65)
    @Test fun default() = component("dial-slider/default", margin = 0.dp) {
        SpecimenFrame(268.dp, 269.dp, 4.35.dp, 4.65.dp) { DialSliderPreview() }
    }

    // compare with crop.sh f4915e4c 63 322 268 269
    @Test fun seventy() = component("dial-slider/value=70", margin = 0.dp) {
        SpecimenFrame(268.dp, 269.dp, 4.35.dp, 4.65.dp) { DialSlider70Preview() }
    }
}
