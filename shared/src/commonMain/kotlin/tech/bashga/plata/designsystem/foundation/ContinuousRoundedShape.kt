package tech.bashga.plata.designsystem.foundation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Rounded rectangle with iOS continuous ("squircle") corners, using Figma's corner-smoothing construction.
 * [smoothing] 0.6 matches iOS `cornerCurve = .continuous`; 0 gives a plain circular corner.
 * A radius of half the short side or more (e.g. `radii.full`) yields a capsule.
 */
@Immutable
class ContinuousRoundedShape(
    private val topStart: Dp,
    private val topEnd: Dp = topStart,
    private val bottomEnd: Dp = topStart,
    private val bottomStart: Dp = topStart,
    private val smoothing: Float = IOS_SMOOTHING,
) : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val ltr = layoutDirection == LayoutDirection.Ltr
        val (tl, tr, br, bl) = with(density) {
            listOf(
                if (ltr) topStart else topEnd,
                if (ltr) topEnd else topStart,
                if (ltr) bottomEnd else bottomStart,
                if (ltr) bottomStart else bottomEnd,
            ).map { it.toPx() }
        }
        val path = PathParser().parsePathString(squirclePath(size.width, size.height, tl, tr, br, bl, smoothing)).toPath()
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean = other is ContinuousRoundedShape &&
        other.topStart == topStart && other.topEnd == topEnd && other.bottomEnd == bottomEnd &&
        other.bottomStart == bottomStart && other.smoothing == smoothing

    override fun hashCode(): Int =
        (((topStart.hashCode() * 31 + topEnd.hashCode()) * 31 + bottomEnd.hashCode()) * 31 + bottomStart.hashCode()) * 31 +
            smoothing.hashCode()

    companion object {
        const val IOS_SMOOTHING = 0.6f
    }
}

fun ContinuousTopRoundedShape(radius: Dp, smoothing: Float = ContinuousRoundedShape.IOS_SMOOTHING) =
    ContinuousRoundedShape(topStart = radius, topEnd = radius, bottomEnd = 0.dp, bottomStart = 0.dp, smoothing = smoothing)

private class Corner(val a: Float, val b: Float, val c: Float, val d: Float, val p: Float, val arc: Float, val r: Float)

private fun rad(deg: Float) = deg * PI.toFloat() / 180f

private fun corner(radius: Float, smoothingIn: Float, budget: Float): Corner {
    val r = min(radius, budget)
    if (r <= 0f) return Corner(0f, 0f, 0f, 0f, 0f, 0f, 0f)
    val smoothing = min(smoothingIn, budget / r - 1f).coerceAtLeast(0f)
    val p = min((1f + smoothing) * r, budget)
    val arcMeasure = 90f * (1f - smoothing)
    val arc = sin(rad(arcMeasure / 2f)) * r * sqrt(2f)
    val angleAlpha = (90f - arcMeasure) / 2f
    val p3ToP4 = r * tan(rad(angleAlpha / 2f))
    val angleBeta = 45f * smoothing
    val c = p3ToP4 * cos(rad(angleBeta))
    val d = c * tan(rad(angleBeta))
    val b = (p - arc - c - d) / 3f
    return Corner(2f * b, b, c, d, p, arc, r)
}

private fun squirclePath(w: Float, h: Float, tl: Float, tr: Float, br: Float, bl: Float, smoothing: Float): String {
    val budget = min(w, h) / 2f
    val cTl = corner(tl, smoothing, budget)
    val cTr = corner(tr, smoothing, budget)
    val cBr = corner(br, smoothing, budget)
    val cBl = corner(bl, smoothing, budget)
    return buildString {
        append("M ${w - cTr.p} 0 ")
        with(cTr) {
            if (r > 0f) append("c $a 0 ${a + b} 0 ${a + b + c} $d a $r $r 0 0 1 $arc $arc c $d $c $d ${b + c} $d ${a + b + c} ")
        }
        append("L $w ${h - cBr.p} ")
        with(cBr) {
            if (r > 0f) append("c 0 $a 0 ${a + b} ${-d} ${a + b + c} a $r $r 0 0 1 ${-arc} $arc c ${-c} $d ${-(b + c)} $d ${-(a + b + c)} $d ")
        }
        append("L ${cBl.p} $h ")
        with(cBl) {
            if (r > 0f) append("c ${-a} 0 ${-(a + b)} 0 ${-(a + b + c)} ${-d} a $r $r 0 0 1 ${-arc} ${-arc} c ${-d} ${-c} ${-d} ${-(b + c)} ${-d} ${-(a + b + c)} ")
        }
        append("L 0 ${cTl.p} ")
        with(cTl) {
            if (r > 0f) append("c 0 ${-a} 0 ${-(a + b)} $d ${-(a + b + c)} a $r $r 0 0 1 $arc ${-arc} c $c ${-d} ${b + c} ${-d} ${a + b + c} ${-d} ")
        }
        append("Z")
    }
}
