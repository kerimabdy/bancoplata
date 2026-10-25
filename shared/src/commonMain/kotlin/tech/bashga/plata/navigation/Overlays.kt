package tech.bashga.plata.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.bashga.plata.designsystem.components.BackGestureShrink
import tech.bashga.plata.designsystem.components.LocalModalMotion
import tech.bashga.plata.designsystem.components.ModalMotion
import tech.bashga.plata.designsystem.components.PageSheetBackdrop
import tech.bashga.plata.designsystem.components.PageSheetSurface
import tech.bashga.plata.designsystem.components.SheetPresenter
import tech.bashga.plata.designsystem.foundation.ProvideBackdrop

internal const val PresentMillis = 350

/** False while a screen is covered by another destination, so its back handlers stay out of the way. */
val LocalIsTopEntry = compositionLocalOf { true }

/**
 * System back and the iOS edge swipe, for the screen on top only. Use it for back that means something other than
 * "pop me" (a step screen's previous step, DeliveryDetails' close event); without one, back pops the top destination.
 */
@Composable
fun PlataBackHandler(enabled: Boolean = true, onBack: () -> Unit) = PlataBackHandler(enabled, gesture = null, onBack)

/** Android's predictive back gesture driving an overlay: [progress] follows the finger, springs back on cancel. */
internal class OverlayBackGesture {
    val progress = Animatable(0f)
    var committed by mutableStateOf(false)
}

@Composable
internal fun PlataBackHandler(enabled: Boolean, gesture: OverlayBackGesture?, onBack: () -> Unit) {
    if (LocalNavigationEventDispatcherOwner.current == null) return
    val state = rememberNavigationEventState(NavigationEventInfo.None)
    if (gesture != null && platformBackStyle == BackStyle.Android) {
        LaunchedEffect(state, gesture) {
            snapshotFlow { state.transitionState }.collectLatest { transition ->
                if (transition is NavigationEventTransitionState.InProgress) {
                    if (transition.direction == NavigationEventTransitionState.TRANSITIONING_BACK) gesture.progress.snapTo(transition.latestEvent.progress)
                } else if (!gesture.committed) {
                    gesture.progress.animateTo(0f, navSpring())
                }
            }
        }
    }
    NavigationBackHandler(
        state,
        isBackEnabled = enabled && LocalIsTopEntry.current,
        onBackCompleted = {
            gesture?.committed = true
            onBack()
        },
    )
}

/** Sends a back event through the dispatcher, so the top screen's [PlataBackHandler] (or the host's pop) handles it. */
@Composable
internal fun rememberDispatchBack(): () -> Unit {
    val dispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher ?: return {}
    val input = remember(dispatcher) { DirectNavigationEventInput() }
    DisposableEffect(dispatcher, input) {
        dispatcher.addInput(input)
        onDispose { dispatcher.removeInput(input) }
    }
    return input::backCompleted
}

internal sealed interface OverlayKind {
    data object Plain : OverlayKind

    data class Sheet(val backdrop: PageSheetBackdrop) : OverlayKind
}

internal class OverlayRequest(kind: OverlayKind, visible: Boolean, content: @Composable () -> Unit) {
    var kind by mutableStateOf(kind)
    var visible by mutableStateOf(visible)
    var content by mutableStateOf(content)
    var onDismiss: () -> Unit = {}
    var drag by mutableFloatStateOf(0f)
    val progress = Animatable(if (visible) 1f else 0f)
    val back = OverlayBackGesture()
    var presentations by mutableIntStateOf(0)
}

private val OverlayRequest.presented get() = progress.value * (1f - drag)

class OverlayHostState internal constructor() {
    internal val requests = mutableStateListOf<OverlayRequest>()
}

private val LocalOverlayHost = staticCompositionLocalOf<OverlayHostState?> { null }

/**
 * Hosts the overlays of everything inside it: [Overlay]s draw over [content] (and over the tab bar), a Stacked
 * [SheetOverlay] shrinks [content] into the card behind it, a Dimmed one dims it. The nav host puts one around every
 * layer; the screenshot harness puts one around the screen under test. [cardTop] is where [content]'s card starts.
 */
