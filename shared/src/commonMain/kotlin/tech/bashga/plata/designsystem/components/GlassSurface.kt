package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import tech.bashga.plata.designsystem.foundation.ContinuousRoundedShape
import tech.bashga.plata.designsystem.foundation.pressable
import tech.bashga.plata.designsystem.theme.PlataPalette
import tech.bashga.plata.dev.dials.DialOption
import tech.bashga.plata.dev.dials.DialPanel

/** The "Glass" dials panel: Home's glass as a shader redrawing the hero bent through it, or the plain frosted wash. */
internal object GlassDials {
    private val panel = DialPanel("Glass")
    private val material by panel.select("material", listOf(DialOption("shader"), DialOption("frosted")))
    private val lens = panel.folder("lens")
    private val refraction by lens.slider("refraction", 10f, 0f..40f, 0.5f, "pt")
    private val edge by lens.slider("edge", 16f, 1f..60f, 0.5f, "pt")
    private val blur by lens.slider("blur", 3f, 0f..16f, 0.5f, "pt")
    private val dispersion by lens.slider("dispersion", 0.3f, 0f..1f, 0.01f)
    private val surface = panel.folder("surface")
    private val tint by surface.slider("tint", 0.12f, 0f..1f, 0.01f)
    private val rim by surface.slider("rim", 0.5f, 0f..1f, 0.01f)
    private val rimWidth by surface.slider("rimWidth", 1.5f, 0.5f..8f, 0.5f, "pt")
    private val shadow by surface.slider("shadow", 0.25f, 0f..1f, 0.01f)
    private val light by surface.slider("light", 135f, 0f..360f, 1f, "°")

    val shader: Boolean get() = material == "shader"

    /** Hands [set] the glass's own uniforms, lengths in px for [density]. */
    fun uniforms(density: Float, set: (name: String, values: FloatArray) -> Unit) {
        listOf("refraction" to refraction, "edge" to edge, "blur" to blur, "rimWidth" to rimWidth)
            .forEach { (name, value) -> set(name, floatArrayOf(value * density)) }
        listOf("dispersion" to dispersion, "tint" to tint, "rim" to rim, "shadow" to shadow)
            .forEach { (name, value) -> set(name, floatArrayOf(value)) }
        set("light", floatArrayOf(light * PI.toFloat() / 180f))
    }
}

/**
 * A glass panel behind [content], rounded by [cornerRadius] (a capsule when null). Over the [LocalHeroWave] it redraws
 * the hero it covers, bent and softened at the rim like thick glass and lit along its edge; elsewhere, where the
 * platform can't run the shader, or with the Glass dial set to frosted, it's [frostedFill] with an optional 1pt [frostedRim].
 */
@Composable
fun GlassSurface(
    frostedFill: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp? = null,
    frostedRim: Color? = null,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val hero = LocalHeroWave.current
    val glass = rememberShaderBrush(GlassSksl)
    val shape = cornerRadius?.let { ContinuousRoundedShape(it) } ?: CircleShape
    var position by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier
            .onGloballyPositioned { position = it.positionInWindow() }
            .drawWithCache {
                val outline = shape.createOutline(size, layoutDirection, this)
                val corner = cornerRadius?.toPx() ?: (minOf(size.width, size.height) / 2)
                val rimStroke = Stroke(1.dp.toPx())
                onDrawBehind {
                    val brush = if (hero != null && GlassDials.shader) {
                        glass { set ->
                            set("size", floatArrayOf(hero.size.width, hero.size.height))
                            HeroWaveDials.uniforms(hero.clock.value, set)
                            val origin = position - hero.origin
                            set("origin", floatArrayOf(origin.x, origin.y))
                            set("box", floatArrayOf(size.width, size.height))
                            set("corner", floatArrayOf(corner))
                            val depth = PlataPalette.HeroDepth
                            set("depth", floatArrayOf(depth.red, depth.green, depth.blue, depth.alpha))
                            set("sink", floatArrayOf(SinkStart, SinkOpacity))
                            GlassDials.uniforms(density, set)
                        }
                    } else {
                        null
                    }
                    if (brush != null) {
                        drawOutline(outline, brush)
                    } else {
                        drawOutline(outline, frostedFill)
                        if (frostedRim != null) drawOutline(outline, frostedRim, style = rimStroke)
                    }
                }
            }
            .then(if (onClick != null) Modifier.pressable(onClickLabel = onClickLabel, onClick = onClick) else Modifier),
        contentAlignment,
    ) { content() }
}

// p is in the glass's own px; the hero under it sits at origin + p
private const val GlassSksl = RaysSksl + """
uniform float2 origin;
uniform float2 box;
uniform float corner;
uniform float4 depth;
uniform float2 sink;
uniform float refraction;
uniform float edge;
uniform float blur;
uniform float dispersion;
uniform float tint;
uniform float rim;
uniform float rimWidth;
uniform float shadow;
uniform float light;

float boxSdf(float2 p) {
    float2 q = abs(p - 0.5 * box) - 0.5 * box + corner;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - corner;
}

// the hero as HeroWave draws it, its foot sinking into the depth colour
float3 backdrop(float2 q) {
    float t = clamp((q.y / size.y - sink.x) / (1.0 - sink.x), 0.0, 1.0);
    return mix(rays(q), depth.rgb, t * t * (3.0 - 2.0 * t) * sink.y * depth.a);
}

half4 main(float2 p) {
    float d = boxSdf(p);
    float2 e = float2(0.5, 0.0);
    float2 n = float2(boxSdf(p + e.xy) - boxSdf(p - e.xy), boxSdf(p + e.yx) - boxSdf(p - e.yx));
    n = n / max(length(n), 1e-4);
    float bend = 1.0 - clamp(-d / edge, 0.0, 1.0);
    bend *= bend;

    // pulling the sample inward squeezes what's near the rim, as a thick lens does
    float2 q = origin + p - n * refraction * bend;
    float3 color = backdrop(q);
    if (blur > 0.0) {
        color = 0.36 * color + 0.16 * (backdrop(q + float2(blur, 0.0)) + backdrop(q - float2(blur, 0.0))
            + backdrop(q + float2(0.0, blur)) + backdrop(q - float2(0.0, blur)));
    }
    if (dispersion > 0.0 && bend > 0.001) {
        float2 shift = n * refraction * bend * dispersion;
        color.r = mix(color.r, backdrop(q + shift).r, bend);
        color.b = mix(color.b, backdrop(q - shift).b, bend);
    }
    color = mix(color, float3(1.0), tint);

    float band = 1.0 - smoothstep(0.0, rimWidth, -d);
    float facing = dot(n, float2(cos(light), -sin(light)));
    color *= 1.0 - shadow * band * max(-facing, 0.0);
    color += rim * band * (0.3 + 0.7 * max(facing, 0.0));
    return half4(half3(dither(clamp(color, 0.0, 1.0), p)), 1.0);
}
"""
