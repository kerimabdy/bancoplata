package tech.bashga.plata.designsystem.foundation

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
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

fun Modifier.pressable(
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier = clickable(
    interactionSource = null,
    indication = PlataPressIndication,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onClick = onClick,
)
