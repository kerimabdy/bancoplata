package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.BalanceHeaderLargeCenterPreview
import tech.bashga.plata.designsystem.components.BalanceHeaderLargePreview
import tech.bashga.plata.designsystem.components.BalanceHeaderLargeTrendDownPreview
import tech.bashga.plata.designsystem.components.BalanceHeaderLargeTrendPreview
import tech.bashga.plata.designsystem.components.BalanceHeaderMediumPreview
import tech.bashga.plata.designsystem.components.BalanceHeaderStatusPreview

class BalanceHeaderScreenshots : ScreenshotTest() {
    @Test fun largeLeading() = component("balance-header/size=large,align=leading", margin = 0.dp) {
        SpecimenFrame(329.dp, 105.dp, 8.dp, 2.65.dp) { Box(Modifier.width(313.dp)) { BalanceHeaderLargePreview() } }
    }

    @Test fun largeCenter() = component("balance-header/size=large,align=center", margin = 0.dp) {
        SpecimenFrame(329.dp, 72.dp, (-12).dp, (-2.65).dp) { Box(Modifier.width(353.dp)) { BalanceHeaderLargeCenterPreview() } }
    }

    @Test fun mediumLeading() = component("balance-header/size=medium,align=leading", margin = 0.dp) {
        SpecimenFrame(329.dp, 60.dp, 8.dp, 3.7.dp) { Box(Modifier.width(313.dp)) { BalanceHeaderMediumPreview() } }
    }

    @Test fun statusLeading() = component("balance-header/size=status,align=leading", margin = 0.dp) {
        SpecimenFrame(329.dp, 58.dp, 8.dp, 2.7.dp) { Box(Modifier.width(313.dp)) { BalanceHeaderStatusPreview() } }
    }

    // trend-line variant from b658c9da @ 32,112,329,106 (compare with a source crop)
    @Test fun largeTrend() = component("balance-header/size=large,trend=up", margin = 0.dp) {
        SpecimenFrame(329.dp, 106.dp, 8.dp, 4.65.dp) { Box(Modifier.width(313.dp)) { BalanceHeaderLargeTrendPreview() } }
    }

    @Test fun largeTrendDown() = component("balance-header/size=large,trend=down") {
        Box(Modifier.width(313.dp)) { BalanceHeaderLargeTrendDownPreview() }
    }
}
