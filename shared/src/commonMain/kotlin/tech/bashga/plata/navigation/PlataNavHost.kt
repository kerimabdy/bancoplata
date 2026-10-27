package tech.bashga.plata.navigation

import androidx.compose.animation.DeferredAnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.DeferredTransitionState
import androidx.compose.animation.core.ExperimentalDeferredTransitionApi
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.LocalOwnersProvider
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlin.time.TimeSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tech.bashga.plata.designsystem.components.PageSheetBackdrop
import tech.bashga.plata.designsystem.components.PageSheetSurface
import tech.bashga.plata.designsystem.components.PageSheetTop
import tech.bashga.plata.designsystem.components.SheetPresenter
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.LocalSharedTransitionScope
import tech.bashga.plata.designsystem.foundation.LocalSharedVisibilityScope
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.ProvideBackdrop
import tech.bashga.plata.designsystem.foundation.ProvideSafeArea
import tech.bashga.plata.designsystem.theme.PlataTheme

/** A run of back-stack entries drawn together: the base screen, or a sheet or cover with what was pushed inside it. */
@Immutable
private class Layer(val entries: List<NavBackStackEntry>, val depth: Int) {
    val root get() = entries.first()
    val top get() = entries.last()
    val key: String get() = root.id
    val presentation get() = if (depth == 0) Presentation.Push else root.plataDestination.presentation

    override fun equals(other: Any?) = other is Layer && other.entries == entries && other.depth == depth

    override fun hashCode() = entries.hashCode() * 31 + depth
}

private fun List<NavBackStackEntry>.toLayers(): List<Layer> {
    val runs = mutableListOf<MutableList<NavBackStackEntry>>()
    forEach { entry ->
        if (runs.isEmpty() || entry.plataDestination.presentation != Presentation.Push) runs += mutableListOf(entry) else runs.last() += entry
    }
    return runs.mapIndexed { depth, run -> Layer(run, depth) }
}

private fun Layer.backdrop(below: Layer?): PageSheetBackdrop =
    root.plataDestination.backdrop ?: if (below?.presentation == Presentation.Cover) PageSheetBackdrop.Dimmed else PageSheetBackdrop.Stacked

/**
 * Draws the [PlataNavigator] back stack as layers: the base screen (inside [baseFrame], the tab shell), then each
 * sheet or cover above it. A Stacked sheet shrinks the layer under it into a card on black; a Dimmed sheet dims it;
 * pushes slide within their layer. Back pops the top entry.
 */
