package tech.bashga.plata.designsystem.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import tech.bashga.plata.designsystem.theme.PlataTheme

/** False where the platform can't blur a layer (Android before 12); bars then keep their flat material. */
internal expect val isBackdropBlurSupported: Boolean

/**
 * What a screen draws under its floating bars. The content tags itself with [backdropSource]; a bar with
 * [backdropBlur] then draws that content blurred over [base], like the iOS bar materials.
 */
@Stable
class Backdrop internal constructor(internal val base: Color) {
    internal var layer: GraphicsLayer? = null
    internal var coordinates: LayoutCoordinates? = null
    internal var recording = false
    internal val readers = mutableSetOf<BackdropBlurNode>()
}

val LocalBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/**
 * Gives [content] its own [Backdrop], so a screen's bars blur that screen and not the one behind it. [base] is the
 * page colour the source content sits on, which the source itself doesn't record.
 */
@Composable
fun ProvideBackdrop(base: Color = PlataTheme.colors.surfaceBackground, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalBackdrop provides remember(base) { Backdrop(base) }, content = content)
}

/**
 * Records this content for the bars of the nearest [ProvideBackdrop]. Put it on the scroll viewport, outside the
 * scroll, and never on an ancestor of the bar: the bar would then draw itself into its own backdrop.
 */
fun Modifier.backdropSource(): Modifier = this then BackdropSourceElement

private data object BackdropSourceElement : ModifierNodeElement<BackdropSourceNode>() {
    override fun create() = BackdropSourceNode()

    override fun update(node: BackdropSourceNode) = Unit
}

private class BackdropSourceNode : Modifier.Node(), DrawModifierNode, LayoutAwareModifierNode, CompositionLocalConsumerModifierNode {
    private var backdrop: Backdrop? = null

    override fun onAttach() {
        val target = currentValueOf(LocalBackdrop) ?: return
        // one source per backdrop: a second would record into the layer the first is recording
        if (target.layer != null) return
        backdrop = target
        target.layer = requireGraphicsContext().createGraphicsLayer()
    }

    override fun onDetach() {
        backdrop?.let { target ->
            target.layer?.let { requireGraphicsContext().releaseGraphicsLayer(it) }
            target.layer = null
            target.coordinates = null
        }
        backdrop = null
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        backdrop?.coordinates = coordinates
    }

    override fun ContentDrawScope.draw() {
        val target = backdrop
        val layer = target?.layer
        if (target == null || layer == null) {
            drawContent()
            return
        }
        target.recording = true
        layer.record { this@draw.drawContent() }
        target.recording = false
        drawLayer(layer)
        target.readers.forEach { it.invalidateDraw() }
    }
}

/**
 * Fills the element with [tint] over the blurred backdrop of the nearest [ProvideBackdrop]. With no source on screen
 * or no platform blur it draws [fallback] instead.
 */
fun Modifier.backdropBlur(tint: Color, fallback: Color, radius: Dp = BackdropRadius): Modifier =
    // its own layer, so the source invalidating the bar redraws the bar alone and not the screen holding both
    this.graphicsLayer().then(BackdropBlurElement(tint, fallback, radius))

// iOS bar materials blur about 30pt and lift saturation so colours read through the frost
private val BackdropRadius = 30.dp
private const val BackdropSaturation = 1.8f

private data class BackdropBlurElement(val tint: Color, val fallback: Color, val radius: Dp) : ModifierNodeElement<BackdropBlurNode>() {
    override fun create() = BackdropBlurNode(tint, fallback, radius)

    override fun update(node: BackdropBlurNode) {
        node.tint = tint
        node.fallback = fallback
        node.radius = radius
        node.invalidateDraw()
    }
}

internal class BackdropBlurNode(var tint: Color, var fallback: Color, var radius: Dp) :
    Modifier.Node(), DrawModifierNode, LayoutAwareModifierNode, CompositionLocalConsumerModifierNode {
    private var backdrop: Backdrop? = null
    private var coordinates: LayoutCoordinates? = null
    private var blurLayer: GraphicsLayer? = null
    private var insideSource = false

    override fun onAttach() {
        backdrop = currentValueOf(LocalBackdrop)?.also { it.readers += this }
        if (isBackdropBlurSupported) {
            blurLayer = requireGraphicsContext().createGraphicsLayer().apply {
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(BackdropSaturation) })
            }
        }
    }

    override fun onDetach() {
        backdrop?.readers?.remove(this)
        backdrop = null
        blurLayer?.let { requireGraphicsContext().releaseGraphicsLayer(it) }
        blurLayer = null
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        this.coordinates = coordinates
    }

    override fun ContentDrawScope.draw() {
        val target = backdrop
        val source = target?.layer
        val sourceCoordinates = target?.coordinates
        val own = coordinates
        val blur = blurLayer
        // a bar inside its own source would draw the recording into itself, so it keeps the flat fill for good
        if (target?.recording == true) insideSource = true
        if (target == null || source == null || sourceCoordinates == null || own == null || blur == null || insideSource ||
            !sourceCoordinates.isAttached || !own.isAttached
        ) {
            drawRect(fallback)
            drawContent()
            return
        }
        val origin = own.localPositionOf(sourceCoordinates, Offset.Zero)
        val px = radius.toPx()
        blur.renderEffect = BlurEffect(px, px, TileMode.Clamp)
        // record past the bottom edge so the frost there blurs what is about to scroll under, not a clamped edge
        val reach = (px * 2).toInt()
        blur.record(IntSize(size.width.toInt(), size.height.toInt() + reach)) {
            translate(origin.x, origin.y) { drawLayer(source) }
        }
        clipRect {
            drawRect(target.base)
            drawLayer(blur)
        }
        drawRect(tint)
        drawContent()
    }
}
