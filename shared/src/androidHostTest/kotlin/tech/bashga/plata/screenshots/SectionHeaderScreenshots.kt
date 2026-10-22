package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.SectionHeaderFullPreview
import tech.bashga.plata.designsystem.components.SectionHeaderGradientPreview
import tech.bashga.plata.designsystem.components.SectionHeaderGradientWordsPreview
import tech.bashga.plata.designsystem.components.SectionHeaderLeadingIconPreview
import tech.bashga.plata.designsystem.components.SectionHeaderSmallPreview
import tech.bashga.plata.designsystem.components.SectionHeaderTitlePreview

class SectionHeaderScreenshots : ScreenshotTest() {
    @Test fun titlePlain() = component("section-header/size=title,emphasis=plain", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(329.dp, 34.dp, 8.dp, 3.5.dp) { Box(Modifier.width(313.dp)) { SectionHeaderTitlePreview() } }
    }

    @Test fun smallPlain() = component("section-header/size=small,emphasis=plain", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(90.dp, 28.dp, 4.dp, 1.3.dp) { SectionHeaderSmallPreview() }
    }

    @Test fun titleGradient() = component("section-header/size=title,emphasis=gradient", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(198.dp, 32.dp, 8.dp, 2.2.dp) { SectionHeaderGradientPreview() }
    }

    @Test fun titleFull() = component("section-header/size=title,emphasis=full", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(204.dp, 32.dp, 3.5.dp, 3.7.dp) { SectionHeaderFullPreview() }
    }

    @Test fun gradientWords() = component("section-header/gradient-words", background = { it.surfaceCard }) { SectionHeaderGradientWordsPreview() }

    @Test fun leadingIcon() = component("section-header/leading-icon", background = { it.surfaceCard }) {
        Box(Modifier.width(313.dp)) { SectionHeaderLeadingIconPreview() }
    }
}
