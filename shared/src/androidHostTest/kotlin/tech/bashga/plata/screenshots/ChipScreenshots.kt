package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.ChipIconWhitePreview
import tech.bashga.plata.designsystem.components.ChipLogoGreyPreview
import tech.bashga.plata.designsystem.components.ChipLogoWhitePreview
import tech.bashga.plata.designsystem.components.ChipTextSelectedPreview
import tech.bashga.plata.designsystem.components.ChipTextWhitePreview

class ChipScreenshots : ScreenshotTest() {
    @Test fun iconWhite() = component("chip/style=icon,tone=white,state=default", margin = 0.dp) {
        SpecimenFrame(150.dp, 50.dp, 3.3.dp, 5.dp) { ChipIconWhitePreview() }
    }

    @Test fun logoWhite() = component("chip/style=logo,tone=white,state=default", margin = 0.dp) {
        SpecimenFrame(90.dp, 48.dp, 2.3.dp, 4.dp) { ChipLogoWhitePreview() }
    }

    @Test fun logoGrey() = component("chip/style=logo,tone=grey,state=default", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(92.dp, 50.dp, 2.dp, 5.3.dp) { ChipLogoGreyPreview() }
    }

    @Test fun textWhite() = component("chip/style=text,tone=white,state=default") { ChipTextWhitePreview() }

    @Test fun textSelected() = component("chip/style=text,tone=white,state=selected") { ChipTextSelectedPreview() }
}
