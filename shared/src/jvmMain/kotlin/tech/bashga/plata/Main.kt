package tech.bashga.plata

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import tech.bashga.plata.designsystem.foundation.PlataSafeArea

/** The app in an iPhone-sized window, for Compose Hot Reload: `./gradlew :shared:hotRunJvm`. */
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Plata", state = rememberWindowState(size = DpSize(393.dp, 852.dp))) {
        App(safeArea = PlataSafeArea.IPhone)
    }
}
