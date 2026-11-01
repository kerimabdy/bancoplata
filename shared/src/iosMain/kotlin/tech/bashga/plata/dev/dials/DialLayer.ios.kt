package tech.bashga.plata.dev.dials

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
internal actual fun DialLayer(offset: IntOffset, content: @Composable () -> Unit) {
    Popup(offset = offset, properties = PopupProperties(focusable = false, clippingEnabled = false), content = content)
}
