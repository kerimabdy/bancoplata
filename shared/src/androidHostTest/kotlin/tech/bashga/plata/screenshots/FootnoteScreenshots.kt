package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.FootnotePlainPreview
import tech.bashga.plata.designsystem.components.FootnotePreview

class FootnoteScreenshots : ScreenshotTest() {
    @Test fun default() = component("footnote/default", background = { it.surfaceInset }, margin = 0.dp) {
        SpecimenFrame(282.dp, 22.dp, 4.dp, 2.dp) { FootnotePreview() }
    }

    @Test fun plain() = component("footnote/plain") { FootnotePlainPreview() }
}
