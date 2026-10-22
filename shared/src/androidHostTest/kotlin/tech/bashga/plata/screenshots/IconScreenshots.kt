package tech.bashga.plata.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.IconLargePreview
import tech.bashga.plata.designsystem.components.IconMediumPreview
import tech.bashga.plata.designsystem.components.IconSmallPreview
import tech.bashga.plata.designsystem.components.IconTintsPreview

class IconScreenshots : ScreenshotTest() {
    @Test fun size20() = component("icon/size=20", background = { it.surfaceCard }, margin = 3.dp) { IconSmallPreview() }

    @Test fun size24() = component("icon/size=24", background = { it.surfaceCard }, margin = 2.dp) { IconMediumPreview() }

    // the tab bar over blurred card content reads #DCDEE1 on 025e71c5
    @Test fun size28() = component("icon/size=28", background = { Color(0xFFDCDEE1) }, margin = 3.dp) { IconLargePreview() }

    @Test fun tints() = component("icon/tints", margin = 0.dp) { IconTintsPreview() }
}
