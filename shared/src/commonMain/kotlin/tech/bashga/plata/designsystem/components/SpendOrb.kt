package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.foundation.plataShadow
import tech.bashga.plata.designsystem.theme.PlataTheme

enum class SpendOrbState { Active, Empty }

/** Active: blue from the left, a red lobe right of centre, a white glow along the bottom and a soft rim (025e71c5). */
@Composable
fun SpendOrb(state: SpendOrbState, modifier: Modifier = Modifier) {
    val colors = PlataTheme.colors
    val white = colors.surfaceCard
    val shade = colors.surfaceBackground
    val blue = colors.orbBlue
    val violet = colors.orbViolet
    val red = colors.orbRed
    Canvas(
        modifier
            .size(48.dp)
            .plataShadow(PlataTheme.shadows.orb, CircleShape)
            // offscreen so the rim mask only fades the orb's own pixels
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        val w = size.width
        val h = size.height
        if (state == SpendOrbState.Empty) {
            drawCircle(Brush.radialGradient(0.7f to white, 1f to shade, center = Offset(w * 0.55f, h * 0.55f)))
            return@Canvas
        }
        drawCircle(
            Brush.linearGradient(
                0f to blue,
                0.35f to blue,
                0.62f to violet,
                0.88f to red,
                start = Offset(0f, h * 0.3f),
                end = Offset(w, h * 0.65f),
            ),
        )
        drawCircle(
            Brush.radialGradient(
                0f to white,
                0.4f to white.copy(alpha = 0.8f),
                1f to Color.Transparent,
                center = Offset(w * 0.42f, h * 1.02f),
                radius = h * 0.56f,
            ),
        )
        val rim = 1.5.dp.toPx()
        val radius = w / 2
        drawCircle(
            Brush.radialGradient(
                0f to Color.Black,
                (radius - rim) / radius to Color.Black,
                1f to Color.Transparent,
                radius = radius,
            ),
            blendMode = BlendMode.DstIn,
        )
    }
}

@Preview
@Composable
internal fun SpendOrbActivePreview() = PlataTheme { SpendOrb(SpendOrbState.Active) }

@Preview
@Composable
internal fun SpendOrbEmptyPreview() = PlataTheme { SpendOrb(SpendOrbState.Empty) }
