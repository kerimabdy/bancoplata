package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.ListGroupDefaultPreview

class ListGroupScreenshots : ScreenshotTest() {
    @Test fun default() = component("list-group/default") { ListGroupDefaultPreview() }
}
