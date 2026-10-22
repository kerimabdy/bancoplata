package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.MediaCardPreview

class MediaCardScreenshots : ScreenshotTest() {
    @Test fun dark() = component("media-card/tone=dark", background = { it.surfaceSheetDark }) {
        Box(Modifier.width(353.dp)) { MediaCardPreview() }
    }
}
