package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.CardThumbAddPreview
import tech.bashga.plata.designsystem.components.CardThumbDigitalPreview
import tech.bashga.plata.designsystem.components.CardThumbPhysicalPreview

class CardThumbScreenshots : ScreenshotTest() {
    @Test fun physical() = component("card-thumb/kind=physical", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(84.dp, 62.dp, 4.5.dp, 5.dp) { CardThumbPhysicalPreview() }
    }

    @Test fun digital() = component("card-thumb/kind=digital", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(84.dp, 62.dp, 4.5.dp, 5.dp) { CardThumbDigitalPreview() }
    }

    @Test fun add() = component("card-thumb/kind=add", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(84.dp, 62.dp, 4.5.dp, 5.dp) { CardThumbAddPreview() }
    }
}
