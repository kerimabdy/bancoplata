package tech.bashga.plata.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.LocalOwnersProvider
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import tech.bashga.plata.designsystem.components.PageSheetBackdrop
import tech.bashga.plata.designsystem.components.PageSheetSurface
import tech.bashga.plata.designsystem.components.PageSheetTop
import tech.bashga.plata.designsystem.components.SheetPresenter
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
    if (LocalNavigationEventDispatcherOwner.current != null) {
        NavigationBackHandler(
            rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = backStack.size > 1,
            onBackCompleted = { controller.popBackStack() },
        )
    }

    val holder = rememberSaveableStateHolder()
    val composed = remember { mutableStateListOf<NavBackStackEntry>() }
    val layers = remember(backStack) { backStack.toLayers() }
    val transition = updateTransition(layers, label = "layers")
    val exiting = transition.currentState.filter { old -> layers.none { it.key == old.key } }
    val drawn = (layers + exiting).sortedWith(compareBy<Layer> { it.depth }.thenBy { it !in exiting })

    val visibility = drawn.associate { layer ->
        layer.key to key(layer.key) {
            transition.animateFloat({ layerSpec() }, label = "layer") { state ->
                if (state.any { it.key == layer.key }) 1f else 0f
            }.value
        }
    }
    val topEntry = backStack.lastOrNull()
    val drag = remember { mutableStateMapOf<Any, Float>() }
    // how presented a sheet is: its entrance, less however far it is dragged down
    val presented = { layer: Layer -> visibility.getValue(layer.key) * (1f - (drag[layer.key] ?: 0f)) }
    val baseStacked = drawn.filter { it.depth == 1 && it.presentation == Presentation.Sheet }.maxOfOrNull(presented) ?: 0f

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
                    isTop = layer.top == topEntry,
                    blocksBelow = layer.depth > 0,
                    baseFrame = { content -> baseFrame(layer.top, content) },
                    onSheetDrag = { drag[layer.key] = it },
                ) {
                    LayerContent(layer, backStack, isTopLayer = layer.top == topEntry) { entry, isTop ->
                        EntryContent(entry, isTop, holder, navigator, composed)
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

internal fun <T> layerSpec() = tween<T>(PresentMillis, easing = FastOutSlowInEasing)

/**
 * One layer of the stack at [visibility] (0–1, its entrance), with the treatment the layer above gives it ([stacked],
 * [dimmed]). The nav host and the screenshot harness both draw layers with it.
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
    onSheetDrag: (Float) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val cardTop = if (presentation == Presentation.Sheet) PageSheetTop else 0.dp
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                when (presentation) {
                    Presentation.Push -> if (visibility < 1f) {
                        // a root replace: the new root zooms in as the old one fades
                        alpha = visibility
                        scaleX = 0.85f + 0.15f * visibility
                        scaleY = scaleX
                    }
                    Presentation.Sheet, Presentation.Cover -> translationY = (1f - visibility) * size.height
                }
            }
            .then(if (isTop) Modifier else Modifier.clearAndSetSemantics {})
            .then(if (blocksBelow) Modifier.blockPointers() else Modifier),
    ) {
        SheetPresenter(stacked, dimmed, cardTop = cardTop) {
            OverlayHost(cardTop = cardTop) {
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

@Composable
private fun LayerContent(
    layer: Layer,
    backStack: List<NavBackStackEntry>,
    isTopLayer: Boolean,
    entryContent: @Composable (NavBackStackEntry, Boolean) -> Unit,
) {
    AnimatedContent(
        targetState = layer.top,
        contentKey = { it.id },
        transitionSpec = {
            val pop = initialState !in backStack
            when {
                isTabSwitch(initialState, targetState) -> EnterTransition.None togetherWith ExitTransition.None
                pop -> (slideInHorizontally(tween(PresentMillis)) { -it / 3 } togetherWith slideOutHorizontally(tween(PresentMillis)) { it })
                    .apply { targetContentZIndex = -1f }
                else -> slideInHorizontally(tween(PresentMillis)) { it } togetherWith slideOutHorizontally(tween(PresentMillis)) { -it / 3 }
            }
        },
        label = "push",
    ) { entry ->
        entryContent(entry, isTopLayer && entry == layer.top)
    }
}

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
    entry.LocalOwnersProvider(holder) {
        CompositionLocalProvider(LocalIsTopEntry provides isTop) { entry.plataDestination.content(entry) }
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
    val outgoing by transition.animateFloat({ layerSpec() }, label = "from") { if (it) 0f else 1f }
    val incoming by transition.animateFloat({ layerSpec() }, label = "to") { if (it) 1f else 0f }
    Box(Modifier.fillMaxSize().background(PlataTheme.colors.surfaceBackground)) {
        if (outgoing > 0f) LayerFrame(Presentation.Push, outgoing, 0f, 0f, isTop = false, blocksBelow = false, baseFrame = { it() }, content = from)
        if (incoming > 0f) LayerFrame(Presentation.Push, incoming, 0f, 0f, isTop = true, blocksBelow = false, baseFrame = { it() }, content = to)
    }
}
