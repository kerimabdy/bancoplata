package tech.bashga.plata.screenshots

import org.junit.Test
import tech.bashga.plata.designsystem.components.ToastHostPreview
import tech.bashga.plata.designsystem.components.ToastNeutralPreview
import tech.bashga.plata.designsystem.components.ToastSuccessPreview

class ToastScreenshots : ScreenshotTest() {
    @Test fun success() = component("toast/tone=success", background = { it.surfaceCard }) { ToastSuccessPreview() }

    @Test fun neutral() = component("toast/tone=neutral") { ToastNeutralPreview() }

    // §4: ToastHost pinned visible by the paused clock; must match success() above
    @Test fun host() = component("toast/host", background = { it.surfaceCard }, clockMillis = 0) { ToastHostPreview() }
}
