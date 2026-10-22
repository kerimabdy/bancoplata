package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.PartnerCardMinimumPreview
import tech.bashga.plata.designsystem.components.PartnerCardPercentPreview

class PartnerCardScreenshots : ScreenshotTest() {
    @Test fun percent() = component("partner-card/trailing=percent", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 88.dp, 4.dp, 4.7.dp) { PartnerCardPercentPreview() }
    }

    @Test fun minimum() = component("partner-card/trailing=minimum", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 88.dp, 4.dp, 3.3.dp) { PartnerCardMinimumPreview() }
    }
}
