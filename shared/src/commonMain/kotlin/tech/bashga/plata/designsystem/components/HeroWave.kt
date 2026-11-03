package tech.bashga.plata.designsystem.components

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlin.math.PI
import tech.bashga.plata.designsystem.theme.PlataPalette
import tech.bashga.plata.designsystem.theme.PlataTheme
import tech.bashga.plata.dev.dials.DialPanel
import tech.bashga.plata.navigation.rememberReduceMotion

/**
 * The "Hero wave" dials panel: god rays fanning from a point (by default off the hero's top-left corner), one layer of
 * rays per colour over [colorBack], stacked more additively as bloom rises and tinted with the bloom colour.
 */
internal object HeroWaveDials {
    private val panel = DialPanel("Hero wave")

    private val colors = panel.folder("colors")
    private val color1 by colors.color("color1", Color(0xFF872808))
    private val color2 by colors.color("color2", Color(0xFFFF5100))
    private val color3 by colors.color("color3", Color(0xFF57210F))
    private val color4 by colors.color("color4", Color(0xFFFF5100))
    private val colorBack by colors.color("colorBack", Color(0xFFE84711))
    private val colorBloom by colors.color("colorBloom", PlataPalette.BrandAmber)

    private val rays = panel.folder("rays")
    private val bloom by rays.slider("bloom", 0.3f, 0f..1f, 0.01f)
    private val intensity by rays.slider("intensity", 0.92f, 0f..1f, 0.01f)
    private val density by rays.slider("density", 0.12f, 0f..1f, 0.01f)
    private val spotty by rays.slider("spotty", 0.46f, 0f..1f, 0.01f)
    private val midSize by rays.slider("midSize", 0.3f, 0f..1f, 0.01f)
    private val midIntensity by rays.slider("midIntensity", 0.4f, 0f..1f, 0.01f)

    private val motion = panel.folder("motion")
    private val speed by motion.slider("speed", 0.5f, 0f..2f, 0.01f)

    // Paper's sizing: the rays' centre sits [offsetX], [offsetY] half-sides from the middle of the hero's square
    private val placement = panel.folder("placement")
    private val scale by placement.slider("scale", 1.24f, 0.01f..4f, 0.01f)
    private val rotation by placement.slider("rotation", 8f, 0f..360f, 1f, "°")
    private val offsetX by placement.slider("offsetX", -1f, -1f..1f, 0.01f)
    private val offsetY by placement.slider("offsetY", -1f, -1f..1f, 0.01f)

    /** Hands [set] every uniform but the size, for [seconds] on the wave's clock. */
    fun uniforms(seconds: Float, set: (name: String, values: FloatArray) -> Unit) {
        set("time", floatArrayOf(seconds * speed))
        listOf("color1" to color1, "color2" to color2, "color3" to color3, "color4" to color4, "colorBack" to colorBack, "colorBloom" to colorBloom)
            .forEach { (name, color) -> set(name, floatArrayOf(color.red, color.green, color.blue, color.alpha)) }
        listOf("bloom" to bloom, "intensity" to intensity, "density" to density, "spotty" to spotty, "midSize" to midSize, "midIntensity" to midIntensity, "scale" to scale)
            .forEach { (name, value) -> set(name, floatArrayOf(value)) }
        set("rotation", floatArrayOf(rotation * PI.toFloat() / 180f))
        set("offset", floatArrayOf(offsetX, offsetY))
    }
}

