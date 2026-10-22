package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.AmountField
import tech.bashga.plata.designsystem.components.AmountFieldEmptyPreview
import tech.bashga.plata.designsystem.components.AmountFieldFilledPreview
import tech.bashga.plata.designsystem.components.AmountFieldSingleRowPreview

class AmountFieldScreenshots : ScreenshotTest() {
    @Test fun filled() = component("amount-field/state=filled", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 84.dp, 4.dp, 4.3.dp) { AmountFieldFilledPreview() }
    }

    @Test fun empty() = component("amount-field/state=empty", margin = 0.dp) {
        SpecimenFrame(360.7.dp, 84.dp, 4.dp, 4.3.dp) { AmountFieldEmptyPreview() }
    }

    // single-row layout, compared with a6fae664 @ 16,397,361,64
    @Test fun singleRow() = component("amount-field/layout=singleRow", margin = 0.dp) {
        SpecimenFrame(361.dp, 64.dp, 4.dp, 4.3.dp) { AmountFieldSingleRowPreview() }
    }

    // §4: focused with the real caret (compare 40d2b403)
    @Test fun focused() = component("amount-field/state=focused", margin = 0.dp, clockMillis = 100, setup = { focus("amount") }) {
        SpecimenFrame(360.7.dp, 84.dp, 4.dp, 4.3.dp) {
            AmountField("1", {}, Modifier.width(353.dp).testTag("amount"), conversion = "$18.76", note = "No commission")
        }
    }
}
