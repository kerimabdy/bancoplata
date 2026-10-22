package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.AccountCardPreview

class AccountCardScreenshots : ScreenshotTest() {
    @Test fun default() = component("account-card/default", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 88.dp, 4.dp, 4.3.dp) { AccountCardPreview() }
    }
}
