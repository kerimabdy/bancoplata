package tech.bashga.plata.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.BrandLogo32Preview
import tech.bashga.plata.designsystem.components.BrandLogo40Preview
import tech.bashga.plata.designsystem.components.BrandLogo56Preview
import tech.bashga.plata.designsystem.components.BrandLogo72Preview
import tech.bashga.plata.designsystem.components.BrandLogo96CheckPreview

class BrandLogoScreenshots : ScreenshotTest() {
    @Test fun size28() = component("brand-logo/size=28", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(40.dp, 40.dp, 3.8.dp, 3.8.dp) { BrandLogo32Preview() }
    }

    @Test fun size40() = component("brand-logo/size=40") { BrandLogo40Preview() }

    @Test fun size56() = component("brand-logo/size=56", margin = 0.dp) {
        SpecimenFrame(64.dp, 64.dp, 5.8.dp, 5.3.dp) { BrandLogo56Preview() }
    }

    // over the Mercado Libre partner photo (b78c58b4)
    @Test fun size72() = component("brand-logo/size=72", background = { Color(0xFFF2C21C) }, margin = 0.dp) {
        SpecimenFrame(84.dp, 84.dp, 8.dp, 5.dp) { BrandLogo72Preview() }
    }

    @Test fun size96() = component("brand-logo/size=96", margin = 0.dp) {
        SpecimenFrame(112.dp, 108.dp, 3.7.dp, 4.dp) { BrandLogo96CheckPreview() }
    }
}
