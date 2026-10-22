package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.OnboardingHeroPreview
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea

class OnboardingHeroScreenshots : ScreenshotTest() {
    // the specimen is dda69155 below the status bar (y 54–852); the hero is the full 393×852 screen
    @Test fun default() = component("onboarding-hero/default", margin = 0.dp) {
        ProvideSafeArea(PlataSafeArea.IPhone) {
            SpecimenFrame(393.dp, 798.dp, 0.dp, (-54).dp) { OnboardingHeroPreview() }
        }
    }
}
