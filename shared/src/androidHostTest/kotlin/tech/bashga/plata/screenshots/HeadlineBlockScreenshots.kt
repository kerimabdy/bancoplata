package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.HeadlineBlockLargePreview
import tech.bashga.plata.designsystem.components.HeadlineBlockLongCopyPreview
import tech.bashga.plata.designsystem.components.HeadlineBlockMediumCenterPreview
import tech.bashga.plata.designsystem.components.HeadlineBlockSmallCenterPreview

class HeadlineBlockScreenshots : ScreenshotTest() {
    @Test fun leadingLarge() = component("headline-block/align=leading,size=large", margin = 0.dp) {
        SpecimenFrame(365.dp, 103.dp, 8.dp, 3.65.dp) { Box(Modifier.width(353.dp)) { HeadlineBlockLargePreview() } }
    }

    @Test fun centerMedium() = component("headline-block/align=center,size=medium", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(361.dp, 135.dp, 4.dp, 4.4.dp) { Box(Modifier.width(353.dp)) { HeadlineBlockMediumCenterPreview() } }
    }

    @Test fun centerSmall() = component("headline-block/align=center,size=small", margin = 0.dp) {
        SpecimenFrame(361.dp, 90.dp, 4.dp, 0.dp) { Box(Modifier.width(353.dp)) { HeadlineBlockSmallCenterPreview() } }
    }

    // 7ab581a2 long copy in body-relaxed 17/24; compare against a crop of the source at 20,360
    @Test fun longCopy() = component("headline-block/long-copy", margin = 0.dp) {
        SpecimenFrame(361.dp, 300.dp, 0.dp, 1.dp) { Box(Modifier.width(353.dp)) { HeadlineBlockLongCopyPreview() } }
    }
}
