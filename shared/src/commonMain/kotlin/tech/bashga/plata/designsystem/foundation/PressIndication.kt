package tech.bashga.plata.designsystem.foundation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.launch

/** iOS-style highlight: the pressed element fades instead of showing a ripple. Installed as `LocalIndication` by `PlataTheme`. */
object PlataPressIndication : IndicationNodeFactory {
    const val PRESSED_ALPHA = 0.6f

    override fun create(interactionSource: InteractionSource): DelegatableNode = PressFadeNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = 0x504c4154
}

private class PressFadeNode(private val interactionSource: InteractionSource) : Modifier.Node(), DrawModifierNode {
    private var pressed = false
    private val layerPaint = Paint().apply { alpha = PlataPressIndication.PRESSED_ALPHA }

    override fun onAttach() {
        coroutineScope.launch {
            val presses = mutableListOf<PressInteraction.Press>()
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> presses += interaction
                    is PressInteraction.Release -> presses -= interaction.press
                    is PressInteraction.Cancel -> presses -= interaction.press
                }
                val nowPressed = presses.isNotEmpty()
                if (nowPressed != pressed) {
                    pressed = nowPressed
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        if (!pressed) {
            drawContent()
            return
        }
        drawIntoCanvas { canvas ->
            // inflate so shadows drawn outside the bounds fade with the content
            canvas.saveLayer(Rect(-size.width, -size.height, size.width * 2, size.height * 2), layerPaint)
            drawContent()
            canvas.restore()
        }
    }
}

/**
 * Shrinks the element from the instant a finger lands. `clickable` holds its press 150 ms inside scrolling
 * containers on iOS, so this reads raw pointer-down instead and lets go once a scroll claims the touch.
 */
fun Modifier.pressScale(enabled: Boolean = true): Modifier = this then PressScaleElement(enabled)

private const val PRESSED_SCALE = 0.97f

// critically damped springs: ~0.15 s response going in, ~0.3 s coming back out
private val PressInSpring = spring<Float>(dampingRatio = 1f, stiffness = 1750f, visibilityThreshold = 0.0001f)
private val PressOutSpring = spring<Float>(dampingRatio = 1f, stiffness = 440f, visibilityThreshold = 0.0001f)

private data class PressScaleElement(val enabled: Boolean) : ModifierNodeElement<PressScaleNode>() {
    override fun create() = PressScaleNode(enabled)

    override fun update(node: PressScaleNode) {
        node.enabled = enabled
        if (!enabled) node.release()
    }
}

private class PressScaleNode(var enabled: Boolean) : Modifier.Node(), PointerInputModifierNode, LayoutModifierNode {
    private val scale = Animatable(1f, visibilityThreshold = 0.0001f)
    private var pressed = false

    override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
        if (pass != PointerEventPass.Final) return
        val change = pointerEvent.changes.firstOrNull() ?: return
        when {
            change.changedToDownIgnoreConsumed() && enabled -> press()
            !pressed -> Unit
            change.changedToUpIgnoreConsumed() -> release()
            // a parent scroll consumes the move before the Final pass reaches us
            change.isConsumed && !change.changedToUpIgnoreConsumed() -> release()
            change.isOutOfBounds(bounds, Size.Zero) -> release()
        }
    }

    override fun onCancelPointerInput() = release()

    private fun press() {
        pressed = true
        coroutineScope.launch { scale.animateTo(PRESSED_SCALE, PressInSpring) }
    }

    fun release() {
        if (!pressed || !isAttached) return
        pressed = false
        coroutineScope.launch { scale.animateTo(1f, PressOutSpring) }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale.value
                scaleY = scale.value
            }
        }
    }
}

fun Modifier.pressable(
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = PlataPressIndication,
    onClick: () -> Unit,
): Modifier = clickable(
    interactionSource = interactionSource,
    indication = indication,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onClick = onClick,
)
