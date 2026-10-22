package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.BulletItemDotPreview
import tech.bashga.plata.designsystem.components.BulletItemIconPreview
import tech.bashga.plata.designsystem.components.BulletItemNumberPreview

class BulletItemScreenshots : ScreenshotTest() {
    @Test fun number() = component("bullet-item/marker=number", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(320.7.dp, 34.dp, 6.dp, 7.dp) { BulletItemNumberPreview() }
    }

    @Test fun icon() = component("bullet-item/marker=icon", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(322.dp, 50.dp, 4.dp, 4.dp) { BulletItemIconPreview() }
    }

    @Test fun dot() = component("bullet-item/marker=dot", margin = 0.dp) {
        SpecimenFrame(250.dp, 28.dp, 4.dp, 4.dp) { BulletItemDotPreview() }
    }
}
