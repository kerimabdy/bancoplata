package tech.bashga.plata.navigation

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VectorizedFiniteAnimationSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tech.bashga.plata.designsystem.foundation.CardMotion
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.GrowCurve
import tech.bashga.plata.designsystem.foundation.LocalSharedTransitionScope
import tech.bashga.plata.designsystem.foundation.LocalSharedVisibilityScope
import tech.bashga.plata.designsystem.foundation.sharedTransitionBounds
import tech.bashga.plata.designsystem.theme.PlataTheme

/** How far a drag that would close an open expanding page has gone (0 at rest, 1 where it lets go). The nav host provides it. */
internal val LocalExpandDismiss = compositionLocalOf<ExpandDismiss?> { null }

internal val DismissReach: Dp get() = CardMotion.dismissReach.dp

internal val ProjectionSeconds: Float get() = CardMotion.projection

/**
 * The drag closing an open expanding page: the back edge pan ([edge], which the nav host reports) or a pull down
 * from the top of its content ([pull]). Once it lets go into the card, [progress] eases off from where the finger left
 * it while the page flies home, carrying the finger's speed.
 */
@Stable
internal class ExpandDismiss {
    var edge: () -> Float = { 0f }
    var pull by mutableFloatStateOf(0f)
        private set
    var flying by mutableStateOf(false)
        private set
    private val flight = Animatable(0f)
    private var pullRelease: Job? = null
    private var swingBack: Job? = null

    /** How far a fling's swing has dipped the page, in drag progress; see [swing]. */
    var swingDip by mutableFloatStateOf(0f)
        private set

    // the release springs overshoot past rest; the page stops there rather than growing past full size
    val progress: Float get() = (if (flying) flight.value else maxOf(edge(), pull)).coerceIn(0f, 1f)

    /** A finger is shrinking the page: the pop it would make is pending. A fling's swing doesn't count. */
    val dragging: Boolean get() = !flying && progress > 0f

    fun dragPull(by: Float) {
        pullRelease?.cancel()
        pull = (pull + by).coerceAtLeast(0f)
    }

    fun releasePull(scope: CoroutineScope, velocity: Float) {
        pullRelease = scope.launch { animate(pull, 0f, velocity, closeSpring()) { value, _ -> pull = value } }
    }

    /**
     * A fling ran into the top at [velocity]: the page dips by its speed and springs back. Kept apart from [pull], so
     * it never closes the page.
     */
    fun swing(scope: CoroutineScope, velocity: Float) {
        if (flying || pull > 0f) return
        val reach = CardMotion.swingReach
        swingBack?.cancel()
        swingBack = scope.launch {
            animate(0f, 0f, CardMotion.swingVelocity(velocity), CardMotion.swingSpring()) { value, _ -> swingDip = value.coerceIn(0f, reach) }
        }
    }

    /** The page has let go: the shrink eases off over its flight home, [velocity] carrying on from the finger. */
    suspend fun fly(velocity: Float) {
        val from = progress
        if (flying || from <= 0f) return
        pullRelease?.cancel()
        flight.snapTo(from)
        flying = true
        flight.animateTo(0f, closeSpring(), velocity)
    }
}

private fun closeSpring() = CardMotion.closeSpring(0.0005f)

// the press a card was let go at, for the page growing out of it to start that size; there's one launch at a time
private var launchScale = 1f

/** Records the press [scale] the tapped card is at, so the page about to grow out of it starts from that size. */
fun launchExpandingPage(scale: Float) {
    launchScale = scale
}

/**
 * The card a page grows out of: it shares [key] with the page's [expandingPage]. While its page flies its place in the
 * feed is empty, as in the App Store's Today feed: the page is drawn as the card all through its flight, and a page
 * still shrunk by the drag that let it go would show the card around its edges. Every other card stays as it is.
 */
