package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.SearchField
import tech.bashga.plata.designsystem.components.SearchFieldActivePreview
import tech.bashga.plata.designsystem.components.SearchFieldPlaceholderPreview

class SearchFieldScreenshots : ScreenshotTest() {
    @Test fun placeholder() = component("search-field/state=placeholder", margin = 0.dp) {
        SpecimenFrame(368.7.dp, 44.dp, 4.dp, 4.7.dp) { SearchFieldPlaceholderPreview() }
    }

    @Test fun active() = component("search-field/state=active", margin = 0.dp) {
        SpecimenFrame(370.dp, 44.dp, 4.dp, 5.dp) { SearchFieldActivePreview() }
    }

    // §4: focused with the real accent caret (compare 7e1c7742)
    @Test fun focused() = component("search-field/state=focused", margin = 0.dp, clockMillis = 100, setup = { focus("search") }) {
        SpecimenFrame(370.dp, 44.dp, 4.dp, 5.dp) { SearchField("krisp", {}, Modifier.width(361.dp).testTag("search"), active = true) }
    }
}