@Composable
fun OverlayHost(modifier: Modifier = Modifier, cardTop: Dp = 0.dp, content: @Composable () -> Unit) {
    val host = remember { OverlayHostState() }
    CompositionLocalProvider(LocalOverlayHost provides host) {
        val sheets = host.requests.filter { it.kind is OverlayKind.Sheet }
        val stacked = sheets.filter { (it.kind as OverlayKind.Sheet).backdrop == PageSheetBackdrop.Stacked }.maxOfOrNull { it.presented } ?: 0f
        val dimmed = sheets.filter { (it.kind as OverlayKind.Sheet).backdrop == PageSheetBackdrop.Dimmed }.maxOfOrNull { it.presented } ?: 0f
        Box(modifier.fillMaxSize()) {
            SheetPresenter(stacked, dimmed, cardTop = cardTop, content = content)
            host.requests.forEach { request -> key(request) { OverlayFrame(request) } }
        }
    }
}

@Composable
private fun OverlayFrame(request: OverlayRequest) {
    LaunchedEffect(request.visible) {
        if (request.visible && request.progress.value < 1f) {
            request.presentations++
            request.back.committed = false
            request.back.progress.snapTo(0f)
        }
        request.progress.animateTo(if (request.visible) 1f else 0f, navSpring())
    }
    // the overlay's own back handler may refuse (a confirm instead): spring back from the gesture
    LaunchedEffect(request.visible, request.back.committed) {
        if (!request.visible || !request.back.committed) return@LaunchedEffect
        repeat(2) { withFrameNanos {} }
        request.back.committed = false
        request.back.progress.animateTo(0f, navSpring())
    }
    val hidden by remember(request) { derivedStateOf { request.progress.value == 0f } }
    if (hidden) return
    when (request.kind) {
        OverlayKind.Plain -> {
            val motion = remember(request) { ModalMotion(progress = { request.progress.value }, back = { request.back.progress.value }) }
            Box(Modifier.fillMaxSize().graphicsLayer { alpha = if (motion.slides) 1f else request.progress.value }) {
                CompositionLocalProvider(LocalModalMotion provides motion) { request.content() }
            }
        }
        is OverlayKind.Sheet -> Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = (1f - request.progress.value) * size.height
                    scaleX = 1f - BackGestureShrink * request.back.progress.value
                    scaleY = scaleX
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
                .blockPointers(),
        ) {
            PageSheetSurface(Modifier.swipeToDismiss(request.onDismiss, { request.drag = it }, resetKey = request.presentations)) { request.content() }
        }
    }
}

@Composable
private fun <T : Any> RegisterOverlay(value: T?, kind: OverlayKind, onDismiss: () -> Unit, dismissOnBack: Boolean, content: @Composable (T) -> Unit) {
    val host = LocalOverlayHost.current ?: error("Overlays need an OverlayHost (the nav host and the screenshot harness provide one)")
    var last by remember { mutableStateOf(value) }
    if (value != null) last = value
    val shown = last
    val latestContent by rememberUpdatedState(content)
    val request = remember { OverlayRequest(kind, value != null, {}) }
    request.kind = kind
    request.visible = value != null
    request.onDismiss = onDismiss
    request.content = { if (shown != null) ProvideBackdrop { latestContent(shown) } }
    // Added after composition: a write inside the host's first composition never invalidates the host's read.
    DisposableEffect(request) {
        host.requests += request
        onDispose { host.requests -= request }
    }
    PlataBackHandler(enabled = value != null && dismissOnBack, gesture = request.back, onBack = onDismiss)
}

/**
 * A dialog, alert, bottom sheet or toast over the host screen, shown while [value] is non-null. [content] draws the
 * whole layer including its scrim (`DialogPlacement`, `ModalScrim`); it keeps the last value while fading out.
 * Back calls [onDismiss] unless [dismissOnBack] is false (toasts).
 */
@Composable
fun <T : Any> Overlay(value: T?, onDismiss: () -> Unit, dismissOnBack: Boolean = true, content: @Composable (T) -> Unit) =
    RegisterOverlay(value, OverlayKind.Plain, onDismiss, dismissOnBack, content)

/** A page sheet over the host screen that returns to it (Otp, PictureIntro, PersonalDetails, Signature). */
@Composable
fun <T : Any> SheetOverlay(
    value: T?,
    onDismiss: () -> Unit,
    backdrop: PageSheetBackdrop = PageSheetBackdrop.Stacked,
    content: @Composable (T) -> Unit,
) = RegisterOverlay(value, OverlayKind.Sheet(backdrop), onDismiss, dismissOnBack = true, content)

