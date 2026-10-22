package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.TabBarItemActiveIconPreview
import tech.bashga.plata.designsystem.components.TabBarItemBadgePreview
import tech.bashga.plata.designsystem.components.TabBarItemIconValuePreview
import tech.bashga.plata.designsystem.components.TabBarItemInactiveIconPreview
import tech.bashga.plata.designsystem.components.TabBarItemValuePreview

class TabBarItemScreenshots : ScreenshotTest() {
    private fun item(name: String, content: @androidx.compose.runtime.Composable () -> Unit) = component(name, margin = 0.dp) {
        SpecimenFrame(79.dp, 46.dp, 0.dp, 0.dp) { Box(Modifier.width(78.6.dp)) { content() } }
    }

    @Test fun activeIcon() = item("tab-bar-item/state=active,kind=icon") { TabBarItemActiveIconPreview() }

    @Test fun inactiveIcon() = item("tab-bar-item/state=inactive,kind=icon") { TabBarItemInactiveIconPreview() }

    @Test fun inactiveValue() = item("tab-bar-item/state=inactive,kind=value") { TabBarItemValuePreview() }

    @Test fun inactiveIconValue() = item("tab-bar-item/state=inactive,kind=iconValue") { TabBarItemIconValuePreview() }

    @Test fun activeBadge() = component("tab-bar-item/state=active,kind=icon,badge", margin = 0.dp) {
        SpecimenFrame(79.dp, 58.dp, 0.dp, 12.dp) { Box(Modifier.width(78.6.dp)) { TabBarItemBadgePreview() } }
    }
}
