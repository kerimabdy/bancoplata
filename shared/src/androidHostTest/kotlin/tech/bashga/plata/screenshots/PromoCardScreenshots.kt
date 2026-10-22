package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.PromoCardBannerPreview
import tech.bashga.plata.designsystem.components.PromoCardHeroPreview

class PromoCardScreenshots : ScreenshotTest() {
    @Test fun hero() = component("promo-card/layout=hero", margin = 0.dp) {
        SpecimenFrame(361.dp, 349.dp, 4.dp, 3.33.dp) { PromoCardHeroPreview() }
    }

    @Test fun banner() = component("promo-card/layout=banner", margin = 0.dp) {
        SpecimenFrame(361.dp, 94.dp, 3.67.dp, 4.33.dp) { PromoCardBannerPreview() }
    }
}
