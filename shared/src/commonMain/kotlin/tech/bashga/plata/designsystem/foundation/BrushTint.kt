package tech.bashga.plata.designsystem.foundation

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Repaints everything the element draws with [brush], keeping its alpha: a gradient tint for monochrome glyphs
 * (`gradients.glyph`, `gradients.brand`, `gradients.notification`), which `ColorFilter.tint` can't do.
 * The brush spans the element's bounds, so a vertical gradient runs from the top to the bottom of the icon box.
 * Apply it to an untinted `Image` of the glyph.
 */
fun Modifier.brushTint(brush: Brush): Modifier = this
    // offscreen so SrcIn only keeps the glyph's own pixels, not whatever is behind it
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(brush, blendMode = BlendMode.SrcIn)
    }
