package tech.bashga.plata.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import plata.shared.generated.resources.Res
import plata.shared.generated.resources.raster_splash_background
import tech.bashga.plata.designsystem.components.Logo
import tech.bashga.plata.designsystem.components.LogoBrand
import tech.bashga.plata.designsystem.foundation.PlataSafeArea

private const val LaunchMillis = 1_200L

// the lockups hold until the root replace (350 ms) has landed, then fade out over the new root
internal const val SplashLockupHoldMillis = 500L
private const val SplashLockupFadeMillis = 300

// e61c2bc5 and b76b9dda: the wordmark box at y 318.5, the Escudo lockup at 741
private val SplashLogoTop = 259.5.dp
private val SplashEscudoTop = 682.dp

/**
 * Splash → Lock or PhoneNumber: Splash [arm]s it before the root replace, and the next root [consume]s it once, so
 * only the first root after the launch draws the fading lockups (not PhoneNumber after a log out).
 */
class SplashHandoff {
    private var pending = false

    fun arm() {
        pending = true
    }

    fun consume(): Boolean = pending.also { pending = false }
}

/** The two launch lockups at their splash positions. */
@Composable
private fun SplashLogos(modifier: Modifier = Modifier) {
    val top = PlataSafeArea.current.top
    Box(modifier.fillMaxSize()) {
        Logo(LogoBrand.Plata, Modifier.align(Alignment.TopCenter).padding(top = top + SplashLogoTop))
        Logo(LogoBrand.Escudo, Modifier.align(Alignment.TopCenter).padding(top = top + SplashEscudoTop))
    }
}

/** The splash lockups over the root that replaced Splash, fading out once that root is in (b76b9dda). */
@Composable
internal fun SplashLockupsFadingOut() {
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        delay(SplashLockupHoldMillis)
        alpha.animateTo(0f, tween(SplashLockupFadeMillis, easing = LinearEasing))
    }
    if (alpha.value > 0f) SplashLogos(Modifier.graphicsLayer { this.alpha = alpha.value }.clearAndSetSemantics {})
}

/** splash: the launch screen; after the launch timer, [onLaunched] hands over to Lock or PhoneNumber. */
@Composable
fun SplashScreen(onLaunched: () -> Unit) {
    val launched by rememberUpdatedState(onLaunched)
    LaunchedEffect(Unit) {
        delay(LaunchMillis)
        launched()
    }
    SplashContent()
}

@Composable
fun SplashContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        Image(painterResource(Res.drawable.raster_splash_background), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        SplashLogos()
    }
}