@Composable
fun Modifier.expandingCard(key: Any): Modifier {
    val transitions = LocalSharedTransitionScope.current
    val state = transitions?.rememberSharedContentState(key)
    return this
        .sharedTransitionBounds(key, remeasure = true, crossfade = false, transform = CardTransform, state = state)
        // only the card paired with the page in flight: any transition running would hide the whole feed's cards
        .graphicsLayer { alpha = if (transitions?.isTransitionActive == true && state?.isMatchFound == true) 0f else 1f }
}

/**
 * The root of a page that grows out of the card sharing [key] (an `expands` destination), drawing its own top as that
 * card at every width in between (see IdeaCard's expansion). Its corners run from the card's to the screen's.
 *
 * A back edge pan, or a pull down while it is scrolled to its top, shrinks it under the finger; going back up undoes
 * it. Shrunk all the way, or let go fast enough to get there, it closes into the card; anything less springs back. A
 * fling that runs into the top only dips it and springs it back: only a finger closes the page.
 */
@Composable
fun Modifier.expandingPage(key: Any): Modifier {
    val dismiss = LocalExpandDismiss.current
    val transitions = LocalSharedTransitionScope.current
    val arriving = LocalSharedVisibilityScope.current?.transition?.currentState == EnterExitState.PreEnter
    val back = rememberDispatchBack()
    val scope = rememberCoroutineScope()
    val reach = with(LocalDensity.current) { DismissReach.toPx() }
    val launch = remember { Animatable(if (arriving) launchScale.also { launchScale = 1f } else 1f) }
    LaunchedEffect(launch) { launch.animateTo(1f, CardMotion.pressOutSpring()) }
    val deviceRadius = PlataTheme.radii.device
    val cardRadius = PlataTheme.radii.xl
    val cardInset = PlataTheme.spacing.screenMargin
    val windowWidth = LocalWindowInfo.current.containerSize.width.toFloat()
    val growingShape = remember(windowWidth) { GrowingCardShape(windowWidth, cardInset, cardRadius, deviceRadius) }
    val connection = remember(dismiss, reach) {
        object : NestedScrollConnection {
            fun close(velocity: Float) {
                scope.launch(start = CoroutineStart.UNDISPATCHED) { dismiss?.fly(velocity) }
                back()
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (dismiss == null) return Offset.Zero
                // flying home, the content holds still under the finger that let it go
                if (dismiss.flying) return available
                if (dismiss.pull <= 0f || available.y >= 0f || source != NestedScrollSource.UserInput) return Offset.Zero
                val taken = maxOf(available.y, -dismiss.pull * reach)
                dismiss.dragPull(taken / reach)
                return Offset(0f, taken)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // a fling running into the top is left alone: it stops there, and onPostFling swings the page with its speed
                if (dismiss == null || dismiss.flying || available.y <= 0f || source != NestedScrollSource.UserInput) return Offset.Zero
                dismiss.dragPull(available.y / reach)
                if (dismiss.pull >= 1f) close(0f)
                return Offset(0f, available.y)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (dismiss == null) return Velocity.Zero
                if (dismiss.flying) return available
                if (dismiss.pull <= 0f) return Velocity.Zero
                val velocity = available.y / reach
                if (dismiss.pull + velocity * ProjectionSeconds >= 1f) close(velocity) else dismiss.releasePull(scope, velocity)
                return available
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (dismiss == null || dismiss.flying || available.y <= 0f) return Velocity.Zero
                dismiss.swing(scope, available.y / reach)
                return available
            }
        }
    }
    return this
        // on top, opaque and relaid out at every size: its header is drawn as the card, so it grows out of it unbroken
        .sharedTransitionBounds(key, remeasure = true, crossfade = false, zIndex = 1f, transform = CardTransform)
        .graphicsLayer {
            // until the card is found the page would show at full size for a frame; it arrives only as the card
            alpha = if (arriving && transitions?.isTransitionActive != true) 0f else 1f
            val drag = maxOf(dismiss?.progress ?: 0f, dismiss?.swingDip ?: 0f)
            val scale = launch.value * (1f - CardMotion.dragShrink * drag)
            scaleX = scale
            scaleY = scale
            // laid out at the bounds it travels through, so it clips itself there: the overlay's clip lags a frame
            val travelling = transitions?.isTransitionActive == true
            // continuous corners, like the card's and the screen's own: a circular arc meets the edges with a visible kink
            shape = if (travelling) growingShape else ContinuousRoundedShape(lerp(0.dp, deviceRadius, (drag * 4f).coerceAtMost(1f)))
            clip = travelling || drag > 0f
        }
        .nestedScroll(connection)
}

