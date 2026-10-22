package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.SectionCardActionPreview
import tech.bashga.plata.designsystem.components.SectionCardAmountPreview
import tech.bashga.plata.designsystem.components.SectionCardGradientPreview
import tech.bashga.plata.designsystem.components.SectionCardNonePreview

/** border=none frames 300 high, past the 287 specimen, so the card's bottom edge always shows. header-amount ← d2e81313 @ 16,236,361,202. header-action (ec4a6b30, under a dialog scrim in the source) renders only. */
class SectionCardScreenshots : ScreenshotTest() {
    @Test fun none() = component("section-card/border=none", margin = 0.dp) {
        SpecimenFrame(361.dp, 300.dp, 4.dp, 3.dp) { SectionCardNonePreview() }
    }

    @Test fun gradient() = component("section-card/border=gradient", margin = 0.dp) {
        SpecimenFrame(361.dp, 286.dp, 3.5.dp, 2.dp) { SectionCardGradientPreview() }
    }

    @Test fun amount() = component("section-card/header-amount", margin = 0.dp) {
        SpecimenFrame(361.dp, 202.dp, 4.dp, 4.dp) { SectionCardAmountPreview() }
    }

    @Test fun action() = component("section-card/header-action") { SectionCardActionPreview() }
}
