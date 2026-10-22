package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.GridItemPreview

class GridItemScreenshots : ScreenshotTest() {
    @Test fun default() = component("grid-item/default", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(80.dp, 102.dp, 4.dp, 8.3.dp) { GridItemPreview() }
    }
}
