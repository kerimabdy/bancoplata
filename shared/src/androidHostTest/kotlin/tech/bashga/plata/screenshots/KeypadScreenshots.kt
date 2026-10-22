package tech.bashga.plata.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.KeypadLightPreview
import tech.bashga.plata.designsystem.components.KeypadOnGradientPreview

class KeypadScreenshots : ScreenshotTest() {
    // the specimens crop 1bad6d9f / b76b9dda @ 36,506; the keypad's top-left key starts at (36.5, 500)
    @Test fun light() = component("keypad/style=light", background = { it.surfaceCard }, margin = 0.dp) {
        SpecimenFrame(321.dp, 288.dp, 0.5.dp, (-6).dp) { KeypadLightPreview() }
    }

    // the lock-screen gradient behind the keys, sampled at the crop corners of b76b9dda
    @Test fun onGradient() = component("keypad/style=onGradient", margin = 0.dp) {
        val backdrop = Brush.linearGradient(
            listOf(Color(0xFFFFA680), Color(0xFFC6988D), Color(0xFF919191)),
            start = Offset(963f, 0f),
            end = Offset(0f, 864f),
        )
        Box(Modifier.background(backdrop)) {
            SpecimenFrame(321.dp, 288.dp, 0.5.dp, (-6).dp) { KeypadOnGradientPreview() }
        }
    }
}
