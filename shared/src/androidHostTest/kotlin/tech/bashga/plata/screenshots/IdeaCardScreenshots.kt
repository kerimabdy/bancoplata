package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.IdeaCardPreview

class IdeaCardScreenshots : ScreenshotTest() {
    @Test fun default() = component("idea-card/default", margin = 0.dp) {
        SpecimenFrame(361.dp, 240.dp, 4.dp, 4.3.dp) { Box(Modifier.width(353.dp)) { IdeaCardPreview() } }
    }
}
