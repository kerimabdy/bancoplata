package tech.bashga.plata.screenshots

import org.junit.Test
import tech.bashga.plata.designsystem.components.SummaryCardActionsPreview
import tech.bashga.plata.designsystem.components.SummaryCardBodyPreview
import tech.bashga.plata.designsystem.components.SummaryCardChevronPreview
import tech.bashga.plata.designsystem.components.SummaryCardDefaultPreview
import tech.bashga.plata.designsystem.components.SummaryCardDetailsPreview
import tech.bashga.plata.designsystem.components.SummaryCardIconPreview

/** details ← 5e61179c @ 16,554.67,361,112; actions ← b658c9da @ 16,377.67,361,96. The rest render only. */
class SummaryCardScreenshots : ScreenshotTest() {
    @Test fun default() = component("summary-card/default") { SummaryCardDefaultPreview() }
    @Test fun details() = component("summary-card/details") { SummaryCardDetailsPreview() }
    @Test fun actions() = component("summary-card/actions") { SummaryCardActionsPreview() }
    @Test fun chevron() = component("summary-card/chevron") { SummaryCardChevronPreview() }
    @Test fun icon() = component("summary-card/icon") { SummaryCardIconPreview() }
    @Test fun body() = component("summary-card/body") { SummaryCardBodyPreview() }
}
