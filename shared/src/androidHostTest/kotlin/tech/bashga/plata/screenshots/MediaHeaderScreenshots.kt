package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.MediaHeaderCarouselPreview
import tech.bashga.plata.designsystem.components.MediaHeaderPagerPreview
import tech.bashga.plata.designsystem.components.MediaHeaderTitlePreview
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea

class MediaHeaderScreenshots : ScreenshotTest() {
    // the crop starts at the page-sheet top (y 69 on 816019bf); inside a sheet the safe-area top is 0
    @Test fun carousel() = component("media-header/content=carousel", margin = 0.dp) {
        SpecimenFrame(393.dp, 424.dp, 0.dp, 0.dp) { MediaHeaderCarouselPreview() }
    }

    // cc4f1c47 under the status bar; the crop starts at y 54
    @Test fun title() = component("media-header/content=title", margin = 0.dp) {
        ProvideSafeArea(PlataSafeArea.IPhone) {
            SpecimenFrame(393.dp, 380.dp, 0.dp, (-54).dp) { MediaHeaderTitlePreview() }
        }
    }

    // §4: the carousel as a real pager on page 0; must match carousel() above
    @Test fun pager() = component("media-header/content=pager", margin = 0.dp) {
        SpecimenFrame(393.dp, 424.dp, 0.dp, 0.dp) { MediaHeaderPagerPreview() }
    }
}
