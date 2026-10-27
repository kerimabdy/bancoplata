package tech.bashga.plata.dev.dials

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.PlataText
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.theme.PlataTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot

/** DialKit's own dark palette: the drawer reads the same over any screen, so it doesn't take the app's colours. */
internal object DialTheme {
    val panelBackground = Color(0xFF212121)
    val border = Color.White.copy(alpha = 0.10f)
    val borderSoft = Color.White.copy(alpha = 0.06f)
    val surface = Color.White.copy(alpha = 0.05f)
    val surfaceActive = Color.White.copy(alpha = 0.11f)
    val textRoot = Color.White
    val textSection = Color.White.copy(alpha = 0.70f)
    val textLabel = Color.White.copy(alpha = 0.70f)
    val shadow = Color.Black.copy(alpha = 0.45f)
}

internal val RowShape = ContinuousRoundedShape(8.dp)
internal val RowHeight = 36.dp

@Composable
internal fun dialText(weight: FontWeight = FontWeight.Medium, monospaced: Boolean = false): TextStyle =
    PlataTheme.typography.footnote.copy(fontWeight = weight, fontFamily = if (monospaced) FontFamily.Monospace else PlataTheme.typography.footnote.fontFamily)

/** SwiftUI's `.spring(response:dampingFraction:)`. */
internal fun <T> swiftSpring(response: Float, dampingFraction: Float, visibilityThreshold: T? = null) =
    spring(dampingFraction, ((2 * PI / response) * (2 * PI / response)).toFloat(), visibilityThreshold)

internal fun Modifier.dialRow(): Modifier = fillMaxWidth().height(RowHeight).background(DialTheme.surface, RowShape)

/** [controls] in order, each folder and spring keeping its open state in [panel] under its path. */
@Composable
internal fun DialControlList(panel: DialPanel, controls: List<DialControl>, prefix: String = "") {
    controls.forEachIndexed { index, control ->
        // a folder or spring gets a rule above it when another one comes before it
        val divider = control.usesSectionDivider() && controls.subList(0, index).any { it.usesSectionDivider() }
        val path = "$prefix${control.key}"
        when (control) {
            is SliderDial -> DialSliderRow(control.label, control.value, control.range, control.step, control.unit, control::set)
            is ToggleDial -> DialSegmentedRow(control.label, listOf(false to "Off", true to "On"), control.value, control::set)
            is SelectDial -> DialSelectRow(control)
            is SpringDial -> DialFolderSection(control.label, panel.expanded[path] ?: true, { panel.expanded[path] = it }, divider) {
                DialSpringEditor(control)
            }
            is DialFolder -> DialFolderSection(control.label, panel.expanded[path] ?: !control.collapsed, { panel.expanded[path] = it }, divider) {
                DialControlList(panel, control.controls, "$path.")
            }
            is ActionDial -> DialActionButton(control.label, control.trigger)
            is DialPanel -> Unit
        }
    }
}

private fun DialControl.usesSectionDivider() = this is SpringDial || this is DialFolder

private val FolderSpring = 0.35f to 0.9f

