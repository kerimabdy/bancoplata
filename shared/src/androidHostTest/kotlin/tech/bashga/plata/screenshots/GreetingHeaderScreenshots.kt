package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.GreetingHeaderCenteredPreview
import tech.bashga.plata.designsystem.components.GreetingHeaderInlinePreview
import tech.bashga.plata.designsystem.components.GreetingHeaderSparklePreview

class GreetingHeaderScreenshots : ScreenshotTest() {
    @Test fun inline() = component("greeting-header/layout=inline", margin = 0.dp) {
        SpecimenFrame(214.dp, 70.dp, 8.dp, 7.3.dp) { GreetingHeaderInlinePreview() }
    }

    @Test fun centered() = component("greeting-header/layout=centered", margin = 0.dp) {
        SpecimenFrame(361.dp, 200.dp, 0.dp, 9.dp) {
            Box(Modifier.width(361.dp), contentAlignment = Alignment.TopCenter) { GreetingHeaderCenteredPreview() }
        }
    }

    // §4: the sparkle ring of a new avatar; compare with crop.sh 9f1aa9fc 28 70 220 92
    @Test fun sparkle() = component("greeting-header/layout=inline,sparkle", margin = 0.dp) {
        SpecimenFrame(220.dp, 92.dp, 0.dp, 0.dp) { GreetingHeaderSparklePreview() }
    }
}
