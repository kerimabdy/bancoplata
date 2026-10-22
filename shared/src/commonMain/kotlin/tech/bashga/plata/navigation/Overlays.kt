package tech.bashga.plata.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.bashga.plata.designsystem.components.PageSheetBackdrop
import tech.bashga.plata.designsystem.components.PageSheetSurface
import tech.bashga.plata.designsystem.components.SheetPresenter
import kotlin.math.roundToInt

internal const val PresentMillis = 350

/** False while a screen is covered by another destination, so its back handlers stay out of the way. */
val LocalIsTopEntry = compositionLocalOf { true }

/**
 * System back and the iOS edge swipe, for the screen on top only. Use it for back that means something other than
 * "pop me" (a step screen's previous step, DeliveryDetails' close event); without one, back pops the top destination.
 */
@Composable
fun PlataBackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    if (LocalNavigationEventDispatcherOwner.current == null) return
    NavigationBackHandler(
        rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = enabled && LocalIsTopEntry.current,
        onBackCompleted = onBack,
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
    LaunchedEffect(request.visible) { request.progress.animateTo(if (request.visible) 1f else 0f, tween(PresentMillis)) }
    val progress = request.progress.value
    if (progress == 0f) return
    when (request.kind) {
        OverlayKind.Plain -> Box(Modifier.fillMaxSize().graphicsLayer { alpha = progress }) { request.content() }
        is OverlayKind.Sheet -> Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = (1f - progress) * size.height }
                .blockPointers(),
        ) {
            PageSheetSurface(Modifier.swipeToDismiss(request.onDismiss) { request.drag = it }) { request.content() }
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
    request.content = { if (shown != null) latestContent(shown) }
    // Added after composition: a write inside the host's first composition never invalidates the host's read.
    DisposableEffect(request) {
        host.requests += request
        onDispose { host.requests -= request }
    }
    PlataBackHandler(enabled = value != null && dismissOnBack, onBack = onDismiss)
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

private val DismissDistance = 120.dp

/**
 * Drag a sheet down (or overscroll its content at the top) to dismiss it, as iOS page sheets do. [onDrag] gets how far
 * down it is as a fraction of its height, so the presenter behind can follow it.
 */
@Composable
internal fun Modifier.swipeToDismiss(onDismiss: () -> Unit, onDrag: (Float) -> Unit = {}): Modifier {
    val offset = remember { Animatable(0f) }
    var height by remember { mutableIntStateOf(0) }
    val drag by rememberUpdatedState(onDrag)
    LaunchedEffect(offset) {
        snapshotFlow { if (height > 0) offset.value / height else 0f }.collect { drag(it) }
    }
    val scope = rememberCoroutineScope()
    val threshold = with(LocalDensity.current) { DismissDistance.toPx() }
    val dismiss by rememberUpdatedState(onDismiss)
    val settle: (Float) -> Unit = { velocity ->
        scope.launch {
            if (offset.value > threshold || velocity > threshold * 8) {
                dismiss()
                // stay put while the sheet leaves, so the presenter keeps following it; spring back only if it didn't
                delay(PresentMillis.toLong())
            }
            offset.animateTo(0f, tween(PresentMillis))
        }
    }
    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (offset.value <= 0f || available.y >= 0f) return Offset.Zero
                val used = maxOf(available.y, -offset.value)
                scope.launch { offset.snapTo(offset.value + used) }
                return Offset(0f, used)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput || available.y <= 0f) return Offset.Zero
                scope.launch { offset.snapTo(offset.value + available.y) }
                return Offset(0f, available.y)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offset.value <= 0f) return Velocity.Zero
                settle(available.y)
                return available
            }
        }
    }
    return this
        .onSizeChanged { height = it.height }
        .offset { IntOffset(0, offset.value.roundToInt()) }
        .nestedScroll(connection)
        .draggable(
            rememberDraggableState { delta -> scope.launch { offset.snapTo((offset.value + delta).coerceAtLeast(0f)) } },
            Orientation.Vertical,
            onDragStopped = { velocity -> settle(velocity) },
        )
}
