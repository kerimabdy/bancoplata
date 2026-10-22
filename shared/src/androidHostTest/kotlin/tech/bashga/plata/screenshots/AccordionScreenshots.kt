package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.AccordionCollapsedPreview
import tech.bashga.plata.designsystem.components.AccordionExpandedPreview

class AccordionScreenshots : ScreenshotTest() {
    @Test fun collapsed() = component("accordion/state=collapsed", margin = 0.dp) {
        SpecimenFrame(361.dp, 72.dp, 4.dp, 3.7.dp) { Box(Modifier.width(353.dp)) { AccordionCollapsedPreview() } }
    }

    @Test fun expanded() = component("accordion/state=expanded", margin = 0.dp) {
        SpecimenFrame(361.dp, 420.dp, 4.dp, 4.dp) { Box(Modifier.width(353.dp)) { AccordionExpandedPreview() } }
    }
}
