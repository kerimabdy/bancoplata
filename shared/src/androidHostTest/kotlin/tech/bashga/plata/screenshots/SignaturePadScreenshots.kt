package tech.bashga.plata.screenshots

import androidx.compose.ui.unit.dp
import org.junit.Test
import tech.bashga.plata.designsystem.components.SignaturePadPreview

/** SignaturePad (§4, new) at the page-sheet size; compare with crop.sh aee18063 0 69 393 783. */
class SignaturePadScreenshots : ScreenshotTest() {
    @Test fun sample() = component("signature-pad/sample", background = { it.surfaceCard }, margin = 0.dp) { SignaturePadPreview() }
}
