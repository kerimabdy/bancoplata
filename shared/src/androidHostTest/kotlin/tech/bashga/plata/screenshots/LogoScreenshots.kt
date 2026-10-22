package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.LogoEscudoPreview
import tech.bashga.plata.designsystem.components.LogoPlataPreview

class LogoScreenshots : ScreenshotTest() {
    @Test fun plata() = component("logo/brand=plata", background = { it.brandPrimary }, margin = 0.dp) {
        SpecimenFrame(215.dp, 68.dp, 4.dp, 6.dp) { LogoPlataPreview() }
    }

    @Test fun escudo() = component("logo/brand=escudo", background = { it.surfaceBlack }, margin = 0.dp) {
        SpecimenFrame(120.dp, 48.dp, 4.dp, 4.dp) { LogoEscudoPreview() }
    }
}
