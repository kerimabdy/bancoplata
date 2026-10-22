package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.ValueChangeAmountsPreview
import tech.bashga.plata.designsystem.components.ValueChangePreview

class ValueChangeScreenshots : ScreenshotTest() {
    @Test fun default() = component("value-change/default", margin = 0.dp) {
        SpecimenFrame(144.dp, 28.dp, 10.5.dp, 3.dp) { ValueChangePreview() }
    }

    @Test fun amounts() = component("value-change/amounts") { ValueChangeAmountsPreview() }
}
