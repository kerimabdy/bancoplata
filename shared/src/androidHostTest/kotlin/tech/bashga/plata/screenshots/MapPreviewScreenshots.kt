package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.MapPreviewPreview

class MapPreviewScreenshots : ScreenshotTest() {
    @Test fun default() = component("map-preview/default", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(322.dp, 108.dp, 4.dp, 4.dp) { MapPreviewPreview() }
    }
}