/**
 * The platform's overscroll for an expanding page's content, without its top edge: there the page dips itself (see
 * [expandingPage]), and a bounce would take the fling's speed, sliding the content down past the page's top. The
 * bottom keeps its native bounce.
 */
@Composable
fun rememberExpandingPageOverscroll(): OverscrollEffect? {
    val platform = rememberOverscrollEffect() ?: return null
    return remember(platform) { TopEdgelessOverscroll(platform) }
}

private class TopEdgelessOverscroll(private val platform: OverscrollEffect) : OverscrollEffect {
    // towards the top, nothing reaches the platform's bounce unless the bottom edge is stretched and has to relax
    private fun skips(towardTop: Boolean) = towardTop && !platform.isInProgress

    override fun applyToScroll(delta: Offset, source: NestedScrollSource, performScroll: (Offset) -> Offset): Offset =
        if (skips(delta.y > 0f)) performScroll(delta) else platform.applyToScroll(delta, source, performScroll)

    override suspend fun applyToFling(velocity: Velocity, performFling: suspend (Velocity) -> Velocity) {
        if (skips(velocity.y > 0f)) performFling(velocity) else platform.applyToFling(velocity, performFling)
    }

    override val isInProgress: Boolean get() = platform.isInProgress

    override val node: DelegatableNode get() = platform.node
}

/** Rounds the travelling page like the card at the card's width and like the screen at the window's. */
private class GrowingCardShape(private val windowWidth: Float, private val cardInset: Dp, private val cardRadius: Dp, private val screenRadius: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cardWidth = windowWidth - 2 * with(density) { cardInset.toPx() }
        val grown = ((size.width - cardWidth) / (windowWidth - cardWidth)).coerceIn(0f, 1f)
        return ContinuousRoundedShape(lerp(cardRadius, screenRadius, grown)).createOutline(size, layoutDirection, density)
    }
}

/**
 * The flight between a card and its page, for both sides of the pair: each side keeps the spec it first animated
 * with, so the one spec picks the motion from where the bounds go. Growing is the App Store's opening, two
 * animations at once: a spring carrying the card to its place (top and centre), its length and bounce set by how far
 * the card travels, and a growth of its size (Grow Curve, a spring with no bounce by default). Shrinking splits the same
 * way: the position flies home on a spring a touch under critical while the size, with no bounce, gets there sooner.
 */
private val CardTransform = BoundsTransform { _, _ -> CardFlightSpec }

// a spring that takes about [seconds] for one oscillation, the way UIKit and SwiftUI describe springs by duration
private fun stiffnessFor(seconds: Float): Float = (2 * PI / seconds).let { it * it }.toFloat()

private fun growSpec(seconds: Float): FiniteAnimationSpec<Size> = when (CardMotion.growCurve) {
    GrowCurve.Spring -> spring(1f, stiffnessFor(seconds), Size.VisibilityThreshold)
    GrowCurve.EaseOut -> tween((seconds * 1000).roundToInt(), easing = EaseOutCubic)
    GrowCurve.Linear -> tween((seconds * 1000).roundToInt(), easing = LinearEasing)
}

private object CardFlightSpec : FiniteAnimationSpec<Rect> {
    @Suppress("UNCHECKED_CAST")
    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Rect, V>): VectorizedFiniteAnimationSpec<V> =
        VectorizedCardFlight() as VectorizedFiniteAnimationSpec<V>
}

