package tech.bashga.plata.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.IconButtonGlass32Preview
import tech.bashga.plata.designsystem.components.IconButtonGrey20Preview
import tech.bashga.plata.designsystem.components.IconButtonGrey32Preview
import tech.bashga.plata.designsystem.components.IconButtonSubtle40Preview
import tech.bashga.plata.designsystem.components.IconButtonTinted48Preview
import tech.bashga.plata.designsystem.components.IconButtonWhite40Preview

class IconButtonScreenshots : ScreenshotTest() {
    // over the dark red hero photo of 816019bf
    @Test fun glass32() = component("icon-button/style=glass,size=32", background = { Color(0xFF5A0E10) }) { IconButtonGlass32Preview() }

    @Test fun grey32() = component("icon-button/style=grey,size=32", background = { it.surfaceCard }) { IconButtonGrey32Preview() }

    @Test fun grey20() = component("icon-button/style=grey,size=20", margin = 3.dp) { IconButtonGrey20Preview() }

    @Test fun subtle40() = component("icon-button/style=subtle,size=40", background = { it.surfaceCard }) { IconButtonSubtle40Preview() }

    @Test fun white40() = component("icon-button/style=white,size=40") { IconButtonWhite40Preview() }

    @Test fun tinted44() = component("icon-button/style=tinted,size=44", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(54.dp, 54.dp, 4.3.dp, 3.dp) { IconButtonTinted48Preview() }
    }
}
