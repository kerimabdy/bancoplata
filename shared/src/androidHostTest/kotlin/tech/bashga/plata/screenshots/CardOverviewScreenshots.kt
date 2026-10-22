package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.CardOverviewActivePreview
import tech.bashga.plata.designsystem.components.CardOverviewDetailsPreview
import tech.bashga.plata.designsystem.components.CardOverviewDigitalLoadingPreview
import tech.bashga.plata.designsystem.components.CardOverviewDigitalRevealPreview
import tech.bashga.plata.designsystem.components.CardOverviewFrozenPreview

class CardOverviewScreenshots : ScreenshotTest() {
    // the crops are y 100–672 of 3b3eaed7 / 697dbf17; the 51-high header box starts at y 113 (the card at 211)
    @Test fun active() = component("card-overview/state=active", margin = 0.dp) {
        SpecimenFrame(393.dp, 572.dp, 0.dp, 13.dp) { CardOverviewActivePreview() }
    }

    @Test fun frozen() = component("card-overview/state=frozen", margin = 0.dp) {
        SpecimenFrame(393.dp, 572.dp, 0.dp, 13.dp) { CardOverviewFrozenPreview() }
    }

    // compare with crop.sh a634583b 0 100 393 572
    @Test fun details() = component("card-overview/details", margin = 0.dp) {
        SpecimenFrame(393.dp, 572.dp, 0.dp, 13.dp) { CardOverviewDetailsPreview() }
    }

    // §4 DigitalReveal body; compare with crop.sh 202bee50 / 7464b63b 0 110 393 600 (the sheet starts at 69)
    @Test fun digitalReveal() = component("card-overview/body=digitalReveal", margin = 0.dp) {
        SpecimenFrame(393.dp, 640.dp, 0.dp, 41.dp) { CardOverviewDigitalRevealPreview() }
    }

    @Test fun digitalLoading() = component("card-overview/body=digitalReveal,loading", margin = 0.dp) {
        SpecimenFrame(393.dp, 640.dp, 0.dp, 41.dp) { CardOverviewDigitalLoadingPreview() }
    }
}
