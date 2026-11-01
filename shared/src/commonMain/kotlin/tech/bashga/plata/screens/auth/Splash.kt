package tech.bashga.plata.screens.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import tech.bashga.plata.designsystem.components.Logo
import tech.bashga.plata.designsystem.components.LogoBrand
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.theme.PlataPalette

// e61c2bc5: the wordmark box at y 318.5, the Escudo lockup at 741
private val SplashLogoTop = 259.5.dp
private val SplashEscudoTop = 682.dp

// fitted to the brand splash render: orange top right easing into red, which sinks into black from the bottom left
private val SplashHue = (0..6).map { it / 6f }.map { t ->
    t to lerp(PlataPalette.SplashOrange, PlataPalette.SplashRed, splashEase(t, 0.898f, 0.518f))
}.toTypedArray()
private val SplashShade = (0..6).map { it / 6f }.map { t -> t to Color.Black.copy(alpha = splashEase(t, 0.828f, 0.770f)) }.toTypedArray()

private fun splashEase(t: Float, power: Float, smooth: Float): Float {
    val smoothstep = t * t * (3 - 2 * t)
    return (1 - smooth) * t.pow(power) + smooth * smoothstep.pow(power)
}

/** The launch gradient, drawn to the screen's size; the iOS launch image is a render of it (`iosApp/…/LaunchBackground`). */
@Composable
fun SplashBackground(modifier: Modifier = Modifier) {
    Spacer(
        modifier.fillMaxSize().drawWithCache {
            fun at(x: Float, y: Float) = Offset(x * size.width, y * size.height)
            val hue = Brush.linearGradient(*SplashHue, start = at(0.897f, 0.261f), end = at(0.276f, 0.448f))
            val shade = Brush.linearGradient(*SplashShade, start = at(0.670f, 0.580f), end = at(0.435f, 0.870f))
            onDrawBehind {
                drawRect(hue)
                drawRect(shade)
            }
        },
    )
}

/** What the native launch screens show: the gradient and both lockups. */
@Composable
fun SplashContent(modifier: Modifier = Modifier) {
    val top = PlataSafeArea.current.top
    Box(modifier.fillMaxSize()) {
        SplashBackground()
        Logo(LogoBrand.Plata, Modifier.align(Alignment.TopCenter).padding(top = top + SplashLogoTop))
        Logo(LogoBrand.Escudo, Modifier.align(Alignment.TopCenter).padding(top = top + SplashEscudoTop))
    }
}
