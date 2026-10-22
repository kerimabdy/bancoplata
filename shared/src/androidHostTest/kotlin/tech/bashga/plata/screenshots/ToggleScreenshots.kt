package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.ToggleOffPreview
import tech.bashga.plata.designsystem.components.ToggleOnPreview

class ToggleScreenshots : ScreenshotTest() {
    @Test fun off() = component("toggle/state=off", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(60.dp, 40.dp, 4.5.dp, 4.5.dp) { ToggleOffPreview() }
    }

    @Test fun on() = component("toggle/state=on", background = { it.surfaceCard }, margin = 4.5.dp) { ToggleOnPreview() }
}
