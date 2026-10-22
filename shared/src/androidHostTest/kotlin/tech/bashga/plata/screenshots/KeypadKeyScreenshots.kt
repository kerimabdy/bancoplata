package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.KeypadKeyDigitPreview
import tech.bashga.plata.designsystem.components.KeypadKeyIconPreview

class KeypadKeyScreenshots : ScreenshotTest() {
    // the specimens are 56pt crops around the glyph; the key hit area starts 24 left and 10 above them
    @Test fun digit() = component("keypad-key/content=digit", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(56.dp, 56.dp, -24.dp, -10.dp) { KeypadKeyDigitPreview() }
    }

    @Test fun icon() = component("keypad-key/content=icon", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(56.dp, 56.dp, -24.dp, -10.dp) { KeypadKeyIconPreview() }
    }
}
