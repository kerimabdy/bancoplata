package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeShader
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

private val WaveEffect by lazy { RuntimeEffect.makeForShader(WaveSksl) }

@Composable
internal actual fun HeroWaveLayer(modifier: Modifier, animate: Boolean) {
    Spacer(modifier.drawWave(rememberWaveClock(animate), rememberWaveBrush()))
}

@Composable
private fun rememberWaveBrush(): (Float, Float, Float) -> ShaderBrush {
    val builder = remember { RuntimeShaderBuilder(WaveEffect) }
    return { width, height, time ->
        builder.uniform("size", width, height)
        HeroWaveDials.uniforms(time, builder::uniform)
        ShaderBrush(builder.makeShader().asComposeShader())
    }
}

/** Draws the wave once at launch, under the app, so Skia compiles it to Metal before Home first shows it. */
@Composable
internal fun PrewarmHeroWave() {
    Spacer(Modifier.size(1.dp).drawWave(remember { mutableStateOf(0f) }, rememberWaveBrush()))
}

private fun RuntimeShaderBuilder.uniform(name: String, values: FloatArray) = when (values.size) {
    1 -> uniform(name, values[0])
    2 -> uniform(name, values[0], values[1])
    3 -> uniform(name, values[0], values[1], values[2])
    else -> uniform(name, values[0], values[1], values[2], values[3])
}
