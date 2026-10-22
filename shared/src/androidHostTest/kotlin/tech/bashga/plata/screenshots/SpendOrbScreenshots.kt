package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.SpendOrbActivePreview
import tech.bashga.plata.designsystem.components.SpendOrbEmptyPreview

class SpendOrbScreenshots : ScreenshotTest() {
    @Test fun active() = component("spend-orb/state=active", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(60.dp, 62.dp, 6.dp, 4.dp) { SpendOrbActivePreview() }
    }

    @Test fun empty() = component("spend-orb/state=empty", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(60.dp, 62.dp, 6.dp, 4.dp) { SpendOrbEmptyPreview() }
    }
}
