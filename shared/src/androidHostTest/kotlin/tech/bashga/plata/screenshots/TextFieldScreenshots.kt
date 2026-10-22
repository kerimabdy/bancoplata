package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.TextField
import tech.bashga.plata.designsystem.components.TextFieldClearButton
import tech.bashga.plata.designsystem.components.TextFieldGreyEmptyPreview
import tech.bashga.plata.designsystem.components.TextFieldGreyFilledPreview
import tech.bashga.plata.designsystem.components.TextFieldSurface
import tech.bashga.plata.designsystem.components.TextFieldWhiteEmptyPreview
import tech.bashga.plata.designsystem.components.TextFieldWhiteFilledPreview

class TextFieldScreenshots : ScreenshotTest() {
    @Test fun whiteEmpty() = component("text-field/surface=white,state=empty") { TextFieldWhiteEmptyPreview() }

    @Test fun whiteFilled() = component("text-field/surface=white,state=filled") { TextFieldWhiteFilledPreview() }

    @Test fun greyEmpty() = component("text-field/surface=grey,state=empty", background = { it.surfaceCard }) { TextFieldGreyEmptyPreview() }

    @Test fun greyFilled() = component("text-field/surface=grey,state=filled", background = { it.surfaceCard }) { TextFieldGreyFilledPreview() }

    // §4: the real caret of a focused field, clock paused so the blink is on (compare 642128ff @ 16,360,361,64)
    @Test fun greyFocused() = component(
        "text-field/surface=grey,state=focused",
        background = { it.surfaceCard },
        clockMillis = 100,
        setup = { focus("field") },
    ) {
        TextField(
            "Depto 4B",
            {},
            Modifier.width(353.dp).testTag("field"),
            placeholder = "Details (optional)",
            surface = TextFieldSurface.Grey,
            trailing = { TextFieldClearButton({}) },
        )
    }
}