// Paper's god-rays shader (github.com/paper-design/shaders, Apache License 2.0), with the noise texture swapped for a hash
internal const val RaysSksl = """
uniform float2 size;
uniform float time;
uniform float4 color1;
uniform float4 color2;
uniform float4 color3;
uniform float4 color4;
uniform float4 colorBack;
uniform float4 colorBloom;
uniform float bloom;
uniform float intensity;
uniform float density;
uniform float spotty;
uniform float midSize;
uniform float midIntensity;
uniform float scale;
uniform float rotation;
uniform float2 offset;

const float TWO_PI = 6.28318530718;

float2 rotate(float2 uv, float th) {
    return float2(cos(th) * uv.x - sin(th) * uv.y, sin(th) * uv.x + cos(th) * uv.y);
}

float hash11(float p) {
    p = fract(p * 0.3183099) + 0.1;
    p *= p + 19.19;
    return fract(p * p);
}

float hash21(float2 p) {
    p = fract(p * float2(0.3183099, 0.3678794)) + 0.1;
    p += dot(p, p + 19.19);
    return fract(p.x * p.y);
}

float valueNoise(float2 st) {
    float2 i = floor(st);
    float2 f = fract(st);
    float a = hash21(i);
    float b = hash21(i + float2(1.0, 0.0));
    float c = hash21(i + float2(0.0, 1.0));
    float d = hash21(i + float2(1.0, 1.0));
    float2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

// the angle wraps at -x, so a second reading from 0 to TWO_PI covers the seam there
float raysShape(float2 uv, float r, float freq, float power) {
    float a = atan(uv.y, uv.x);
    float left = pow(valueNoise(float2(a * freq, r)), power);
    float right = pow(valueNoise(float2(fract(a / TWO_PI) * TWO_PI * freq, r)), power);
    return mix(right, left, smoothstep(-0.15, 0.15, uv.x));
}

void addRays(float i, float4 color, float2 uv, float radius, float t, float spots, float power, float middle, inout float3 accColor, inout float accAlpha) {
    float2 rotated = rotate(uv, i + 1.0);
    float r1 = radius * (1.0 + 0.4 * i) - 3.0 * t;
    float r2 = 0.5 * radius * (1.0 + spots) - 2.0 * t;
    float rayDensity = 6.0 * density + step(0.5, density) * pow(4.5 * max(density - 0.5, 0.0), 4.0);
    float f = mix(1.0, 3.0 + 0.5 * i, hash11(i * 15.0)) * rayDensity;
    float ray = raysShape(rotated, r1, 5.0 * f, power) * raysShape(rotated, r2, 4.0 * f, power);
    ray = clamp(ray + (1.0 + 4.0 * ray) * middle, 0.0, 1.0);
    float srcAlpha = color.a * ray;
    float3 srcColor = color.rgb * srcAlpha;
    float3 blendColor = accColor + (1.0 - accAlpha) * srcColor;
    float blendAlpha = accAlpha + (1.0 - accAlpha) * srcAlpha;
    accColor = mix(blendColor, accColor + srcColor, bloom);
    accAlpha = mix(blendAlpha, accAlpha + srcAlpha, bloom);
}

float3 rays(float2 p) {
    float2 uv = p / size - 0.5;
    uv.y = -uv.y;
    float2 objectUV = uv * size / min(size.x, size.y);
    objectUV += float2(-offset.x, offset.y);
    objectUV = rotate(objectUV / scale, rotation);

    float t = 0.2 * time;
    float radius = length(objectUV);
    float spots = 6.5 * abs(spotty);
    float power = 4.0 - 3.0 * clamp(intensity, 0.0, 1.0);
    float mid = 10.0 * abs(midSize);
    float middle = pow(midIntensity, 0.3) * (1.0 - smoothstep(0.02 * mid, max(mid, 1e-6), 3.0 * radius));
    middle = pow(middle, 5.0);

    float3 accColor = float3(0.0);
    float accAlpha = 0.0;
    addRays(0.0, color1, objectUV, radius, t, spots, power, middle, accColor, accAlpha);
    addRays(1.0, color2, objectUV, radius, t, spots, power, middle, accColor, accAlpha);
    addRays(2.0, color3, objectUV, radius, t, spots, power, middle, accColor, accAlpha);
    addRays(3.0, color4, objectUV, radius, t, spots, power, middle, accColor, accAlpha);

    float3 withBloom = accColor + accAlpha * colorBloom.rgb * colorBloom.a;
    accColor = mix(accColor, withBloom, bloom);
    return clamp(accColor + (1.0 - accAlpha) * colorBack.rgb * colorBack.a, 0.0, 1.0);
}

// a 1/256 dither against banding in the long gradients
float3 dither(float3 color, float2 p) {
    return color + (fract(sin(dot(0.014 * p, float2(12.9898, 78.233))) * 43758.5453123) - 0.5) / 256.0;
}
"""

