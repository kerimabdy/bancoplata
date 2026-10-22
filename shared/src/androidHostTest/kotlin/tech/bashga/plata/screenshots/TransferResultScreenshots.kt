package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.TransferResultBetweenAccountsPreview
import tech.bashga.plata.designsystem.components.TransferResultPreview

class TransferResultScreenshots : ScreenshotTest() {
    // the crop is y 160–640 of 372e6fd9; the logo starts at y 169.7
    @Test fun default() = component("transfer-result/default", margin = 0.dp) {
        SpecimenFrame(393.dp, 480.dp, 0.dp, 9.7.dp) { TransferResultPreview() }
    }

    // compare with crop.sh 705c4f5e 0 200 393 400; the logo starts at y 212.3
    @Test fun betweenAccounts() = component("transfer-result/between-accounts", margin = 0.dp) {
        SpecimenFrame(393.dp, 400.dp, 0.dp, 12.3.dp) { TransferResultBetweenAccountsPreview() }
    }
}
