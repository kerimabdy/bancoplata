package tech.bashga.plata.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.NavigationBarCenterIconPreview
import tech.bashga.plata.designsystem.components.NavigationBarLargeCollapsedPreview
import tech.bashga.plata.designsystem.components.NavigationBarLargePreview
import tech.bashga.plata.designsystem.components.NavigationBarMaterialPreview
import tech.bashga.plata.designsystem.components.NavigationBarOverMediaPreview
import tech.bashga.plata.designsystem.components.NavigationBarProgressPreview
import tech.bashga.plata.designsystem.components.NavigationBarSheetDarkPreview
import tech.bashga.plata.designsystem.components.NavigationBarSheetPreview
import tech.bashga.plata.designsystem.components.NavigationBarSheetTwoLinePreview
import tech.bashga.plata.designsystem.components.NavigationBarStandardPreview
import tech.bashga.plata.designsystem.components.NavigationBarTwoLinePreview
import tech.bashga.plata.designsystem.foundation.ContinuousTopRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea

/**
 * Bars are 393 wide. Sheet specimens are cropped from y 70 with the sheet top at 69, so sheet bars sit 1pt above the
 * window inside a mock sheet (10pt top corners over the stacked-card grey). Extra renders compare with source crops:
 * material ← d58c1486 @ 0,0,393,98; two-line ← 537a4b5a @ 0,54,393,44; sheet-two-line ← 5f6bf512 @ 0,70,393,54;
 * center-icon ← d7d78e0b @ 0,54,393,44.
 */
class NavigationBarScreenshots : ScreenshotTest() {
    private fun bar(name: String, height: Int, content: @Composable () -> Unit) = component(name, margin = 0.dp) {
        SpecimenFrame(393.dp, height.dp, 0.dp, 0.dp) { Box(Modifier.width(393.dp)) { content() } }
    }

    private fun sheetBar(name: String, backdrop: Color, sheet: Color, content: @Composable () -> Unit) =
        component(name, margin = 0.dp) {
            SpecimenFrame(393.dp, 54.dp, 0.dp, (-2).dp) {
                Box(Modifier.width(393.dp).background(backdrop).padding(top = 1.dp)) {
                    Box(Modifier.clip(ContinuousTopRoundedShape(10.dp)).background(sheet)) { content() }
                }
            }
        }

    @Test fun standard() = bar("navigation-bar/style=standard", 44) { NavigationBarStandardPreview() }

    @Test fun sheet() = sheetBar("navigation-bar/style=sheet", Color(0xFFD7D9DC), Color(0xFFF5F7FA)) { NavigationBarSheetPreview() }

    @Test fun sheetDark() = sheetBar("navigation-bar/style=sheetDark", Color(0xFF000000), Color(0xFF191B1C)) { NavigationBarSheetDarkPreview() }

    @Test fun progress() = bar("navigation-bar/style=progress", 44) { NavigationBarProgressPreview() }

    @Test fun large() = bar("navigation-bar/style=large", 90) { NavigationBarLargePreview() }

    @Test fun overMedia() = sheetBar("navigation-bar/style=overMedia", Color(0xFFD8D8DC), Color(0xFF225890)) { NavigationBarOverMediaPreview() }

    @Test fun material() = component("navigation-bar/material", background = { Color(0xFFF5F7FA) }, margin = 0.dp) {
        ProvideSafeArea(PlataSafeArea.IPhone) { Box(Modifier.width(393.dp)) { NavigationBarMaterialPreview() } }
    }

    @Test fun twoLine() = bar("navigation-bar/two-line", 44) { NavigationBarTwoLinePreview() }

    @Test fun sheetTwoLine() = sheetBar("navigation-bar/sheet-two-line", Color(0xFFD5D5D8), Color(0xFFFDEBD6)) { NavigationBarSheetTwoLinePreview() }

    @Test fun centerIcon() = bar("navigation-bar/center-icon", 44) { NavigationBarCenterIconPreview() }

    // §4: a scrolled large-title bar goes inline with the material (compare 0b2e699a @ 0,0,393,98)
    @Test fun largeCollapsed() = bar("navigation-bar/style=large,collapsed", 98) {
        ProvideSafeArea(PlataSafeArea.IPhone) { NavigationBarLargeCollapsedPreview() }
    }
}
