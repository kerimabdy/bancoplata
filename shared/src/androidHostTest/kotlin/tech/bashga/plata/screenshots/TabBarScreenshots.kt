package tech.bashga.plata.screenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.TabBarBadgePreview
import tech.bashga.plata.designsystem.components.TabBarHomePreview
import tech.bashga.plata.designsystem.components.TabBarInvestPreview
import tech.bashga.plata.designsystem.components.TabBarPaymentsPreview
import tech.bashga.plata.designsystem.components.TabBarSupportPreview
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea

/** Specimens are cropped from y 766; the bar's top edge is 769. badge ← 9f1aa9fc @ 0,766,393,86. */
class TabBarScreenshots : ScreenshotTest() {
    private fun bar(name: String, content: @Composable () -> Unit) = component(name, margin = 0.dp) {
        ProvideSafeArea(PlataSafeArea.IPhone) { SpecimenFrame(393.dp, 86.dp, 0.dp, 3.dp) { content() } }
    }

    @Test fun home() = bar("tab-bar/active=home") { TabBarHomePreview() }

    @Test fun payments() = bar("tab-bar/active=payments") { TabBarPaymentsPreview() }

    @Test fun invest() = bar("tab-bar/active=invest") { TabBarInvestPreview() }

    @Test fun support() = bar("tab-bar/active=support") { TabBarSupportPreview() }

    @Test fun badge() = bar("tab-bar/badge") { TabBarBadgePreview() }
}
