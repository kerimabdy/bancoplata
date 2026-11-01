package tech.bashga.plata.dev.dials

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataSafeArea
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

enum class DialPosition { TopRight, TopLeft, BottomRight, BottomLeft }

private enum class DrawerPresentation { Hidden, Medium, Tall }

// a drag on the drawer shorter than this keeps it where it is; longer steps it one size up or down
private val DrawerStepDrag = 40.dp
private val DrawerDragMinimum = 16.dp
private val DrawerShape = ContinuousRoundedShape(24.dp)
private val DrawerInset = 8.dp
private val ContentInset = 12.dp

private fun DrawerPresentation.next(dragUp: Boolean): DrawerPresentation = when {
    dragUp && this == DrawerPresentation.Hidden -> DrawerPresentation.Medium
    dragUp -> DrawerPresentation.Tall
    this == DrawerPresentation.Tall -> DrawerPresentation.Medium
    else -> DrawerPresentation.Hidden
}

/**
 * DialKit's drawer over [content]: a draggable button, or a two-finger tap anywhere, opens a sheet of every
 * [DialStore] panel from the bottom. The sheet comes at a medium height and drags up to tall or down to close; a tap
 * outside closes it.
 */
@Composable
fun DialRoot(
    modifier: Modifier = Modifier,
    position: DialPosition = DialPosition.BottomRight,
    defaultOpen: Boolean = false,
    content: @Composable () -> Unit,
) {
    val panels = DialStore.panels
    var openRequest by remember { mutableIntStateOf(0) }
    BoxWithConstraints(modifier.fillMaxSize().pointerInput(Unit) { detectTwoFingerTap { openRequest++ } }) {
        content()
        if (panels.isEmpty()) return@BoxWithConstraints
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current
        val hiddenOffset = with(density) { (maxHeight + 24.dp).toPx() }
        val stepDrag = with(density) { DrawerStepDrag.toPx() }
        var presentation by remember { mutableStateOf(if (defaultOpen) DrawerPresentation.Medium else DrawerPresentation.Hidden) }
        // the height the drawer keeps while it slides away, so it doesn't collapse on the way down
        var visual by remember { mutableStateOf(DrawerPresentation.Medium) }
        var overlayShown by remember { mutableStateOf(defaultOpen) }
        val slide = remember { Animatable(if (defaultOpen) 0f else hiddenOffset) }
        var selectedPanel by remember { mutableIntStateOf(0) }
        val panel = panels.getOrElse(selectedPanel) { panels.first() }

        fun present(target: DrawerPresentation) {
            visual = target
            presentation = target
            overlayShown = true
            scope.launch { slide.animateTo(0f, DrawerSpring) }
        }

        fun hide() {
            presentation = DrawerPresentation.Hidden
            scope.launch { slide.animateTo(hiddenOffset, DrawerSpring) }
            scope.launch {
                delay(360)
                if (presentation == DrawerPresentation.Hidden) overlayShown = false
            }
        }

        val shown = if (presentation == DrawerPresentation.Hidden) visual else presentation
        val medium = min(maxHeight * 0.58f, 560.dp)
        val tall = min(maxHeight * 0.9f, maxHeight - 12.dp)
        LaunchedEffect(openRequest) {
            if (openRequest > 0 && presentation == DrawerPresentation.Hidden) present(DrawerPresentation.Medium)
        }
        val cap by animateDpAsState(if (shown == DrawerPresentation.Tall) tall else medium, drawerHeightSpring)

        if (overlayShown) {
            DialLayer(IntOffset.Zero) {
                Box(Modifier.fillMaxSize()) {
                    if (presentation != DrawerPresentation.Hidden) {
                        Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { hide() } })
                    }
                    DialDrawerPanel(
                        panel = panel,
                        panels = panels,
                        onSelectPanel = { selectedPanel = it },
                        maxHeight = cap,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = DrawerInset)
                            .offset { IntOffset(0, slide.value.roundToInt()) },
                        onDrag = { dy ->
                            if (abs(dy) > stepDrag) {
                                val target = presentation.next(dragUp = dy < 0)
                                if (target == DrawerPresentation.Hidden) hide() else if (target != presentation) present(target)
                            }
                        },
                    )
                }
            }
        }
        AnimatedVisibility(!overlayShown, enter = fadeIn(swiftSpring(0.22f, 0.9f)), exit = fadeOut(swiftSpring(0.22f, 0.9f))) {
            DialFab(position, maxWidth, maxHeight) { present(DrawerPresentation.Medium) }
        }
    }
}

