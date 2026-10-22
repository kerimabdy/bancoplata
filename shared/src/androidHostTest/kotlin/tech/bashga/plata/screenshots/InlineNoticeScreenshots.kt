package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.InlineNoticeInfoPreview
import tech.bashga.plata.designsystem.components.InlineNoticeWarningPreview

class InlineNoticeScreenshots : ScreenshotTest() {
    @Test fun warning() = component("inline-notice/tone=warning", margin = 0.dp) {
        SpecimenFrame(361.dp, 64.dp, 4.dp, 3.7.dp) { Box(Modifier.width(353.dp)) { InlineNoticeWarningPreview() } }
    }

    @Test fun info() = component("inline-notice/tone=info", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(321.dp, 74.dp, 4.dp, 4.7.dp) { Box(Modifier.width(313.dp)) { InlineNoticeInfoPreview() } }
    }
}
