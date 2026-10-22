package tech.bashga.plata.screenshots

import org.junit.Test
import tech.bashga.plata.designsystem.components.CodeFieldEmptyPreview
import tech.bashga.plata.designsystem.components.CodeFieldPartialPreview

/** CodeField (§4, new): a0a0ef2b's four cells; compare with crop.sh a0a0ef2b 108 284 196 64. */
class CodeFieldScreenshots : ScreenshotTest() {
    @Test fun empty() = component("code-field/empty") { CodeFieldEmptyPreview() }

    @Test fun partial() = component("code-field/partial") { CodeFieldPartialPreview() }
}
