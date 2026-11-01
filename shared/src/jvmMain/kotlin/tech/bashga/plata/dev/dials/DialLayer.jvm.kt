package tech.bashga.plata.dev.dials

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

@Composable
internal actual fun DialLayer(offset: IntOffset, content: @Composable () -> Unit) {
    Box(Modifier.offset { offset }) { content() }
}
