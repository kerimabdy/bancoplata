package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.InsetRowLinkPreview
import tech.bashga.plata.designsystem.components.InsetRowValuePreview

class InsetRowScreenshots : ScreenshotTest() {
    @Test fun link() = component("inset-row/trailing=link", background = { it.surfaceCard }) {
        Box(Modifier.width(313.dp)) { InsetRowLinkPreview() }
    }

    @Test fun value() = component("inset-row/trailing=value", background = { it.surfaceCard }) {
        Box(Modifier.width(297.dp)) { InsetRowValuePreview() }
    }
}
