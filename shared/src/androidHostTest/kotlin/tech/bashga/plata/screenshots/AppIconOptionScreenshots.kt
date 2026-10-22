package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.AppIconOptionDefaultPreview
import tech.bashga.plata.designsystem.components.AppIconOptionSelectedPreview

class AppIconOptionScreenshots : ScreenshotTest() {
    @Test fun selected() = component("app-icon-option/state=selected", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(72.dp, 96.dp, 0.dp, 9.dp) { AppIconOptionSelectedPreview() }
    }

    @Test fun default() = component("app-icon-option/state=default", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(72.dp, 96.dp, 0.dp, 9.dp) { AppIconOptionDefaultPreview() }
    }
}
