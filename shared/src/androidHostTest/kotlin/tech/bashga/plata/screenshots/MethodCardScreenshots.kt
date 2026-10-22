package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.MethodCardBankCollapsedPreview
import tech.bashga.plata.designsystem.components.MethodCardBankExpandedPreview
import tech.bashga.plata.designsystem.components.MethodCardDarkExpandedPreview
import tech.bashga.plata.designsystem.components.MethodCardDetectedPreview
import tech.bashga.plata.designsystem.components.MethodCardOrangeExpandedPreview

class MethodCardScreenshots : ScreenshotTest() {
    @Test fun darkExpanded() = component("method-card/tone=dark,state=expanded", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 164.dp, 4.dp, 4.3.dp) { MethodCardDarkExpandedPreview() }
    }

    @Test fun orangeExpanded() = component("method-card/tone=orange,state=expanded", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 164.dp, 4.dp, 4.3.dp) { MethodCardOrangeExpandedPreview() }
    }

    @Test fun bankExpanded() = component("method-card/tone=bank,state=expanded", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 164.dp, 4.dp, 4.3.dp) { MethodCardBankExpandedPreview() }
    }

    @Test fun bankCollapsed() = component("method-card/tone=bank,state=collapsed", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 88.dp, 4.dp, 4.3.dp) { MethodCardBankCollapsedPreview() }
    }

    // §4: bank detection recolours the card: none, Santander, Plata (43e51dd1, 62147891, 5e785f05)
    @Test fun detected() = component("method-card/bank=none,santander,plata") { MethodCardDetectedPreview() }
}
