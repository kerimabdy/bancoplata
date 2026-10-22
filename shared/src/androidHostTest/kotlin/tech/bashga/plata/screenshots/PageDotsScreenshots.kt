package tech.bashga.plata.screenshots

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.PageDots
import tech.bashga.plata.designsystem.components.PageDotsOverflowPreview
import tech.bashga.plata.designsystem.components.PageDotsPreview

class PageDotsScreenshots : ScreenshotTest() {
    // over the red hero photo of 816019bf
    @Test fun default() = component("page-dots/default", background = { Color(0xFF6A1112) }, margin = 0.dp) {
        SpecimenFrame(164.dp, 20.dp, 6.dp, 6.dp) { PageDotsPreview() }
    }

    @Test fun overflow() = component("page-dots/overflow", background = { Color(0xFF6A1112) }) { PageDotsOverflowPreview() }

    // §4: driven by a PagerState on page 1 (8417fbd4)
    @Test fun pagerState() = component("page-dots/pager-page=1", background = { Color(0xFF6A1112) }, margin = 0.dp) {
        SpecimenFrame(164.dp, 20.dp, 6.dp, 6.dp) { PageDots(rememberPagerState(initialPage = 1) { 9 }) }
    }
}
