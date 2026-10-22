package tech.bashga.plata.screenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.CardArtCreditoPreview
import tech.bashga.plata.designsystem.components.CardArtDetailsPreview
import tech.bashga.plata.designsystem.components.CardArtDetailsSilverPreview
import tech.bashga.plata.designsystem.components.CardArtDigitalPreview
import tech.bashga.plata.designsystem.components.CardArtFrozenPreview
import tech.bashga.plata.designsystem.components.CardArtPhysicalFrozenDetailsPreview
import tech.bashga.plata.designsystem.components.CardArtPhysicalPreview

class CardArtScreenshots : ScreenshotTest() {
    // the crops start at (32, 203); the card's top-left is (40, 211) on every CardOverview screen
    private fun card(kind: String, preview: @Composable () -> Unit) = component("card-art/kind=$kind", margin = 0.dp) {
        SpecimenFrame(329.dp, 214.dp, 8.dp, 8.dp) { preview() }
    }

    @Test fun physical() = card("physical") { CardArtPhysicalPreview() }

    @Test fun digital() = card("digital") { CardArtDigitalPreview() }

    @Test fun frozen() = card("frozen") { CardArtFrozenPreview() }

    @Test fun details() = card("details") { CardArtDetailsPreview() }

    @Test fun detailsSilver() = card("detailsSilver") { CardArtDetailsSilverPreview() }

    // 5b9474e2 is a white page; the crop starts at (32, 185), the card at (40, 194.7)
    @Test fun credito() = component("card-art/kind=credito", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(329.dp, 216.dp, 8.dp, 9.7.dp) { CardArtCreditoPreview() }
    }

    // §4 variant: frozen is a flag, so a frozen card still flips to its details back (render only)
    @Test fun frozenDetails() = card("physical,face=details,frozen=true") { CardArtPhysicalFrozenDetailsPreview() }
}