private val DrawerSpring = swiftSpring<Float>(0.32f, 0.86f)

private val drawerHeightSpring = swiftSpring(0.32f, 0.86f, visibilityThreshold = 0.5.dp)

private val FabDiameter = 56.dp
private val FabColor = Color(0.13f, 0.13f, 0.13f, 0.96f)

@Composable
private fun DialFab(position: DialPosition, width: Dp, height: Dp, onClick: () -> Unit) {
    val density = LocalDensity.current
    val safeArea = PlataSafeArea.current
    var dragging by remember { mutableStateOf(false) }
    val lift by animateFloatAsState(if (dragging) 1f else 0f, tween(160, easing = EaseOut))
    with(density) {
        val radius = FabDiameter.toPx() / 2
        val minX = radius + 8.dp.toPx()
        val maxX = maxOf(minX, width.toPx() - radius - 8.dp.toPx())
        val minY = radius + 8.dp.toPx() + safeArea.top.toPx()
        val maxY = maxOf(minY, height.toPx() - radius - 2.dp.toPx() - safeArea.bottom.toPx())
        fun clamp(point: Offset) = Offset(point.x.coerceIn(minX, maxX), point.y.coerceIn(minY, maxY))
        val default = when (position) {
            DialPosition.TopRight -> Offset(maxX, minY)
            DialPosition.TopLeft -> Offset(minX, minY)
            DialPosition.BottomRight -> Offset(maxX, maxY)
            DialPosition.BottomLeft -> Offset(minX, maxY)
        }
        val at = clamp(DialStore.fabCenter ?: default)
        DialLayer(IntOffset((at.x - radius).roundToInt(), (at.y - radius).roundToInt())) {
            Box(
                Modifier
                    .size(FabDiameter)
                    .graphicsLayer {
                        scaleX = lerp(1f, 1.24f, lift)
                        scaleY = scaleX
                    }
                    .dropShadow(
                        CircleShape,
                        Shadow(
                            radius = lerp(16f, 28f, lift).dp,
                            color = Color.Black.copy(alpha = lerp(0.25f, 0.68f, lift)),
                            offset = DpOffset(0.dp, lerp(4f, 0f, lift).dp),
                        ),
                    )
                    .background(FabColor, CircleShape)
                    .border(1.dp, DialTheme.border, CircleShape)
                    .pointerInput(Unit) { detectTapGestures { onClick() } }
                    .pointerInput(minX, maxX, minY, maxY) {
                        detectDragGestures(
                            onDragStart = { dragging = true },
                            onDragEnd = { dragging = false },
                            onDragCancel = { dragging = false },
                        ) { change, amount ->
                            change.consume()
                            DialStore.fabCenter = clamp((DialStore.fabCenter ?: default) + amount)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                DialIcon(DialGlyph.Sliders, 22.dp, Color.White, strokeWidth = 1.9f)
            }
        }
    }
}

/**
 * The drawer and the button, placed at [offset] from the top left. On iOS they go in a popup, the one Compose layer
 * drawn above native views such as the tab bar; elsewhere they draw in place.
 */
@Composable
internal expect fun DialLayer(offset: IntOffset, content: @Composable () -> Unit)

@Composable
private fun DialDrawerPanel(
    panel: DialPanel,
    panels: List<DialPanel>,
    onSelectPanel: (Int) -> Unit,
    maxHeight: Dp,
    onDrag: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Column(
        modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .dropShadow(DrawerShape, Shadow(radius = 32.dp, color = DialTheme.shadow, offset = DpOffset(0.dp, 8.dp)))
            .clip(DrawerShape)
            .background(DialTheme.panelBackground.copy(alpha = 0.94f))
            .border(1.dp, DialTheme.border, DrawerShape)
            .pointerInput(Unit) {
                // a drag the controls didn't take (a slider, the scroll) moves the drawer once the finger lifts
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
                    var claimed = false
                    var last = down.position
                    while (true) {
                        val pointer = awaitPointerEvent(PointerEventPass.Final).changes.firstOrNull { it.id == down.id } ?: return@awaitEachGesture
                        last = pointer.position
                        if (!pointer.pressed) break
                        if (pointer.isConsumed) claimed = true
                    }
                    val travel = last - down.position
                    if (!claimed && hypot(travel.x, travel.y) >= DrawerDragMinimum.toPx()) onDrag(travel.y)
                }
            },
    ) {
        Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(42.dp, 5.dp).background(Color.White.copy(alpha = 0.22f), CircleShape))
        }
        if (panels.size > 1) DialPanelPicker(panel, panels, onSelectPanel, Modifier.padding(horizontal = ContentInset).padding(bottom = 8.dp))
        DialToolbar(panel, Modifier.padding(horizontal = ContentInset).padding(bottom = 6.dp))
        Column(
            Modifier
                .weight(1f, fill = false)
                // content that fits doesn't scroll, so a drag anywhere on it can move the drawer
                .verticalScroll(scroll, enabled = scroll.maxValue > 0)
                .padding(horizontal = ContentInset)
                .padding(bottom = ContentInset),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            DialControlList(panel, panel.controls)
        }
    }
}

