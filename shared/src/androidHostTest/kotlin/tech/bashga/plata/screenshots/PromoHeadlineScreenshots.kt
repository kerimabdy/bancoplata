package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.PromoHeadlineBrandPreview
import tech.bashga.plata.designsystem.components.PromoHeadlineGradientPreview
import tech.bashga.plata.designsystem.components.PromoHeadlinePlainPreview
import tech.bashga.plata.designsystem.components.PromoHeadlinePrimaryBodyPreview
import tech.bashga.plata.designsystem.components.PromoHeadlineSplitPreview

class PromoHeadlineScreenshots : ScreenshotTest() {
    @Test fun leadingSplit() = component("promo-headline/align=leading,tone=split", margin = 0.dp) {
        SpecimenFrame(338.dp, 78.dp, 7.7.dp, 1.8.dp) { PromoHeadlineSplitPreview() }
    }

    @Test fun centerPlain() = component("promo-headline/align=center,tone=plain", margin = 0.dp) {
        SpecimenFrame(273.dp, 70.dp, 0.dp, (-1.5).dp) { Box(Modifier.width(273.dp)) { PromoHeadlinePlainPreview() } }
    }

    @Test fun centerGradient() = component("promo-headline/align=center,tone=gradient", margin = 0.dp) {
        SpecimenFrame(313.dp, 116.dp, 0.dp, 1.65.dp) { Box(Modifier.width(313.dp)) { PromoHeadlineGradientPreview() } }
    }

    @Test fun centerBrand() = component("promo-headline/align=center,tone=brand", margin = 0.dp) {
        SpecimenFrame(313.dp, 193.dp, 0.dp, 0.65.dp) { Box(Modifier.width(313.dp)) { PromoHeadlineBrandPreview() } }
    }

    // 787363a9 @ 20,300,353,105: text/primary body 12 below the headline
    @Test fun centerPrimaryBody() = component("promo-headline/align=center,tone=plain,body=primary", margin = 0.dp) {
        SpecimenFrame(353.dp, 105.dp, 0.dp, 13.3.dp) { Box(Modifier.width(353.dp)) { PromoHeadlinePrimaryBodyPreview() } }
    }
}
