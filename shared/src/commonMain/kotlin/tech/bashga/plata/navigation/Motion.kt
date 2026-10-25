package tech.bashga.plata.navigation

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable

/** Which native back idiom the app follows: iOS edge-swipe slide, or Android predictive-back shrink. */
enum class BackStyle { Ios, Android }

expect val platformBackStyle: BackStyle

/** Critically damped like UIKit's push and sheet springs; a spring keeps velocity across interruptions and finger release. */
internal fun navSpring(visibilityThreshold: Float = ProgressThreshold): SpringSpec<Float> =
    spring(dampingRatio = 1f, stiffness = NavStiffness, visibilityThreshold = visibilityThreshold)

// the default 0.01 on a 0..1 progress is ~13px of screen width, so the spring visibly snaps at rest
private const val ProgressThreshold = 0.0005f

internal const val NavStiffness = 380f

/** The system's reduce-motion setting: pushes crossfade instead of sliding. */
@Composable
internal expect fun rememberReduceMotion(): Boolean