@Composable
private fun DialPanelPicker(panel: DialPanel, panels: List<DialPanel>, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            Modifier.fillMaxWidth().height(32.dp).background(DialTheme.surface, RowShape).pressable { open = true }.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlataText(panel.name, dialText(FontWeight.SemiBold), DialTheme.textRoot, maxLines = 1)
            Spacer(Modifier.weight(1f))
            DialIcon(DialGlyph.ChevronDown, 10.dp, DialTheme.textLabel.copy(alpha = 0.7f * 0.8f), strokeWidth = 2.8f)
        }
        if (open) DialMenu(panels.mapIndexed { i, candidate -> DialMenuItem.Choice(candidate.name, checked = candidate === panel) { onSelect(i) } }) { open = false }
    }
}

@Composable
private fun DialToolbar(panel: DialPanel, modifier: Modifier = Modifier) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }
    var presetsOpen by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1400)
            copied = false
        }
    }
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(RowHeight).background(DialTheme.surface, RowShape).pressable { panel.savePreset() }, contentAlignment = Alignment.Center) {
            DialIcon(DialGlyph.AddVersion, 18.dp, DialTheme.textLabel, strokeWidth = 1.8f)
        }
        Box(Modifier.weight(1f)) {
            Row(
                Modifier.dialRow().pressable { presetsOpen = true }.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlataText(panel.activePresetName, dialText(), DialTheme.textLabel, Modifier.weight(1f), maxLines = 1)
                DialIcon(DialGlyph.ChevronDown, 10.dp, DialTheme.textLabel.copy(alpha = 0.7f * 0.6f), strokeWidth = 2.8f)
            }
            if (presetsOpen) {
                val active = panel.activePresetId
                val items = buildList {
                    add(DialMenuItem.Choice("Version 1", checked = active == null) { panel.clearActivePreset() })
                    panel.presets.forEach { preset -> add(DialMenuItem.Choice(preset.name, checked = preset.id == active) { panel.loadPreset(preset.id) }) }
                    if (active != null) {
                        add(DialMenuItem.Divider)
                        add(DialMenuItem.Choice("Delete Current Preset", destructive = true) { panel.deletePreset(active) })
                    }
                }
                DialMenu(items) { presetsOpen = false }
            }
        }
        Row(
            Modifier
                .height(RowHeight)
                .background(DialTheme.surface, RowShape)
                .pressable {
                    val text = panel.copyInstructionText()
                    scope.launch { clipboard.setClipEntry(dialClipEntry(text)) }
                    copied = true
                }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            DialIcon(if (copied) DialGlyph.Check else DialGlyph.Copy, 13.dp, DialTheme.textLabel, strokeWidth = 2.2f)
            PlataText("Copy", dialText(), DialTheme.textLabel, maxLines = 1)
        }
    }
}

// one tap of two fingers together: quick, held still, no third finger
private const val TwoFingerTapMillis = 400L

/**
 * Watches every touch on the way down without taking it, so the app underneath works as before. Only a finished
 * two-finger tap is kept from the app (its lift is consumed), so the tap doesn't also press whatever is under it.
 */
private suspend fun PointerInputScope.detectTwoFingerTap(onTap: () -> Unit) = awaitEachGesture {
    val first = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
    val starts = mutableMapOf(first.id to first.position)
    var twoDown = false
    var still = true
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Initial)
        event.changes.forEach { if (it.changedToDownIgnoreConsumed()) starts[it.id] = it.position }
        if (event.changes.count { it.pressed } == 2) twoDown = true
        if (starts.size > 2) still = false
        if (event.changes.any { it.pressed && (it.position - (starts[it.id] ?: it.position)).getDistance() > viewConfiguration.touchSlop }) still = false
        if (event.changes.none { it.pressed }) {
            if (twoDown && still && event.changes.first().uptimeMillis - first.uptimeMillis <= TwoFingerTapMillis) {
                event.changes.forEach { it.consume() }
                onTap()
            }
            break
        }
    }
}