@Composable
private fun DialFolderSection(title: String, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, showsTopDivider: Boolean, content: @Composable ColumnScope.() -> Unit) {
    val (response, damping) = FolderSpring
    val chevron by animateFloatAsState(if (expanded) 0f else 180f, swiftSpring(response, damping))
    Column(
        Modifier
            .padding(vertical = 4.dp)
            .drawWithContent {
                drawContent()
                if (showsTopDivider) drawRect(DialTheme.borderSoft, size = Size(size.width, 1.dp.toPx()))
            },
    ) {
        Row(
            Modifier.fillMaxWidth().height(RowHeight).pressable { onExpandedChange(!expanded) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlataText(title, dialText(FontWeight.SemiBold), DialTheme.textSection, maxLines = 1)
            Spacer(Modifier.weight(1f))
            DialIcon(DialGlyph.ChevronDown, 12.dp, DialTheme.textLabel.copy(alpha = 0.7f * 0.8f), Modifier.rotate(chevron), strokeWidth = 2.6f)
        }
        AnimatedVisibility(
            expanded,
            enter = expandVertically(swiftSpring(response, damping), expandFrom = Alignment.Top) + fadeIn(swiftSpring(response, damping)),
            exit = shrinkVertically(swiftSpring(response, damping), shrinkTowards = Alignment.Top) + fadeOut(swiftSpring(response, damping)),
        ) {
            Column(Modifier.padding(bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
        }
    }
}

// a drag this far decides between moving the slider and scrolling the drawer; sideways wins only by a clear margin
private val SliderActivation = 10.dp
private const val SliderHorizontalBias = 1.25f

/**
 * DialKit's slider: the whole row is the track. A touch lights it up; dragging sideways moves the value by how far the
 * finger travels (not to where it is), snapping to [step] with a selection tick per step; dragging up or down lets
 * the drawer scroll instead.
 */
@Composable
internal fun DialSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    unit: String?,
    onChange: (Float) -> Unit,
) {
    var interacting by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val current by rememberUpdatedState(value)
    val change by rememberUpdatedState(onChange)
    val span = range.endInclusive - range.start
    val progress = if (span > 0f) ((value - range.start) / span).coerceIn(0f, 1f) else 0f
    BoxWithConstraints(
        Modifier
            .dialRow()
            .clip(RowShape)
            .pointerInput(range, step) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    interacting = true
                    val start = current
                    var last = start
                    var sliding = false
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!pointer.pressed) break
                            val travel = pointer.position - down.position
                            if (!sliding) {
                                if (hypot(travel.x, travel.y) < SliderActivation.toPx()) continue
                                if (abs(travel.x) <= abs(travel.y) * SliderHorizontalBias) break
                                sliding = true
                            }
                            pointer.consume()
                            val next = dialRound(start + travel.x / size.width.coerceAtLeast(1) * span, step, range)
                            if (next != last) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            last = next
                            change(next)
                        }
                    } finally {
                        interacting = false
                    }
                }
            },
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(11) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(1.dp, 8.dp).background(Color.White.copy(alpha = if (interacting) 0.15f else 0f), RoundedCornerShape(0.5.dp)))
                }
            }
        }
        Box(Modifier.fillMaxHeight().width(maxWidth * progress).background(Color.White.copy(alpha = if (interacting) 0.14f else 0.10f), RowShape))
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = max(maxWidth * progress - 2.dp, 6.dp))
                .size(3.dp, 20.dp)
                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(99.dp)),
        )
        Row(Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            PlataText(title, dialText(), DialTheme.textLabel, maxLines = 1)
            Spacer(Modifier.weight(1f).widthIn(min = 12.dp))
            PlataText(dialFormattedNumber(value, step) + (unit ?: ""), dialText(monospaced = true), if (interacting) Color.White else DialTheme.textLabel, maxLines = 1)
        }
    }
}