private class Flight(val move: VectorizedFiniteAnimationSpec<AnimationVector2D>, val grow: VectorizedFiniteAnimationSpec<AnimationVector2D>)

// Rect animates as (left, top, right, bottom); the flight splits that into anchor and size and joins them back
private class VectorizedCardFlight : VectorizedFiniteAnimationSpec<AnimationVector4D> {
    private var from: AnimationVector4D? = null
    private var to: AnimationVector4D? = null
    private var flight: Flight? = null

    private fun flight(initialValue: AnimationVector4D, targetValue: AnimationVector4D): Flight {
        flight?.let { if (from == initialValue && to == targetValue) return it }
        val opening = targetValue.extent().v2 > initialValue.extent().v2
        val next = if (opening) {
            val travel = (abs(initialValue.v2 - targetValue.v2) / max(targetValue.extent().v2, 1f)).coerceIn(0f, 1f)
            val seconds = lerp(CardMotion.openShortest, CardMotion.openLongest, travel) * CardMotion.slowdown
            Flight(
                spring(lerp(CardMotion.openDampingNear, CardMotion.openDampingFar, travel), stiffnessFor(seconds), Offset.VisibilityThreshold).vectorize(Offset.VectorConverter),
                growSpec(seconds * CardMotion.growShare).vectorize(Size.VectorConverter),
            )
        } else {
            Flight(
                CardMotion.closeSpring(Offset.VisibilityThreshold).vectorize(Offset.VectorConverter),
                CardMotion.closeShrinkSpring(Size.VisibilityThreshold).vectorize(Size.VectorConverter),
            )
        }
        from = initialValue.copy()
        to = targetValue.copy()
        flight = next
        return next
    }

    override fun getValueFromNanos(
        playTimeNanos: Long,
        initialValue: AnimationVector4D,
        targetValue: AnimationVector4D,
        initialVelocity: AnimationVector4D,
    ): AnimationVector4D {
        val flight = flight(initialValue, targetValue)
        return join(
            flight.move.getValueFromNanos(playTimeNanos, initialValue.anchor(), targetValue.anchor(), initialVelocity.anchor()),
            flight.grow.getValueFromNanos(playTimeNanos, initialValue.extent(), targetValue.extent(), initialVelocity.extent()),
        )
    }

    override fun getVelocityFromNanos(
        playTimeNanos: Long,
        initialValue: AnimationVector4D,
        targetValue: AnimationVector4D,
        initialVelocity: AnimationVector4D,
    ): AnimationVector4D {
        val flight = flight(initialValue, targetValue)
        return join(
            flight.move.getVelocityFromNanos(playTimeNanos, initialValue.anchor(), targetValue.anchor(), initialVelocity.anchor()),
            flight.grow.getVelocityFromNanos(playTimeNanos, initialValue.extent(), targetValue.extent(), initialVelocity.extent()),
        )
    }

    override fun getDurationNanos(initialValue: AnimationVector4D, targetValue: AnimationVector4D, initialVelocity: AnimationVector4D): Long {
        val flight = flight(initialValue, targetValue)
        return max(
            flight.move.getDurationNanos(initialValue.anchor(), targetValue.anchor(), initialVelocity.anchor()),
            flight.grow.getDurationNanos(initialValue.extent(), targetValue.extent(), initialVelocity.extent()),
        )
    }

    private fun AnimationVector4D.anchor() = AnimationVector2D((v1 + v3) / 2f, v2)

    private fun AnimationVector4D.extent() = AnimationVector2D(v3 - v1, v4 - v2)

    private fun AnimationVector4D.copy() = AnimationVector4D(v1, v2, v3, v4)

    private fun join(anchor: AnimationVector2D, extent: AnimationVector2D) =
        AnimationVector4D(anchor.v1 - extent.v1 / 2f, anchor.v2, anchor.v1 + extent.v1 / 2f, anchor.v2 + extent.v2)
}