/** Swallows touches so nothing under a presented layer reacts. */
internal fun Modifier.blockPointers(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) awaitPointerEvent()
    }
}

private const val DismissFraction = 0.5f
private val DismissVelocity = 1000.dp
private val RubberBandLimit = 32.dp
private const val RubberBandStiffness = 0.55f

private class SheetDrag(private val rubberLimit: Float, private val dismissVelocity: Float) {
    var offset by mutableFloatStateOf(0f)
        private set
    var height = 0
    var coordinates: LayoutCoordinates? = null
    var onDismiss: () -> Unit = {}
    var onDrag: (Float) -> Unit = {}
    var onThreshold: () -> Unit = {}
    private var raw = 0f
    private var settle: Job? = null

    private val pastThreshold get() = height > 0 && offset > height * DismissFraction

    private fun show(value: Float) {
        offset = value
        onDrag(if (height > 0) value.coerceAtLeast(0f) / height else 0f)
    }

    fun grab() {
        settle?.cancel()
        settle = null
        raw = if (offset >= 0f) offset else -(rubberLimit / (rubberLimit + offset) - 1f) * rubberLimit / RubberBandStiffness
    }

    fun dragBy(delta: Float) {
        if (settle != null) grab()
        val wasPast = pastThreshold
        raw += delta
        show(if (raw >= 0f) raw else -rubberLimit * (1f - 1f / (-raw * RubberBandStiffness / rubberLimit + 1f)))
        if (pastThreshold != wasPast) onThreshold()
    }

    fun release(scope: CoroutineScope, velocity: Float) {
        settle?.cancel()
        settle = scope.launch {
            if (offset > 0f && (pastThreshold || velocity > dismissVelocity)) {
                springTo(height.toFloat(), velocity)
                val top = coordinates?.takeIf { it.isAttached }?.positionInRoot()?.y
                onDismiss()
                if (top == null || leaving(top)) return@launch
            }
            springTo(0f, if (raw >= 0f) velocity else velocity * rubberSlope())
        }
    }

    private fun rubberSlope(): Float {
        val stretch = -raw * RubberBandStiffness / rubberLimit + 1f
        return RubberBandStiffness / (stretch * stretch)
    }

    private suspend fun springTo(target: Float, velocity: Float) =
        animate(offset, target, velocity, navSpring(visibilityThreshold = 0.5f)) { value, _ -> show(value) }

    // the host starts moving the layer within a few frames of a dismissal it accepts
    private suspend fun leaving(top: Float): Boolean {
        repeat(8) {
            withFrameNanos {}
            val now = coordinates?.takeIf { it.isAttached } ?: return true
            if (now.positionInRoot().y > top + 1f) return true
        }
        return false
    }
}

/**
 * Drag a sheet down (or overscroll its content at the top) to dismiss it, as iOS page sheets do. [onDrag] gets how far
 * down it is as a fraction of its height, so the presenter behind can follow it. A new [resetKey] puts it back at rest.
 */
@Composable
internal fun Modifier.swipeToDismiss(onDismiss: () -> Unit, onDrag: (Float) -> Unit = {}, resetKey: Any? = null): Modifier {
    val density = LocalDensity.current
    val sheet = remember(resetKey, density) { with(density) { SheetDrag(RubberBandLimit.toPx(), DismissVelocity.toPx()) } }
    val haptics = LocalHapticFeedback.current
    sheet.onDismiss = onDismiss
    sheet.onDrag = onDrag
    sheet.onThreshold = { haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate) }
    val scope = rememberCoroutineScope()
    val connection = remember(sheet, scope) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (sheet.offset <= 0f || available.y >= 0f) return Offset.Zero
                val used = maxOf(available.y, -sheet.offset)
                sheet.dragBy(used)
                return Offset(0f, used)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput || available.y <= 0f) return Offset.Zero
                sheet.dragBy(available.y)
                return Offset(0f, available.y)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (sheet.offset <= 0f) return Velocity.Zero
                sheet.release(scope, available.y)
                return available
            }
        }
    }
    return this
        .onPlaced { sheet.coordinates = it }
        .onSizeChanged { sheet.height = it.height }
        .graphicsLayer { translationY = sheet.offset }
        .nestedScroll(connection)
        .draggable(
            rememberDraggableState { sheet.dragBy(it) },
            Orientation.Vertical,
            onDragStarted = { sheet.grab() },
            onDragStopped = { velocity -> sheet.release(scope, velocity) },
        )
}