internal const val WaveSksl = RaysSksl + """
half4 main(float2 p) {
    return half4(half3(dither(rays(p), p)), 1.0);
}
"""

/** The hero's clock and where it sits in the window, so glass over it can redraw the rays it covers. */
@Stable
class HeroWaveState internal constructor(internal val clock: State<Float>) {
    internal var origin by mutableStateOf(Offset.Zero)
    internal var size by mutableStateOf(Size.Zero)
}

/** A hero that flows slowly, or stands still under reduce motion. */
@Composable
fun rememberHeroWaveState(): HeroWaveState {
    val clock = rememberWaveClock(!rememberReduceMotion())
    return remember(clock) { HeroWaveState(clock) }
}

/** The hero that [GlassSurface]s draw over. */
val LocalHeroWave = compositionLocalOf<HeroWaveState?> { null }

/**
 * The god-rays hero behind Home's greeting and tiles, fading into [fadeTo] over its last [fade]. An AGSL RuntimeShader
 * on Android, a Skia runtime effect on iOS.
 */
@Composable
fun HeroWave(
    modifier: Modifier = Modifier,
    state: HeroWaveState = rememberHeroWaveState(),
    fade: Dp = 96.dp,
    fadeTo: Color = PlataTheme.colors.surfaceBackground,
) {
    val wave = rememberShaderBrush(WaveSksl)
    Box(
        modifier.onGloballyPositioned {
            state.origin = it.positionInWindow()
            state.size = it.size.toSize()
        },
    ) {
        Spacer(
            Modifier.matchParentSize().drawWave(state.clock) { width, height, time ->
                wave { set ->
                    set("size", floatArrayOf(width, height))
                    HeroWaveDials.uniforms(time, set)
                }
            },
        )
        Spacer(
            Modifier.matchParentSize().drawWithCache {
                val sink = Brush.verticalGradient(*WaveDepth)
                val fadeOut = Brush.verticalGradient(
                    listOf(fadeTo.copy(alpha = 0f), fadeTo),
                    startY = size.height - fade.toPx(),
                    endY = size.height,
                )
                onDrawBehind {
                    drawRect(sink)
                    drawRect(fadeOut)
                }
            },
        )
    }
}

/**
 * A brush from [source]'s runtime shader, its uniforms set by the block it's given, or null where the platform can't
 * run one.
 */
@Composable
internal expect fun rememberShaderBrush(source: String): (uniforms: ((name: String, values: FloatArray) -> Unit) -> Unit) -> Brush?

internal const val SinkStart = 0.35f
internal const val SinkOpacity = 0.9f

// the foot sinks into HeroDepth from SinkStart down to SinkOpacity at the bottom, eased as a smoothstep
private val WaveDepth = (0..8).map { it / 8f }.map { y ->
    val t = ((y - SinkStart) / (1 - SinkStart)).coerceIn(0f, 1f)
    y to PlataPalette.HeroDepth.copy(alpha = t * t * (3 - 2 * t) * SinkOpacity)
}.toTypedArray()

/** The still wave, where a platform can't draw the live one. */
internal val WaveStillColors = listOf(PlataPalette.BrandAmber, PlataPalette.BrandOrange, PlataPalette.BrandRed)

/** Seconds since the wave appeared, ticking each frame while [animate]. */
@Composable
internal fun rememberWaveClock(animate: Boolean): State<Float> {
    val time = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(animate) {
        if (!animate) return@LaunchedEffect
        val start = withInfiniteAnimationFrameMillis { it } - (time.floatValue * 1000).toLong()
        while (true) withInfiniteAnimationFrameMillis { time.floatValue = (it - start) / 1000f }
    }
    return time
}

/** Fills with [wave] for the current size and clock, or the still brand gradient where it gives none. */
internal fun Modifier.drawWave(clock: State<Float>, wave: (width: Float, height: Float, time: Float) -> Brush?): Modifier =
    drawWithCache {
        val still = Brush.verticalGradient(WaveStillColors)
        onDrawBehind { drawRect(wave(size.width, size.height, clock.value) ?: still) }
    }

@Preview
@Composable
internal fun HeroWavePreview() = PlataTheme {
    HeroWave(Modifier.fillMaxWidth().height(360.dp))
}
