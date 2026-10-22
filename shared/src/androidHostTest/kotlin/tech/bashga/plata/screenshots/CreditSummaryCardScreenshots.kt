package tech.bashga.plata.screenshots

import org.junit.Test
import tech.bashga.plata.designsystem.components.CreditSummaryCardDefaultPreview

class CreditSummaryCardScreenshots : ScreenshotTest() {
    @Test fun default() = component("credit-summary-card/default") { CreditSummaryCardDefaultPreview() }
}