@Composable
fun PlataNavHost(
    controller: NavHostController,
    navigator: PlataNavigator,
    graph: NavGraph,
    modifier: Modifier = Modifier,
    baseFrame: @Composable (top: NavBackStackEntry, content: @Composable () -> Unit) -> Unit = { _, content -> content() },
    style: BackStyle = platformBackStyle,
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) { "PlataNavHost needs a ViewModelStoreOwner" }
    controller.setViewModelStore(viewModelStoreOwner.viewModelStore)
    controller.graph = graph
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        controller.setLifecycleOwner(lifecycleOwner)
        onDispose {}
    }

    val backStack by navigator.backStack.collectAsState()
    val inTransition by navigator.transitionsInProgress.collectAsState()
    val topEntry = backStack.lastOrNull()
    val gesture = remember { BackGesture() }
    val edgeReach = rememberEdgeReach()
    if (LocalNavigationEventDispatcherOwner.current != null) {
        val backState = rememberNavigationEventState(NavigationEventInfo.None)
        val scope = rememberCoroutineScope()
        LaunchedEffect(backState) {
            snapshotFlow { backState.transitionState }.collect { state ->
                if (state is NavigationEventTransitionState.InProgress && state.direction == NavigationEventTransitionState.TRANSITIONING_BACK) {
                    gesture.track(navigator.backStack.value.lastOrNull(), state.latestEvent)
                }
            }
        }
        NavigationBackHandler(
            backState,
            // still listening while a gesture that already popped runs out, or its end never arrives and the next one is lost
            isBackEnabled = backStack.size > 1 || gesture.early,
            onBackCancelled = {
                val expands = gesture.entry?.plataDestination?.expands == true
                when {
                    gesture.early -> gesture.endEarly()
                    // an expanding page closes on a flick that would carry it all the way, as a pull down does
                    expands && style == BackStyle.Ios && gesture.projected() >= edgeReach -> {
                        gesture.release()
                        controller.popBackStack()
                    }
                    // UIKit's edge pan cancels on a slow release past halfway, where UINavigationController would still pop
                    !expands && style == BackStyle.Ios && gesture.entry != null && gesture.shouldCommit() -> {
                        gesture.release()
                        controller.popBackStack()
                    }
                    else -> scope.launch { gesture.cancel() }
                }
            },
            onBackCompleted = {
                if (gesture.early) {
                    gesture.endEarly()
                } else {
                    gesture.release()
                    controller.popBackStack()
                }
            },
        )
        // an expanding page shrunk all the way lets go into its card while the finger is still down
        LaunchedEffect(gesture, edgeReach) {
            snapshotFlow { gesture.entry?.plataDestination?.expands == true && gesture.progress.value >= edgeReach }.collect { full ->
                if (full) {
                    gesture.releaseEarly()
                    controller.popBackStack()
                }
            }
        }
    }
    val reduceMotion = rememberReduceMotion()

    val holder = rememberSaveableStateHolder()
    val composed = remember { mutableStateListOf<NavBackStackEntry>() }
    val layers = remember(backStack) { backStack.toLayers() }
    val transition = updateTransition(layers, label = "layers")
    val exiting = transition.currentState.filter { old -> layers.none { it.key == old.key } }
    val drawn = (layers + exiting).sortedWith(compareBy<Layer> { it.depth }.thenBy { it !in exiting })

    val visibility = drawn.associate { layer ->
        val entrance = key(layer.key) {
            transition.animateFloat({ navSpring() }, label = "layer") { state ->
                if (state.any { it.key == layer.key }) 1f else 0f
            }.value
        }
        val predictive = if (style == BackStyle.Android && layer.depth > 0 && layer.entries.size == 1) gesture.predictive(layer.top) else 0f
        layer.key to entrance * (1f - SheetBackLift * predictive)
    }
    val drag = remember { mutableStateMapOf<Any, Float>() }
    // how presented a sheet is: its entrance, less however far it is dragged down
    val presented = { layer: Layer -> visibility.getValue(layer.key) * (1f - (drag[layer.key] ?: 0f)) }
    val baseStacked = drawn.filter { it.depth == 1 && it.presentation == Presentation.Sheet }.maxOfOrNull(presented) ?: 0f

    SharedTransitions {
        Box(
            modifier
                .fillMaxSize()
                .background(PlataTheme.colors.sheetBackdrop)
                // a root replace fades through the page colour; under a sheet the black shows at once
                .then(if (baseStacked == 0f) Modifier.background(PlataTheme.colors.surfaceBackground) else Modifier),
        ) {
            drawn.forEach { layer ->
                key(layer.key) {
                    val above = drawn.filter { it.depth == layer.depth + 1 && it.presentation == Presentation.Sheet }
                    val stacked = above.filter { it.backdrop(layer) == PageSheetBackdrop.Stacked }.maxOfOrNull(presented) ?: 0f
                    val dimmed = above.filter { it.backdrop(layer) == PageSheetBackdrop.Dimmed }.maxOfOrNull(presented) ?: 0f
                    DisposableEffect(layer.key) { onDispose { drag -= layer.key } }
                    LayerFrame(
                        presentation = layer.presentation,
                        visibility = visibility.getValue(layer.key),
                        stacked = stacked,
                        dimmed = dimmed,
                        covered = drawn.filter { it.depth == layer.depth + 1 }.maxOfOrNull(presented) ?: 0f,
                        isTop = layer.top == topEntry,
                        blocksBelow = layer.depth > 0,
                        baseFrame = { content -> baseFrame(layer.top, content) },
                        leaving = layer in exiting,
                        onSheetDrag = { drag[layer.key] = it },
                    ) {
                        val swiped = if (layer.top == topEntry && layer.entries.size > 1) gesture.takeIf { it.entry == layer.top } else null
                        LayerContent(layer, backStack, gesture, swiped, style, reduceMotion, edgeReach, isTopLayer = layer.top == topEntry) { entry, isTop ->
                            EntryContent(entry, isTop, holder, navigator, composed)
                        }
                    }
                }
            }
        }
    }

    // entries popped without ever being drawn (the lower entries of a dismissed layer) finish here
    LaunchedEffect(inTransition, composed.toList()) {
        inTransition.filter { it !in backStack && it !in composed }.forEach(navigator::onTransitionComplete)
    }
}

