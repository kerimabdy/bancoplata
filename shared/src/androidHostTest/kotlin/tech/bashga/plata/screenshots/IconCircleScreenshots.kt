package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.IconCircle40FilledPreview
import tech.bashga.plata.designsystem.components.IconCircle40NeutralPreview
import tech.bashga.plata.designsystem.components.IconCircle56NeutralPreview
import tech.bashga.plata.designsystem.components.IconCircle80NeutralPreview
import tech.bashga.plata.designsystem.components.IconCircle80SuccessPreview
import tech.bashga.plata.designsystem.components.IconCircleTonesPreview

class IconCircleScreenshots : ScreenshotTest() {
    @Test fun neutral40() = component("icon-circle/size=40,tone=neutral", background = { it.surfaceCard }) { IconCircle40NeutralPreview() }

    @Test fun neutral56() = component("icon-circle/size=56,tone=neutral", background = { it.surfaceCard }) { IconCircle56NeutralPreview() }

    @Test fun neutral80() = component("icon-circle/size=80,tone=neutral", background = { it.surfaceCard }) { IconCircle80NeutralPreview() }

    @Test fun success80() = component("icon-circle/size=80,tone=success", background = { it.surfaceCard }) { IconCircle80SuccessPreview() }

    @Test fun filled40() = component("icon-circle/size=40,tone=filled") { IconCircle40FilledPreview() }

    @Test fun tones() = component("icon-circle/tones", background = { it.surfaceCard }, margin = 8.dp) { IconCircleTonesPreview() }
}
