package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.BadgeRegularPreview
import tech.bashga.plata.designsystem.components.BadgeSmallPreview
import tech.bashga.plata.designsystem.components.BadgeWidePreview

class BadgeScreenshots : ScreenshotTest() {
    @Test fun small() = component("badge/size=small", background = { it.surfaceTabbar }, margin = 3.5.dp) { BadgeSmallPreview() }

    @Test fun regular() = component("badge/size=regular", background = { it.surfaceCard }, margin = 5.dp) { BadgeRegularPreview() }

    @Test fun wide() = component("badge/wide", background = { it.surfaceCard }) { BadgeWidePreview() }
}