/** [DismissReach] as back-gesture progress, which runs across the window's width. */
@Composable
private fun rememberEdgeReach(): Float {
    val reach = with(LocalDensity.current) { DismissReach.toPx() }
    val width = LocalWindowInfo.current.containerSize.width
    return if (width > 0) reach / width else 1f
}

/** How far a predictive back may lower a sheet or cover, as a fraction of its entrance. */
private const val SheetBackLift = 0.1f

private val PredictiveEasing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)

/** Rightward release speed, in screen widths per second, that commits an iOS swipe short of halfway. */
private const val FlingCommit = 2.5f

/**
 * The system back gesture in flight (Android predictive back, the iOS edge swipe), as the host's handler sees it. Only
 * reaches here when no screen's [PlataBackHandler] claims back, so it always means "pop the top entry".
 */
@Stable
private class BackGesture {
    var entry by mutableStateOf<NavBackStackEntry?>(null)
        private set
    val progress = Animatable(0f)
    var edge by mutableStateOf(NavigationEvent.EDGE_LEFT)
        private set

    /** Set on commit: the entry the gesture popped and where it let go, for the pop to carry on from. */
    var released: NavBackStackEntry? = null
        private set
    var releasedAt = 0f
        private set
    var releasedVelocity = 0f
        private set

    /** Where the top screen still is in its push slide; the layer drawing it sets this so a swipe can grab it mid-push. */
    var pushOffset: (NavBackStackEntry) -> Float = { 0f }
    private var grabbedAt = 0f

    private var sampledAt = TimeSource.Monotonic.markNow()
    private var frameMillis = 0L
    private var velocity = 0f

    /** Set when the gesture let go of an expanding page before the finger lifted; the rest of that gesture is spent. */
    var early by mutableStateOf(false)
        private set

    suspend fun track(top: NavBackStackEntry?, event: NavigationEvent) {
        if (early) return
        if (entry == null) {
            entry = top
            edge = event.swipeEdge
            velocity = 0f
            grabbedAt = top?.let(pushOffset) ?: 0f
        } else {
            val seconds = if (event.frameTimeMillis > 0 && frameMillis > 0) {
                (event.frameTimeMillis - frameMillis) / 1_000f
            } else {
                sampledAt.elapsedNow().inWholeMicroseconds / 1_000_000f
            }
            if (seconds > 0f) velocity = (grabbed(event.progress) - progress.value) / seconds
        }
        sampledAt = TimeSource.Monotonic.markNow()
        frameMillis = event.frameTimeMillis
        progress.snapTo(grabbed(event.progress))
    }

    private fun grabbed(fingerProgress: Float) = grabbedAt + fingerProgress * (1f - grabbedAt)

    /** True when an iOS swipe was let go far or fast enough that the edge recogniser's cancel should be overruled. */
    fun shouldCommit() = progress.value > 0.5f || velocity > FlingCommit

    /** Where a release at the current speed would carry the swipe, in progress. */
    fun projected() = progress.value + velocity * ProjectionSeconds

    fun release() {
        released = entry
        releasedAt = progress.value
        releasedVelocity = velocity
        entry = null
    }

    fun releaseEarly() {
        release()
        early = true
    }

    fun endEarly() {
        early = false
    }

    suspend fun cancel() {
        progress.animateTo(0f, navSpring(), velocity)
        entry = null
    }

