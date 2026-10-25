package tech.bashga.plata.designsystem.foundation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * [painter] cropped to fill, blurring progressively from [from] (0–1 of the height) to full [radius] at the bottom, so
 * white text set over the bottom reads like on the iOS media cards. The blur is drawn here so the source art stays sharp.
 */
@Composable
fun ProgressiveBlurImage(painter: Painter, modifier: Modifier = Modifier, from: Float = 0.5f, radius: Dp = 16.dp) {
    Box(modifier) {
        Image(painter, null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        Image(
            painter,
            null,
            Modifier
                .matchParentSize()
                // offscreen so the DstIn mask cuts only the blurred copy, not the sharp image under it
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        Brush.verticalGradient(0f to Color.Transparent, from to Color.Transparent, (1f + from) / 2f to Color.Black),
                        blendMode = BlendMode.DstIn,
                    )
                }
                .blur(radius, BlurredEdgeTreatment.Rectangle),
            contentScale = ContentScale.Crop,
        )
    }
}
