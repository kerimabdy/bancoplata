package tech.bashga.plata

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import tech.bashga.plata.designsystem.foundation.PlataSafeArea

/** True while a wide window shows the app inside the iPhone frame rather than filling the page; matches index.html's media query. */
internal val isDeviceFramed = mutableStateOf(false)

private const val IPhoneWidth = 402f

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val query = window.matchMedia("(min-width: 600px)")
    isDeviceFramed.value = query.matches
    query.addEventListener("change") { isDeviceFramed.value = query.matches }

    ComposeViewport(document.getElementById("screen")!!) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val framed = isDeviceFramed.value
            // the frame shrinks with the window, so lay the app out in iPhone points whatever its size on screen
            val density = if (framed) Density(constraints.maxWidth / IPhoneWidth, LocalDensity.current.fontScale) else LocalDensity.current
            CompositionLocalProvider(LocalDensity provides density) {
                DeviceStatusBar(visible = framed) {
                    App(safeArea = if (framed) PlataSafeArea.IPhone else null)
                }
            }
        }
    }
}
