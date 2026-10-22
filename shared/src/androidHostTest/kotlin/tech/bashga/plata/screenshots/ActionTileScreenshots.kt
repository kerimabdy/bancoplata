package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.ActionTileHalfPreview
import tech.bashga.plata.designsystem.components.ActionTileThirdPreview

class ActionTileScreenshots : ScreenshotTest() {
    @Test fun half() = component("action-tile/layout=half", margin = 0.dp) {
        SpecimenFrame(174.dp, 94.dp, 4.dp, 5.dp) { ActionTileHalfPreview() }
    }

    @Test fun third() = component("action-tile/layout=third", margin = 0.dp) {
        SpecimenFrame(112.dp, 94.dp, 4.dp, 4.7.dp) { ActionTileThirdPreview() }
    }
}
