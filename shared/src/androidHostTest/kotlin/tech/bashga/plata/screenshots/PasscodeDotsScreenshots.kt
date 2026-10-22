package tech.bashga.plata.screenshots

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.PasscodeDotsEntryPreview
import tech.bashga.plata.designsystem.components.PasscodeDotsErrorPreview
import tech.bashga.plata.designsystem.components.PasscodeDotsOnGradientPreview
import tech.bashga.plata.designsystem.components.PasscodeDotsSuccessPreview

class PasscodeDotsScreenshots : ScreenshotTest() {
    // six dots start at (98.5, 288.5) on 1bad6d9f and c126aa45; the crops start at (90, 282)
    @Test fun entry() = component("passcode-dots/state=entry,tone=default", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(214.dp, 30.dp, 8.5.dp, 6.5.dp) { PasscodeDotsEntryPreview() }
    }

    @Test fun success() = component("passcode-dots/state=success,tone=default", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(214.dp, 30.dp, 8.5.dp, 6.5.dp) { PasscodeDotsSuccessPreview() }
    }

    // four dots start at (134.7, 298.7) on b76b9dda; the crop starts at (132, 296); backdrop sampled from the crop
    @Test fun onGradient() = component("passcode-dots/state=entry,tone=onGradient", background = { Color(0xFFFFA37E) }, margin = 0.dp) {
        SpecimenFrame(130.dp, 22.dp, 2.7.dp, 2.7.dp) { PasscodeDotsOnGradientPreview() }
    }

    // §4 Error state, at rest after the shake (render only, no reference)
    @Test fun error() = component("passcode-dots/state=error,tone=default", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(214.dp, 30.dp, 8.5.dp, 6.5.dp) { PasscodeDotsErrorPreview() }
    }
}
