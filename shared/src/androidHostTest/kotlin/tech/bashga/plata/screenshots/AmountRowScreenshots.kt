package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.AmountRowCashbackPreview
import tech.bashga.plata.designsystem.components.AmountRowIncomingPreview
import tech.bashga.plata.designsystem.components.AmountRowNegativePreview
import tech.bashga.plata.designsystem.components.AmountRowNeutralPreview
import tech.bashga.plata.designsystem.components.AmountRowPositivePreview

// the specimens crop the middle 60 of the 80 rows
class AmountRowScreenshots : ScreenshotTest() {
    @Test fun positive() = component("amount-row/amountTone=default,secondaryTone=positive", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 60.dp, 4.dp, (-10).dp) { AmountRowPositivePreview() }
    }

    @Test fun negative() = component("amount-row/amountTone=default,secondaryTone=negative", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 60.dp, 4.dp, (-10).dp) { AmountRowNegativePreview() }
    }

    @Test fun neutral() = component("amount-row/amountTone=default,secondaryTone=neutral", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 60.dp, 4.dp, (-10.3).dp) { AmountRowNeutralPreview() }
    }

    @Test fun incoming() = component("amount-row/amountTone=positive,secondaryTone=neutral", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 60.dp, 4.dp, (-10).dp) { AmountRowIncomingPreview() }
    }

    @Test fun cashback() = component("amount-row/amountTone=default,secondaryTone=gradient", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 60.dp, 4.dp, (-10).dp) { AmountRowCashbackPreview() }
    }
}
