package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.CheckCircleCheckedPreview
import tech.bashga.plata.designsystem.components.CheckCircleDisabledPreview
import tech.bashga.plata.designsystem.components.CheckCircleUncheckedPreview

class CheckCircleScreenshots : ScreenshotTest() {
    @Test fun checked() = component("check-circle/state=checked", margin = 5.dp) { CheckCircleCheckedPreview() }

    @Test fun unchecked() = component("check-circle/state=unchecked", margin = 5.dp) { CheckCircleUncheckedPreview() }

    @Test fun disabled() = component("check-circle/state=disabled", margin = 5.dp) { CheckCircleDisabledPreview() }
}
