package tech.bashga.plata.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.PasscodeDotEmptyOnGradientPreview
import tech.bashga.plata.designsystem.components.PasscodeDotEmptyPreview
import tech.bashga.plata.designsystem.components.PasscodeDotErrorPreview
import tech.bashga.plata.designsystem.components.PasscodeDotFilledPreview
import tech.bashga.plata.designsystem.components.PasscodeDotSuccessPreview

class PasscodeDotScreenshots : ScreenshotTest() {
    @Test fun filled() = component("passcode-dot/state=filled,tone=default", background = { it.surfaceCard }, margin = 5.dp) { PasscodeDotFilledPreview() }

    @Test fun empty() = component("passcode-dot/state=empty,tone=default", background = { it.surfaceCard }, margin = 3.dp) { PasscodeDotEmptyPreview() }

    @Test fun success() = component("passcode-dot/state=success,tone=default", background = { it.surfaceCard }, margin = 5.dp) { PasscodeDotSuccessPreview() }

    // the orange lock-screen gradient behind the dots on b76b9dda
    @Test fun emptyOnGradient() = component("passcode-dot/state=empty,tone=onGradient", background = { Color(0xFFFFA57F) }, margin = 2.dp) {
        PasscodeDotEmptyOnGradientPreview()
    }

    @Test fun error() = component("passcode-dot/state=error,tone=default", background = { it.surfaceCard }, margin = 5.dp) { PasscodeDotErrorPreview() }
}
