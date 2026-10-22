package tech.bashga.plata.designsystem.foundation

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.theme.PlataShadow

/** Draws a `PlataTheme.shadows` token behind the element, shaped like [shape]. Apply before `background`/`clip`. */
fun Modifier.plataShadow(shadow: PlataShadow, shape: Shape): Modifier = dropShadow(
    shape = shape,
    shadow = Shadow(radius = shadow.blur, color = shadow.color, offset = DpOffset(0.dp, shadow.offsetY)),
)