    /** Eased predictive-back progress for [top]: live while swiping, frozen at release while it leaves. */
    fun predictive(top: NavBackStackEntry): Float = when (top) {
        entry -> PredictiveEasing.transform(progress.value)
        released -> PredictiveEasing.transform(releasedAt)
        else -> 0f
    }
}

/**
 * One layer of the stack at [visibility] (0–1, its entrance), with the treatment the layer above gives it ([stacked],
 * [dimmed]). A [leaving] base layer stays put under the root that replaces it. The nav host and the screenshot harness
 * both draw layers with it.
 */
@Composable
internal fun LayerFrame(
    presentation: Presentation,
    visibility: Float,
    stacked: Float,
    dimmed: Float,
    isTop: Boolean,
    blocksBelow: Boolean,
    baseFrame: @Composable (content: @Composable () -> Unit) -> Unit,
    covered: Float = maxOf(stacked, dimmed),
    leaving: Boolean = false,
    onSheetDrag: (Float) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val cardTop = if (presentation == Presentation.Sheet) PageSheetTop else 0.dp
    val coveredState = rememberUpdatedState(covered)
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                when (presentation) {
                    Presentation.Push -> if (visibility < 1f && !leaving) {
                        // a root replace: the new root fades in over the old one, which stays opaque underneath
                        alpha = visibility
                        scaleX = 0.96f + 0.04f * visibility
                        scaleY = scaleX
                    }
                    Presentation.Sheet, Presentation.Cover -> translationY = (1f - visibility) * size.height
                }
            }
            .then(if (isTop) Modifier else Modifier.clearAndSetSemantics {})
            .then(if (blocksBelow) Modifier.blockPointers() else Modifier),
    ) {
        SheetPresenter(stacked, dimmed, cardTop = cardTop) {
            CompositionLocalProvider(LocalCovered provides { coveredState.value }) {
                OverlayHost(cardTop = cardTop) {
                    ProvideBackdrop {
                        when (presentation) {
                            Presentation.Push -> Box(Modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) { baseFrame(content) }
                            Presentation.Sheet -> {
                                val dispatchBack = rememberDispatchBack()
                                PageSheetSurface(Modifier.swipeToDismiss(dispatchBack, onSheetDrag)) { content() }
                            }
                            Presentation.Cover -> Box(Modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) { content() }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A push or pop between [front] and [behind] inside one layer. [offset] is how far [front] has gone (0 on screen,
 * 1 off it); it springs to [target], after which only the screen left on top is drawn. [predictive] is the Android
 * back-gesture shrink a pop leaves from, swiped from [edge].
 */
@Stable
private class PushMotion(
    val front: NavBackStackEntry,
    val behind: NavBackStackEntry?,
    val from: Float,
    val target: Float,
    val velocity: Float = 0f,
    val predictive: Float = 0f,
    val edge: Int = NavigationEvent.EDGE_LEFT,
) {
    val offset = Animatable(from)
    var settled by mutableStateOf(behind == null)
    val top get() = if (target == 1f) checkNotNull(behind) else front
}

@OptIn(ExperimentalDeferredTransitionApi::class)
@Composable
private fun LayerContent(
    layer: Layer,
    backStack: List<NavBackStackEntry>,
    gesture: BackGesture,
    swiped: BackGesture?,
    style: BackStyle,
    reduceMotion: Boolean,
    edgeReach: Float,
    isTopLayer: Boolean,
    entryContent: @Composable (NavBackStackEntry, Boolean) -> Unit,
) {
    val last = remember { arrayOfNulls<PushMotion>(1) }
    val motion = remember(layer.top) {
        val prev = last[0]
        val shown = prev?.top
        when {
            prev == null || shown == null || shown == layer.top || isTabSwitch(shown, layer.top) -> PushMotion(layer.top, null, 0f, 0f)
            shown !in backStack -> when {
                // an expanding page doesn't slide: it flies into its card from wherever the drag left it
                shown.plataDestination.expands && !reduceMotion -> PushMotion(shown, layer.top, 0f, 1f)
                gesture.released == shown -> PushMotion(
                    shown, layer.top,
                    from = if (style == BackStyle.Ios) gesture.releasedAt else 0f,
                    target = 1f,
                    velocity = if (style == BackStyle.Ios) gesture.releasedVelocity else 0f,
                    predictive = if (style == BackStyle.Android) PredictiveEasing.transform(gesture.releasedAt) else 0f,
                    edge = gesture.edge,
                )
                !prev.settled && prev.behind == layer.top -> PushMotion(shown, layer.top, prev.offset.value, 1f, prev.offset.velocity)
                else -> PushMotion(shown, layer.top, 0f, 1f)
            }
            else -> PushMotion(layer.top, shown, 1f, 0f)
        }.also { last[0] = it }
    }
    val pushing = { entry: NavBackStackEntry -> !motion.settled && motion.target == 0f && motion.front == entry }
    if (isTopLayer && style == BackStyle.Ios) {
        SideEffect { gesture.pushOffset = { entry -> if (pushing(entry)) motion.offset.value else 0f } }
    }
    val grabbed = swiped != null && pushing(layer.top)
    val page = if (motion.settled) motion.top else motion.front
    val expanding = !reduceMotion && page.plataDestination.expands
    val dismiss = remember(page) { ExpandDismiss() }
    SideEffect {
        dismiss.edge = {
            when (page) {
                gesture.entry -> gesture.progress.value / edgeReach
                gesture.released -> gesture.releasedAt / edgeReach
                else -> 0f
            }
        }
    }
    val dragging by remember(dismiss) { derivedStateOf { dismiss.dragging } }
    // shrunk under a finger, the page's pop is pending: the page under it comes in, still held where it was. A fling's
    // swing leaves it out: bringing the page under in builds it from scratch, a stall in the frame the swing starts
    val dismissing = expanding && motion.settled && dragging
    // each entry's visibility, which its shared elements read as incoming or outgoing
    val transitions = remember { mutableStateMapOf<String, Transition<Boolean>>() }
    LaunchedEffect(motion, grabbed) {
        if (motion.settled) return@LaunchedEffect
        // the swipe now drives the slide from where it caught it, like an interrupted UIKit push
        if (!grabbed) motion.offset.animateTo(motion.target, navSpring(), motion.velocity)
        // both pages stay composed until their shared elements have landed
        snapshotFlow { transitions.values.all { !it.isRunning && it.currentState == it.targetState } }.first { it }
        motion.settled = true
    }
    LaunchedEffect(motion) {
        if (expanding && motion.target == 1f && gesture.released == motion.front) dismiss.fly(gesture.releasedVelocity / edgeReach)
    }

    val swipeBehind = if ((swiped != null || dismissing) && motion.settled && motion.top == layer.top && layer.entries.size > 1) {
        layer.entries[layer.entries.lastIndex - 1]
    } else {
        null
    }
    val front = if (motion.settled) motion.top else motion.front
    val behind = if (motion.settled) swipeBehind else motion.behind
    val look = when {
        reduceMotion -> PushLook.Fade
        expanding -> PushLook.Expand
        style == BackStyle.Ios -> PushLook.Ios
        else -> PushLook.Android
    }
    val pose = when {
        !motion.settled -> PushPose(motion.offset.value, motion.predictive, motion.edge)
        swiped == null || look == PushLook.Expand -> PushPose(0f, 0f, NavigationEvent.EDGE_LEFT)
        style == BackStyle.Ios -> PushPose(swiped.progress.value, 0f, swiped.edge)
        else -> PushPose(0f, PredictiveEasing.transform(swiped.progress.value), swiped.edge)
    }

    Box(Modifier.fillMaxSize()) {
        listOfNotNull(behind, front).forEach { entry ->
            key(entry.id) {
                val isFront = entry == front && behind != null
                val isBehind = entry == behind
                // only the top of an expanding pair is shown; the other is there for the card they share
                val shown = !expanding || entry == layer.top
                // a newly pushed page starts hidden so an expanding push can grow it out of the card it shares
                val state = remember { DeferredTransitionState((entry != layer.top || motion.settled) && shown) }
                when {
                    shown != state.targetState -> state.animateTo(shown)
                    dismissing && entry == front -> state.defer(false)
                    dismissing && entry == behind -> state.defer(true)
                    !dismissing && state.pendingTargetState != null -> state.defer(state.targetState)
                }
                val transition = rememberTransition(state, label = "entry")
                DisposableEffect(transition) {
                    transitions[entry.id] = transition
                    onDispose { transitions -= entry.id }
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .then(if (isFront) Modifier.pushFront(pose, look) else if (isBehind) Modifier.pushBehind(pose, look) else Modifier)
                        // the expanding page brings its own page colour inside the bounds it grows through
                        .then(if (isFront && look == PushLook.Expand) Modifier else Modifier.background(PlataTheme.colors.surfaceBackground)),
                ) {
                    transition.DeferredAnimatedVisibility({ it }, enter = EnterTransition.None, exit = ExitTransition.None) {
                        CompositionLocalProvider(
                            LocalSharedVisibilityScope provides this,
                            LocalExpandDismiss provides if (expanding && entry == page) dismiss else null,
                        ) {
                            ProvideBackdrop { entryContent(entry, isTopLayer && entry == layer.top && entry == front) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitions(content: @Composable () -> Unit) = SharedTransitionLayout {
    CompositionLocalProvider(LocalSharedTransitionScope provides this, content = content)
}

private enum class PushLook { Ios, Android, Fade, Expand }

/** Where a push or pop is: [offset] 0–1 how far the front screen has gone, [predictive] 0–1 its back-gesture shrink. */
private class PushPose(val offset: Float, val predictive: Float, val edge: Int)

@Composable
private fun Modifier.pushFront(pose: PushPose, look: PushLook): Modifier {
    val shade = PlataTheme.colors.sheetBackdrop
    val deviceShape = ContinuousRoundedShape(PlataTheme.radii.device)
    val inset = PlataTheme.spacing.s8
    val shadowWidth = PlataTheme.spacing.s16
    return when (look) {
        PushLook.Fade -> graphicsLayer { alpha = 1f - pose.offset }
        // the page moves itself (expandingPage): it shrinks under a drag and travels in the shared transition overlay
        PushLook.Expand -> this
        PushLook.Ios -> this
            .graphicsLayer { translationX = pose.offset * size.width }
            // shaped like the screen so the edge shadow rounds its corners; top, bottom and right fall off screen
            .dropShadow(deviceShape) {
                radius = shadowWidth.toPx()
                color = shade
                alpha = 0.2f * (1f - pose.offset)
            }
            // after the shadow, so the clip rounds the screen but not the shadow outside it
            .graphicsLayer {
                shape = deviceShape
                clip = pose.offset > 0f
            }
        PushLook.Android -> graphicsLayer {
            shape = deviceShape
            clip = pose.offset > 0f || pose.predictive > 0f
            val toward = if (pose.edge == NavigationEvent.EDGE_RIGHT) -1f else 1f
            if (pose.predictive > 0f) {
                val scale = 1f - 0.1f * pose.predictive
                scaleX = scale
                scaleY = scale
                translationX = toward * (pose.predictive * (size.width / 20f - inset.toPx()) + pose.offset * size.width / 4f)
                alpha = 1f - pose.offset
            } else {
                translationX = pose.offset * size.width
            }
        }
    }
}

@Composable
private fun Modifier.pushBehind(pose: PushPose, look: PushLook): Modifier {
    val shade = PlataTheme.colors.sheetBackdrop
    return when (look) {
        PushLook.Fade -> this
        // the page under an expanding card blurs and dims as the card grows over it, like the App Store's Today feed
        PushLook.Expand -> this
            .graphicsLayer {
                val radius = ExpandBlur.toPx() * (1f - pose.offset)
                renderEffect = if (radius > 0f) BlurEffect(radius, radius) else null
            }
            .drawWithContent {
                drawContent()
                drawRect(shade.copy(alpha = ExpandDim * (1f - pose.offset)))
            }
        PushLook.Ios -> this
            .graphicsLayer { translationX = -0.3f * size.width * (1f - pose.offset) }
            .drawWithContent {
                drawContent()
                drawRect(shade.copy(alpha = 0.1f * (1f - pose.offset)))
            }
        // a light scrim, so the shrinking screen's edges read against a page of the same colour
        PushLook.Android -> if (pose.predictive > 0f) {
            drawWithContent {
                drawContent()
                drawRect(shade.copy(alpha = 0.1f * pose.predictive * (1f - pose.offset)))
            }
        } else {
            graphicsLayer { translationX = -0.3f * size.width * (1f - pose.offset) }
        }
    }
}

private const val ExpandDim = 0.2f
private val ExpandBlur = 12.dp

private fun isTabSwitch(from: NavBackStackEntry, to: NavBackStackEntry): Boolean {
    val a = from.destination.tab
    val b = to.destination.tab
    return a != null && b != null && a != b
}

@Composable
private fun EntryContent(
    entry: NavBackStackEntry,
    isTop: Boolean,
    holder: SaveableStateHolder,
    navigator: PlataNavigator,
    composed: MutableList<NavBackStackEntry>,
) {
    DisposableEffect(entry) {
        composed += entry
        onDispose {
            composed -= entry
            if (entry in navigator.transitionsInProgress.value && entry !in navigator.backStack.value) navigator.onTransitionComplete(entry)
        }
    }
    // per entry, so a route that hides the bar doesn't shorten the tab page it is pushed over
    val tabBarInset = if (entry.destination.tab != null && !entry.plataDestination.hidesTabBar) LocalTabBarInset.current else 0.dp
    val safeArea = PlataSafeArea.current
    entry.LocalOwnersProvider(holder) {
        ProvideSafeArea(safeArea.copy(bottom = safeArea.bottom + tabBarInset)) {
            CompositionLocalProvider(LocalIsTopEntry provides isTop) { entry.plataDestination.content(entry) }
        }
    }
}

/** A layer for [StaticLayers]: what the screenshot harness stacks to reproduce a presented reference. */
class StaticLayer(val presentation: Presentation, val backdrop: PageSheetBackdrop? = null, val content: @Composable () -> Unit)

/** [layers] bottom first, fully presented, with the same presenter treatment the nav host gives them. */
@Composable
internal fun StaticLayers(layers: List<StaticLayer>, baseFrame: @Composable (content: @Composable () -> Unit) -> Unit = { it() }) {
    Box(Modifier.fillMaxSize().background(PlataTheme.colors.sheetBackdrop)) {
        layers.forEachIndexed { index, layer ->
            val above = layers.getOrNull(index + 1)?.takeIf { it.presentation == Presentation.Sheet }
            val backdrop = above?.let { it.backdrop ?: if (layer.presentation == Presentation.Cover) PageSheetBackdrop.Dimmed else PageSheetBackdrop.Stacked }
            LayerFrame(
                presentation = layer.presentation,
                visibility = 1f,
                stacked = if (backdrop == PageSheetBackdrop.Stacked) 1f else 0f,
                dimmed = if (backdrop == PageSheetBackdrop.Dimmed) 1f else 0f,
                isTop = index == layers.lastIndex,
                blocksBelow = index > 0,
                covered = if (index < layers.lastIndex) 1f else 0f,
                baseFrame = if (index == 0) baseFrame else { content -> content() },
                content = layer.content,
            )
        }
    }
}

/** The nav host's root-replace transition from [from] to [to], for mid-transition references under a paused clock. */
@Composable
internal fun RootReplaceTransition(from: @Composable () -> Unit, to: @Composable () -> Unit) {
    var replaced by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { replaced = true }
    val transition = updateTransition(replaced, label = "replace")
    val incoming by transition.animateFloat({ navSpring() }, label = "to") { if (it) 1f else 0f }
    Box(Modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        if (incoming < 1f) LayerFrame(Presentation.Push, 0f, 0f, 0f, isTop = false, blocksBelow = false, baseFrame = { it() }, leaving = true, content = from)
        if (incoming > 0f) LayerFrame(Presentation.Push, incoming, 0f, 0f, isTop = true, blocksBelow = false, baseFrame = { it() }, content = to)
    }
}
