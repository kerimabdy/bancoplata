package tech.bashga.plata.designsystem.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeShader
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

// the desktop preview window draws the same SkSL through Skia
@Composable
internal actual fun HeroWaveLayer(modifier: Modifier, animate: Boolean) {
    val clock = rememberWaveClock(animate)
    val builder = remember { RuntimeShaderBuilder(RuntimeEffect.makeForShader(WaveSksl)) }
    Spacer(
        modifier.drawWave(clock) { width, height, time ->
            builder.uniform("size", width, height)
            HeroWaveDials.uniforms(time, builder::uniform)
            ShaderBrush(builder.makeShader().asComposeShader())
        },
    )
}

private fun RuntimeShaderBuilder.uniform(name: String, values: FloatArray) = when (values.size) {
    1 -> uniform(name, values[0])
    2 -> uniform(name, values[0], values[1])
    3 -> uniform(name, values[0], values[1], values[2])
    else -> uniform(name, values[0], values[1], values[2], values[3])
}
