package tech.bashga.plata.designsystem.foundation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape

/** The app's shared transition scope; the nav host provides it, previews and screenshots run without one. */
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/** The visibility of the destination this content belongs to, which shared elements read as incoming or outgoing. */
val LocalSharedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

// a touch under critical, like UIKit's zoom transition: the card settles into place rather than stopping dead
private val SharedBoundsTransform = BoundsTransform { _, _ ->
    spring(dampingRatio = 0.86f, stiffness = 300f, visibilityThreshold = Rect.VisibilityThreshold)
}

/**
 * Pairs this element with the one under the same [key] on the destination being pushed or popped: its bounds morph
 * between the two along [transform], the incoming side's. [remeasure] lays the content out at each animated size
 * (containers, pages) instead of scaling it; [crossfade] false keeps both sides opaque, for a page whose top is drawn
 * as the card it grows from; the higher [zIndex] draws on top while they travel; [clip] shapes it. Pass a [state]
 * from `rememberSharedContentState` to read whether this element is paired right now. A null [key] or no scope is a
 * no-op.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedTransitionBounds(
    key: Any?,
    clip: Shape? = null,
    remeasure: Boolean = false,
    crossfade: Boolean = true,
    zIndex: Float = 0f,
    transform: BoundsTransform = SharedBoundsTransform,
    state: SharedTransitionScope.SharedContentState? = null,
): Modifier {
    val transitions = LocalSharedTransitionScope.current
    val visibility = LocalSharedVisibilityScope.current
    if (key == null || transitions == null || visibility == null) return this
    return with(transitions) {
        val state = state ?: rememberSharedContentState(key)
        val resize = if (remeasure) RemeasureToBounds else scaleToBounds()
        val enter = if (crossfade) fadeIn() else EnterTransition.None
        val exit = if (crossfade) fadeOut() else ExitTransition.None
        if (clip == null) {
            this@sharedTransitionBounds.sharedBounds(state, visibility, enter, exit, transform, resize, zIndexInOverlay = zIndex)
        } else {
            this@sharedTransitionBounds.sharedBounds(
                state,
                visibility,
                enter,
                exit,
                transform,
                resize,
                zIndexInOverlay = zIndex,
                clipInOverlayDuringTransition = OverlayClip(clip),
            )
        }
    }
}
