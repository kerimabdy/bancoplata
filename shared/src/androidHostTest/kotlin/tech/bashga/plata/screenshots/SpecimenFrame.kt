package tech.bashga.plata.screenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Frames a component the way its specimen was cropped: a [width]×[height] window with the component's
 * top-left at ([x], [y]) inside it. Use with `component(name, margin = 0.dp)` when the specimen crop is not
 * the component's bounds plus a uniform margin (hit areas larger than the glyph, badges overhanging, shadows).
 */
@Composable
fun SpecimenFrame(width: Dp, height: Dp, x: Dp, y: Dp, content: @Composable () -> Unit) {
    Box(Modifier.size(width, height).clipToBounds()) {
        Box(Modifier.offset(x, y).wrapContentSize(Alignment.TopStart, unbounded = true)) { content() }
    }
}