@Composable
internal fun <T> DialSegmentedRow(title: String, options: List<Pair<T, String>>, selection: T, onSelect: (T) -> Unit) {
    Row(Modifier.dialRow().padding(start = 12.dp, end = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        PlataText(title, dialText(), DialTheme.textLabel, maxLines = 1)
        Spacer(Modifier.weight(1f).widthIn(min = 8.dp))
        DialSegmentedControl(options, selection, onSelect)
    }
}

private val SegmentWidth = 56.dp

@Composable
private fun <T> DialSegmentedControl(options: List<Pair<T, String>>, selection: T, onSelect: (T) -> Unit) {
    val width = SegmentWidth * maxOf(options.size, 2)
    val segment = width / maxOf(options.size, 1)
    val selected = options.indexOfFirst { it.first == selection }.coerceAtLeast(0)
    val thumb by animateFloatAsState(selected.toFloat(), swiftSpring(0.22f, 0.84f))
    Box(Modifier.size(width, 32.dp).background(DialTheme.surface, RowShape).clip(RowShape)) {
        Box(
            Modifier
                .offset(x = segment * thumb + 2.dp, y = 2.dp)
                .size(segment - 4.dp, 28.dp)
                .background(DialTheme.surfaceActive, ContinuousRoundedShape(6.dp)),
        )
        Row(Modifier.fillMaxSize().padding(2.dp)) {
            options.forEach { (value, label) ->
                Box(Modifier.weight(1f).fillMaxHeight().pressable(indication = null) { onSelect(value) }, contentAlignment = Alignment.Center) {
                    PlataText(label, dialText(), if (value == selection) Color.White.copy(alpha = 0.82f) else DialTheme.textLabel, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun DialSelectRow(control: SelectDial) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier.dialRow().pressable { open = true }.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlataText(control.label, dialText(), DialTheme.textLabel, maxLines = 1)
            Spacer(Modifier.weight(1f).widthIn(min = 2.dp))
            PlataText(control.options.firstOrNull { it.value == control.value }?.label ?: control.value, dialText(), DialTheme.textLabel, maxLines = 1)
            DialIcon(DialGlyph.ChevronDown, 10.dp, DialTheme.textLabel.copy(alpha = 0.7f * 0.7f), strokeWidth = 2.8f)
        }
        if (open) {
            DialMenu(control.options.map { option -> DialMenuItem.Choice(option.label, checked = option.value == control.value) { control.set(option.value) } }) { open = false }
        }
    }
}

@Composable
private fun DialActionButton(title: String, onClick: () -> Unit) {
    Row(
        Modifier.dialRow().pressable(onClick = onClick).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DialIcon(DialGlyph.Bolt, 13.dp, DialTheme.textLabel)
        PlataText(title, dialText(), DialTheme.textLabel, maxLines = 1)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun DialSpringEditor(control: SpringDial) {
    val spring = control.value
    DialSpringVisualization(spring, Modifier.fillMaxWidth().height(140.dp))
    DialSegmentedRow("Type", listOf(true to "Time", false to "Physics"), spring is DialSpring.Time) { time ->
        control.set(if (time) control.value.updatingTime() else control.value.updatingPhysics())
    }
    when (spring) {
        is DialSpring.Time -> {
            DialSliderRow("Duration", spring.duration, 0.1f..1f, 0.05f, "s") { control.set(control.value.updatingTime(duration = it)) }
            DialSliderRow("Bounce", spring.bounce, 0f..1f, 0.05f, null) { control.set(control.value.updatingTime(bounce = it)) }
        }
        is DialSpring.Physics -> {
            DialSliderRow("Stiffness", spring.stiffness, 1f..1000f, 1f, null) { control.set(control.value.updatingPhysics(stiffness = it)) }
            DialSliderRow("Damping", spring.damping, 1f..100f, 1f, null) { control.set(control.value.updatingPhysics(damping = it)) }
            DialSliderRow("Mass", spring.mass, 0.1f..10f, 0.1f, null) { control.set(control.value.updatingPhysics(mass = it)) }
        }
    }
}

/** The spring's step response over two seconds, stretched to fill the middle 60% of the box whatever its overshoot. */
@Composable
private fun DialSpringVisualization(spring: DialSpring, modifier: Modifier = Modifier) {
    val physics = spring.resolved
    val values = remember(physics.stiffness, physics.damping, physics.mass) { springCurve(physics) }
    Canvas(modifier.background(DialTheme.surface, RowShape).clip(RowShape)) {
        val grid = Color.White.copy(alpha = 0.08f)
        for (i in 1..3) {
            val y = size.height / 4 * i
            val x = size.width / 4 * i
            drawLine(grid, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            drawLine(grid, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
        }
        val dash = 4.dp.toPx()
        drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)))
        val low = values.min()
        val range = (values.max() - low).coerceAtLeast(0.001)
        val curve = Path()
        values.forEachIndexed { i, v ->
            val x = i.toFloat() / (values.size - 1) * size.width
            val y = size.height - ((v - low) / range).toFloat() * size.height * 0.6f - size.height * 0.2f
            if (i == 0) curve.moveTo(x, y) else curve.lineTo(x, y)
        }
        drawPath(curve, Color.White.copy(alpha = 0.62f), style = Stroke(2.dp.toPx()))
    }
}

// DialKit integrates the spring with Euler steps; matching it keeps the curve identical, quirks included
private fun springCurve(physics: ResolvedSpring): List<Double> {
    val steps = 100
    val dt = 2.0 / steps
    var position = 0.0
    var velocity = 0.0
    return List(steps + 1) {
        val value = position
        val acceleration = (-physics.stiffness * (position - 1.0) - physics.damping * velocity) / physics.mass
        velocity += acceleration * dt
        position += velocity * dt
        value
    }
}
